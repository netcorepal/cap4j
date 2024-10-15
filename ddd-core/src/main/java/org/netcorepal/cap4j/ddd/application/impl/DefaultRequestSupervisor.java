package org.netcorepal.cap4j.ddd.application.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestInterceptor;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.command.NoneResultCommandParam;
import org.netcorepal.cap4j.ddd.application.query.*;
import org.netcorepal.cap4j.ddd.application.saga.SagaHandler;
import org.netcorepal.cap4j.ddd.application.saga.SagaParam;
import org.netcorepal.cap4j.ddd.application.saga.SagaSupervisor;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import java.util.*;

/**
 * 默认请求管理器
 *
 * @author binking338
 * @date 2024/8/24
 */
@RequiredArgsConstructor
public class DefaultRequestSupervisor implements RequestSupervisor {
    private final List<RequestHandler<?, ?>> requestHandlers;
    private final List<RequestInterceptor<?, ?>> requestInterceptors;

    private Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = null;
    private Map<Class<?>, List<RequestInterceptor<?, ?>>> requestInterceptorMap = null;

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
                    requestHandler.getClass(), 0,
                    RequestHandler.class,
                    Command.class, NoneResultCommandParam.class,
                    Query.class, ListQuery.class, PageQuery.class);
            requestHandlerMap.put(requestPayloadClass, requestHandler);
        }
        for (RequestInterceptor<?, ?> requestInterceptor : requestInterceptors) {
            Class<?> requestPayloadClass = ClassUtils.resolveGenericTypeClass(
                    requestInterceptor.getClass(), 0,
                    RequestInterceptor.class);
            List<RequestInterceptor<?, ?>> interceptors = requestInterceptorMap.computeIfAbsent(requestPayloadClass, cls -> new ArrayList<>());
            interceptors.add(requestInterceptor);
        }
    }

    @Override
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request) {
        if(request instanceof SagaParam){
            return SagaSupervisor.getInstance().send((SagaParam<RESPONSE>) request);
        }
        init();
        requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).preRequest(request));
        RESPONSE response = ((RequestHandler<REQUEST, RESPONSE>) requestHandlerMap.get(request.getClass())).exec(request);
        requestInterceptorMap.getOrDefault(request.getClass(), Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).postRequest(request, response));
        return response;
    }
}
