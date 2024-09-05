package org.netcorepal.cap4j.ddd.application;

/**
 * 请求管理器
 *
 * @author binking338
 * @date 2024/8/24
 */
public interface RequestSupervisor {

    /**
     * 获取请求管理器
     *
     * @return 请求管理器
     */
    static RequestSupervisor getInstance() {
        return RequestSupervisorSupport.instance;
    }

    /**
     * 执行请求
     *
     * @param request   请求参数
     * @param <REQUEST> 请求参数类型
     */
    <REQUEST> Object send(REQUEST request);

    /**
     * 执行请求
     *
     * @param request     请求参数
     * @param resultClass 返回结果类型
     * @param <REQUEST>   请求参数类型
     * @param <RESPONSE>  返回结果类型
     */
    <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<RESPONSE> resultClass);

    /**
     * 执行请求
     *
     * @param request     请求参数
     * @param paramClass  请求参数类型
     * @param resultClass 返回结果类型
     * @param <REQUEST>   请求参数类型
     * @param <RESPONSE>  返回结果类型
     * @return 请求结果
     */
    <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<REQUEST> paramClass, Class<RESPONSE> resultClass);
}
