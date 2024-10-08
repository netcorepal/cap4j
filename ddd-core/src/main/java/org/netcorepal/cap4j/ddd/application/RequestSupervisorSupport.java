package org.netcorepal.cap4j.ddd.application;

/**
 * 请求管理器配置
 *
 * @author binking338
 * @date 2024/8/24
 */
public class RequestSupervisorSupport {
    static RequestSupervisor instance = null;

    /**
     * 配置请求管理器
     *
     * @param requestSupervisor {@link RequestSupervisor}
     */
    public static void configure(RequestSupervisor requestSupervisor) {
        instance = requestSupervisor;
    }
}
