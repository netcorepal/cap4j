package org.netcorepal.cap4j.ddd.application.saga.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 默认SagaSupervisor实现
 *
 * @author binking338
 * @date 2024/10/12
 */
@RequiredArgsConstructor
public class DefaultSagaSupervisor implements SagaSupervisor, SagaProcessSupervisor, SagaManager {
    private final List<RequestHandler<?, ?>> requestHandlers;
    private final List<RequestInterceptor<?, ?>> requestInterceptors;
    private final Validator validator;
    private final SagaRecordRepository sagaRecordRepository;
    private final String svcName;
    private final int threadPoolSize;
    private final String threadFactoryClassName;

    /**
     * 默认Saga过期时间（分钟）
     * 一天 60*24 = 1440
     */
    private static final int DEFAULT_SAGA_EXPIRE_MINUTES = 1440;
    /**
     * 默认Saga重试次数
     */
    private static final int DEFAULT_SAGA_RETRY_TIMES = 200;
    /**
     * 本地调度时间阈值
     */
    private static final int LOCAL_SCHEDULE_ON_INIT_TIME_THRESHOLDS_MINUTES = 2;

    private static final ThreadLocal<SagaRecord> SAGA_RECORD_THREAD_LOCAL = new ThreadLocal<>();

    private Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = null;
    private Map<Class<?>, List<RequestInterceptor<?, ?>>> requestInterceptorMap = null;

    private ScheduledExecutorService executorService = null;

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

        if (null != this.executorService) {
            return;
        }
        synchronized (this) {
            if (null != this.executorService) {
                return;
            }
            if (threadFactoryClassName == null || threadFactoryClassName.isEmpty()) {
                executorService = Executors.newScheduledThreadPool(threadPoolSize);
            } else {
                Class<?> threadFactoryClass = org.springframework.objenesis.instantiator.util.ClassUtils.getExistingClass(this.getClass().getClassLoader(), threadFactoryClassName);
                ThreadFactory threadFactory = (ThreadFactory) org.springframework.objenesis.instantiator.util.ClassUtils.newInstance(threadFactoryClass);
                if (threadFactory != null) {
                    executorService = Executors.newScheduledThreadPool(threadPoolSize, threadFactory);
                } else {
                    executorService = Executors.newScheduledThreadPool(threadPoolSize);
                }
            }
        }
    }

    @Override
    public <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request) {
        if (validator != null) {
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        SagaRecord sagaRecord = createSagaRecord(request.getClass().getName(), request, LocalDateTime.now());
        return internalSend(request, sagaRecord);
    }

    @Override
    public <REQUEST extends SagaParam<RESPONSE>, RESPONSE> String schedule(REQUEST request, LocalDateTime schedule) {
        if (validator != null) {
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        SagaRecord sagaRecord = createSagaRecord(request.getClass().getName(), request, schedule);
        if (sagaRecord.isExecuting()) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = now.isBefore(sagaRecord.getScheduleTime())
                    ? Duration.between(LocalDateTime.now(), sagaRecord.getScheduleTime())
                    : Duration.ZERO;
            executorService.schedule(() ->
                            internalSend((SagaParam) request, sagaRecord)
                    , duration.toMillis(), TimeUnit.MILLISECONDS);
        }

        return sagaRecord.getId();
    }

    @Override
    public <R> R result(String id) {
        SagaRecord sagaRecord = sagaRecordRepository.getById(id);
        return sagaRecord == null ? null : sagaRecord.getResult();
    }

    @Override
    public void resume(SagaRecord saga) {
        if (!saga.beginSaga(LocalDateTime.now())) {
            sagaRecordRepository.save(saga);
            return;
        }
        SagaParam<?> param = saga.getParam();
        if (validator != null) {
            Set<ConstraintViolation<SagaParam<?>>> constraintViolations = validator.validate(param);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        if (saga.isExecuting()) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = now.isBefore(saga.getScheduleTime())
                    ? Duration.between(LocalDateTime.now(), saga.getScheduleTime())
                    : Duration.ZERO;
            executorService.schedule(() ->
                            internalSend((SagaParam) param, saga)
                    , duration.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public List<SagaRecord> getByNextTryTime(LocalDateTime maxNextTryTime, int limit) {
        return sagaRecordRepository.getByNextTryTime(svcName, maxNextTryTime, limit);
    }

    @Override
    public int archiveByExpireAt(LocalDateTime maxExpireAt, int limit) {
        return sagaRecordRepository.archiveByExpireAt(svcName, maxExpireAt, limit);
    }

    @Override
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE sendProcess(String processCode, REQUEST request) {
        SagaRecord sagaRecord = SAGA_RECORD_THREAD_LOCAL.get();
        if (null == sagaRecord) {
            throw new IllegalStateException("No SagaRecord found in thread local");
        }
        if (sagaRecord.isSagaProcessExecuted(processCode)) {
            RESPONSE subResult = sagaRecord.getSagaProcessResult(processCode);
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
    protected SagaRecord createSagaRecord(String sagaType, SagaParam<?> request, LocalDateTime scheduleAt) {
        SagaRecord sagaRecord = sagaRecordRepository.create();
        sagaRecord.init(request, svcName, sagaType, scheduleAt, Duration.ofMinutes(DEFAULT_SAGA_EXPIRE_MINUTES), DEFAULT_SAGA_RETRY_TIMES);
        if (scheduleAt.isBefore(LocalDateTime.now()) || Duration.between(LocalDateTime.now(), scheduleAt).toMinutes() < LOCAL_SCHEDULE_ON_INIT_TIME_THRESHOLDS_MINUTES) {
            sagaRecord.beginSaga(scheduleAt);
        }
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
