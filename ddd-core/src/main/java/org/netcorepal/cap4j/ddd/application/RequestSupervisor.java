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
    <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request);
}
