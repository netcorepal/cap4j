package org.netcorepal.cap4j.ddd.application.saga;

import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * Saga子环节执行器
 *
 * @author binking338
 * @date 2024/10/14
 */
public interface SagaProcessSupervisor {
    /**
     * 获取Saga子环节执行管理器
     *
     * @return
     */
    static SagaProcessSupervisor getInstance() {
        return SagaSupervisorSupport.sagaProcessSupervisor;
    }

    /**
     * 执行Saga子环节
     *
     * @param processCode Saga子环节标识
     * @param request     请求参数
     * @param <REQUEST>   请求参数类型
     * @param <RESPONSE>  响应参数类型
     * @return
     */
    <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE sendProcess(String processCode, REQUEST request);
}
