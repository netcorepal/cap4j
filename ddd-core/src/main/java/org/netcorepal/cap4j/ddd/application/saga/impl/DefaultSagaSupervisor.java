package org.netcorepal.cap4j.ddd.application.saga.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestInterceptor;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.command.NoneResultCommandParam;
import org.netcorepal.cap4j.ddd.application.impl.DefaultRequestSupervisor;
import org.netcorepal.cap4j.ddd.application.query.ListQuery;
import org.netcorepal.cap4j.ddd.application.query.PageQuery;
import org.netcorepal.cap4j.ddd.application.query.Query;
import org.netcorepal.cap4j.ddd.application.saga.*;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 默认SagaSupervisor实现
 *
 * @author binking338
 * @date 2024/10/12
 */
@RequiredArgsConstructor
public class DefaultSagaSupervisor implements SagaSupervisor, SagaProcessSupervisor {
    private final List<RequestHandler<?, ?>> requestHandlers;
    private final List<RequestInterceptor<?, ?>> requestInterceptors;
    private final Validator validator;
    private final SagaRecordRepository sagaRecordRepository;
    private final String svcName;
    private final int threadPoolSize;

    /**
     * 默认Saga过期时间（分钟）
     * 一天 60*24 = 1440
     */
    private static final int DEFAULT_EVENT_EXPIRE_MINUTES = 1440;
    /**
     * 默认Saga重试次数
     */
    private static final int DEFAULT_EVENT_RETRY_TIMES = 200;

    private static final ThreadLocal<SagaRecord> SAGA_RECORD_THREAD_LOCAL = new ThreadLocal<>();

    private Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = null;
    private Map<Class<?>, List<RequestInterceptor<?, ?>>> requestInterceptorMap = null;

    private ScheduledExecutorService executor = null;

    public void init() {
        if (null != requestHandlerMap) {
            return;
        }
        synchronized (DefaultRequestSupervisor.class) {
            if (null != requestHandlerMap) {
                return;
            }
        }
        requestHandlerMap = new HashMap<>();
        requestInterceptorMap = new HashMap<>();
        for (RequestHandler<?, ?> requestHandler : requestHandlers) {
            Class<?> requestPayloadClass = ClassUtils.resolveGenericTypeClass(
                    requestHandler, 0,
                    RequestHandler.class,
                    Command.class, NoneResultCommandParam.class,
                    Query.class, ListQuery.class, PageQuery.class,
                    SagaHandler.class);
            requestHandlerMap.put(requestPayloadClass, requestHandler);
        }
        for (RequestInterceptor<?, ?> requestInterceptor : requestInterceptors) {
            Class<?> requestPayloadClass = ClassUtils.resolveGenericTypeClass(
                    requestInterceptor, 0,
                    RequestInterceptor.class);
            List<RequestInterceptor<?, ?>> interceptors = requestInterceptorMap.computeIfAbsent(requestPayloadClass, cls -> new ArrayList<>());
            interceptors.add(requestInterceptor);
        }

        if (null != this.executor) {
            return;
        }
        synchronized (this) {
            if (null != this.executor) {
                return;
            }
            this.executor = Executors.newScheduledThreadPool(threadPoolSize);
        }
    }

    @Override
    public <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request) {
        if(validator != null){
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if(!constraintViolations.isEmpty()){
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        SagaRecord sagaRecord = createSagaRecord(request.getClass().getName(), request);
        return internalSend(request, sagaRecord);
    }

    @Override
    public <REQUEST extends SagaParam<?>> SagaRecord sendAsync(REQUEST request) {
        if(validator != null){
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if(!constraintViolations.isEmpty()){
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        SagaRecord sagaRecord = createSagaRecord(request.getClass().getName(), request);
        executor.submit(() ->
                internalSend((SagaParam) request, sagaRecord)
        );

        return sagaRecord;
    }

    @Override
    public SagaRecord getById(String id) {
        return sagaRecordRepository.getById(id);
    }

    @Override
    public Object resume(SagaRecord saga) {
        if (!saga.beginSaga(LocalDateTime.now())) {
            sagaRecordRepository.save(saga);
            return saga.getResult();
        }
        SagaParam param = saga.getParam();
        if(validator != null){
            Set<ConstraintViolation<SagaParam>> constraintViolations = validator.validate(param);
            if(!constraintViolations.isEmpty()){
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        return internalSend(param, saga);
    }

    @Override
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE sendProcess(String processCode, REQUEST request) {
        SagaRecord sagaRecord = SAGA_RECORD_THREAD_LOCAL.get();
        if (null == sagaRecord) {
            throw new IllegalStateException("No SagaRecord found in thread local");
        }
        if (sagaRecord.isSagaProcessExecuted(processCode)) {
            RESPONSE subResult = (RESPONSE) sagaRecord.getSagaProcessResult(processCode);
            return subResult;
        }

        sagaRecord.beginSagaProcess(LocalDateTime.now(), processCode, request);
        sagaRecordRepository.save(sagaRecord);
        try {
            RESPONSE response = RequestSupervisor.getInstance().send(request);

            sagaRecord.endSagaProcess(LocalDateTime.now(), processCode, response);
            sagaRecordRepository.save(sagaRecord);
            return response;
        } catch (Throwable throwable) {
            sagaRecord.sagaProcessOccuredException(LocalDateTime.now(), processCode, throwable);
            sagaRecordRepository.save(sagaRecord);
            throw throwable;
        }
    }

    /**
     * 创建SagaRecord
     *
     * @param sagaType
     * @param request
     * @return
     */
    protected SagaRecord createSagaRecord(String sagaType, SagaParam<?> request) {
        LocalDateTime now = LocalDateTime.now();
        SagaRecord sagaRecord = sagaRecordRepository.create();
        sagaRecord.init(request, svcName, sagaType, now, Duration.ofMinutes(DEFAULT_EVENT_EXPIRE_MINUTES), DEFAULT_EVENT_RETRY_TIMES);
        sagaRecord.beginSaga(now);
        sagaRecordRepository.save(sagaRecord);
        return sagaRecord;
    }

    /**
     * 执行Saga
     *
     * @param request
     * @param sagaRecord
     * @param <REQUEST>
     * @param <RESPONSE>
     * @return
     */
    protected <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE internalSend(REQUEST request, SagaRecord sagaRecord) {
        try {
            SAGA_RECORD_THREAD_LOCAL.set(sagaRecord);
            requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                    .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).preRequest(request));
            RESPONSE response = ((RequestHandler<REQUEST, RESPONSE>) requestHandlerMap.get(request.getClass())).exec(request);
            requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                    .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).postRequest(request, response));

            sagaRecord.endSaga(LocalDateTime.now(), response);
            sagaRecordRepository.save(sagaRecord);
            return response;
        } catch (Throwable throwable) {
            sagaRecord.occuredException(LocalDateTime.now(), throwable);
            sagaRecordRepository.save(sagaRecord);
            throw throwable;
        } finally {
            SAGA_RECORD_THREAD_LOCAL.remove();
        }
    }

}
