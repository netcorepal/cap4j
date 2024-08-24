package org.netcorepal.cap4j.ddd.application.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.share.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认请求管理器
 *
 * @author binking338
 * @date 2024/8/24
 */
@RequiredArgsConstructor
public class DefaultRequestSupervisor implements RequestSupervisor {
    private final List<RequestHandler> requestHandlers;

    private Map<Class<?>, RequestHandler> requestHandlerMap = null;

    public void init() {
        if(requestHandlerMap != null){
            return;
        }
        synchronized (DefaultRequestSupervisor.class){
            if(requestHandlerMap != null){
                return;
            }
        }
        requestHandlerMap = new HashMap<>();
        for (RequestHandler requestHandler : requestHandlers) {
            Class requestPayloadClass = ClassUtils.findMethod(
                    requestHandler.getClass(),
                    "exec",
                    m -> m.getParameterCount() == 1
            ).getParameters()[0].getType();
            requestHandlerMap.put(requestPayloadClass, requestHandler);
        }
    }
    @Override
    public <PARAM> Object request(PARAM param){
        return request(param, (Class<PARAM>)param.getClass(), Object.class);
    }
    @Override
    public <PARAM, RESULT> RESULT request(PARAM param, Class<RESULT> resultClass) {
        return request(param, (Class<PARAM>)param.getClass(), resultClass);
    }

    @Override
    public <PARAM, RESULT> RESULT request(PARAM param, Class<PARAM> paramClass, Class<RESULT> resultClass) {
        init();
        return (RESULT) requestHandlerMap.get(paramClass).exec(param);
    }
}
