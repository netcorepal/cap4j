package org.netcorepal.cap4j.ddd.application.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestInterceptor;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.command.CommandNoneParam;
import org.netcorepal.cap4j.ddd.application.command.CommandNoneParamAndResult;
import org.netcorepal.cap4j.ddd.application.command.CommandNoneResult;
import org.netcorepal.cap4j.ddd.application.query.*;
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
                    Command.class, CommandNoneParam.class, CommandNoneResult.class, CommandNoneParamAndResult.class,
                    Query.class, QueryNoArgs.class, ListQuery.class, ListQueryNoArgs.class, PageQuery.class);
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
    public <REQUEST> Object send(REQUEST request) {
        return send(request, (Class<REQUEST>) request.getClass(), Object.class);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<RESPONSE> responseClass) {
        return send(request, (Class<REQUEST>) request.getClass(), responseClass);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<REQUEST> requestClass, Class<RESPONSE> responseClass) {
        init();
        requestInterceptorMap.getOrDefault(requestClass, Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).preRequest(request));
        RESPONSE response = ((RequestHandler<REQUEST, RESPONSE>) requestHandlerMap.get(requestClass)).exec(request);
        requestInterceptorMap.getOrDefault(requestClass, Collections.emptyList())
                .forEach(interceptor -> ((RequestInterceptor<REQUEST, RESPONSE>) interceptor).postRequest(request, response));
        return response;
    }
}
