package org.netcorepal.cap4j.ddd.application.saga;

/**
 * Saga控制器
 *
 * @author binking338
 * @date 2024/10/12
 */
public interface SagaSupervisor {

    /**
     * 获取请求管理器
     *
     * @return 请求管理器
     */
    static SagaSupervisor getInstance() {
        return SagaSupervisorSupport.instance;
    }

    /**
     * 执行Saga流程
     *
     * @param request   请求参数
     * @param <REQUEST> 请求参数类型
     */
    <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request);

    /**
     * 异步执行Saga流程
     *
     * @param request
     * @param <REQUEST>
     * @return Saga ID
     */
    <REQUEST extends SagaParam<?>> SagaRecord sendAsync(REQUEST request);

    /**
     * 根据ID获取Saga记录
     *
     * @param id
     * @return
     */
    SagaRecord getById(String id);

    /**
     * 重新执行Saga流程
     *
     * @param saga
     * @return
     */
    Object resume(SagaRecord saga);
}
