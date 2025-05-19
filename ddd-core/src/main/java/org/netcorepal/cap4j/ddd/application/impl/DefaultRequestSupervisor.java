package org.netcorepal.cap4j.ddd.application.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.*;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.command.NoneResultCommandParam;
import org.netcorepal.cap4j.ddd.application.query.ListQuery;
import org.netcorepal.cap4j.ddd.application.query.PageQuery;
import org.netcorepal.cap4j.ddd.application.query.Query;
import org.netcorepal.cap4j.ddd.application.saga.SagaParam;
import org.netcorepal.cap4j.ddd.application.saga.SagaSupervisor;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 默认请求管理器
 *
 * @author binking338
 * @date 2024/8/24
 */
@RequiredArgsConstructor
public class DefaultRequestSupervisor implements RequestSupervisor, RequestManager {
    private final List<RequestHandler<?, ?>> requestHandlers;
    private final List<RequestInterceptor<?, ?>> requestInterceptors;
    private final Validator validator;
    private final RequestRecordRepository requestRecordRepository;
    private final String svcName;
    private final int threadPoolSize;
    private final String threadFactoryClassName;

    /**
     * 默认Request过期时间（分钟）
     * 一天 60*24 = 1440
     */
    private static final int DEFAULT_REQUEST_EXPIRE_MINUTES = 1440;
    /**
     * 默认Request重试次数
     */
    private static final int DEFAULT_REQUEST_RETRY_TIMES = 200;
    /**
     * 本地调度时间阈值
     */
    private static final int LOCAL_SCHEDULE_ON_INIT_TIME_THRESHOLDS_MINUTES = 2;

    private Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = null;
    private Map<Class<?>, List<RequestInterceptor<?, ?>>> requestInterceptorMap = null;

    private ScheduledExecutorService executorService;

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
                    Query.class, ListQuery.class, PageQuery.class);
            requestHandlerMap.put(requestPayloadClass, requestHandler);
        }
        for (RequestInterceptor<?, ?> requestInterceptor : requestInterceptors) {
            Class<?> requestPayloadClass = ClassUtils.resolveGenericTypeClass(
                    requestInterceptor, 0,
                    RequestInterceptor.class);
            List<RequestInterceptor<?, ?>> interceptors = requestInterceptorMap.computeIfAbsent(requestPayloadClass, cls -> new ArrayList<>());
            interceptors.add(requestInterceptor);
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

    @Override
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request) {
        if (request instanceof SagaParam) {
            return SagaSupervisor.getInstance().send((SagaParam<RESPONSE>) request);
        }
        if (validator != null) {
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        return internalSend(request);
    }

    @Override
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> String schedule(REQUEST request, LocalDateTime schedule) {
        if (request instanceof SagaParam) {
            return SagaSupervisor.getInstance().schedule((SagaParam<?>) request, schedule);
        }
        if (validator != null) {
            Set<ConstraintViolation<REQUEST>> constraintViolations = validator.validate(request);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        init();
        RequestRecord requestRecord = createRequestRecord(request.getClass().getName(), request, schedule);
        if (requestRecord.isExecuting()) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = now.isBefore(requestRecord.getScheduleTime())
                    ? Duration.between(LocalDateTime.now(), requestRecord.getScheduleTime())
                    : Duration.ZERO;
            executorService.schedule(() -> {
                internalSend(request, requestRecord);
            }, duration.toMillis(), TimeUnit.MILLISECONDS);
        }
        return requestRecord.getId();
    }

    @Override
    public <R> R result(String requestId) {
        RequestRecord requestRecord = requestRecordRepository.getById(requestId);
        if (requestRecord == null) {
            return RequestSupervisor.getInstance().result(requestId);
        }
        return requestRecord == null ? null : requestRecord.getResult();
    }

    @Override
    public void resume(RequestRecord request) {
        if (!request.beginRequest(LocalDateTime.now())) {
            requestRecordRepository.save(request);
            return;
        }
        RequestParam<?> param = request.getParam();
        if (validator != null) {
            Set<ConstraintViolation<RequestParam<?>>> constraintViolations = validator.validate(param);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        if (request.isExecuting()) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = now.isBefore(request.getScheduleTime())
                    ? Duration.between(LocalDateTime.now(), request.getScheduleTime())
                    : Duration.ZERO;
            executorService.schedule(() -> {
                internalSend(param, request);
            }, duration.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public List<RequestRecord> getByNextTryTime(LocalDateTime maxNextTryTime, int limit) {
        return requestRecordRepository.getByNextTryTime(svcName, maxNextTryTime, limit);
    }

    @Override
    public int archiveByExpireAt(LocalDateTime maxExpireAt, int limit) {
        return requestRecordRepository.archiveByExpireAt(svcName, maxExpireAt, limit);
    }

    protected RequestRecord createRequestRecord(String requestType, RequestParam<?> request, LocalDateTime scheduleAt) {
        RequestRecord requestRecord = requestRecordRepository.create();
        requestRecord.init(request, svcName, requestType, scheduleAt, Duration.ofMinutes(DEFAULT_REQUEST_EXPIRE_MINUTES), DEFAULT_REQUEST_RETRY_TIMES);
        if (scheduleAt.isBefore(LocalDateTime.now()) || Duration.between(LocalDateTime.now(), scheduleAt).toMinutes() < LOCAL_SCHEDULE_ON_INIT_TIME_THRESHOLDS_MINUTES) {
            requestRecord.beginRequest(scheduleAt);
        }
        requestRecordRepository.save(requestRecord);
        return requestRecord;
    }

    protected <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE internalSend(REQUEST request, RequestRecord requestRecord) {
        try {
            RESPONSE response = internalSend(request);
            requestRecord.endRequest(LocalDateTime.now(), response);
            requestRecordRepository.save(requestRecord);
            return response;
        } catch (Throwable throwable) {
            requestRecord.occuredException(LocalDateTime.now(), throwable);
            requestRecordRepository.save(requestRecord);
            throw throwable;
        }
    }

    protected <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE internalSend(REQUEST request) {
        requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).preRequest(request));
        RESPONSE response = ((RequestHandler<REQUEST, RESPONSE>) requestHandlerMap.get(request.getClass())).exec(request);
        requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).postRequest(request, response));

        return response;
    }
}
