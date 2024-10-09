package org.netcorepal.cap4j.ddd.application;

/**
 * 请求拦截器
 *
 * @author binking338
 * @date 2024/9/1
 */
public interface RequestInterceptor<REQUEST, RESPONSE> {
    /**
     * 请求前
     *
     * @param request
     */
    void preRequest(REQUEST request);

    /**
     * 请求后
     *
     * @param request
     * @param response
     */
    void postRequest(REQUEST request, RESPONSE response);
}
