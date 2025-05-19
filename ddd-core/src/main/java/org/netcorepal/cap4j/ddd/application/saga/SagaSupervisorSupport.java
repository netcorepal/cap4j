package org.netcorepal.cap4j.ddd.application.saga;

/**
 * Saga 管理器配置
 *
 * @author binking338
 * @date 2024/10/12
 */
public class SagaSupervisorSupport {
    static SagaSupervisor instance;
    static SagaProcessSupervisor sagaProcessSupervisor;
    static SagaManager sagaManager;

    /**
     * 配置 Saga 管理器
     *
     * @param sagaSupervisor
     */
    public static void configure(SagaSupervisor sagaSupervisor)
    {
        instance = sagaSupervisor;
    }

    /**
     * 配置 Saga 子执行器
     *
     * @param sagaProcessSupervisor
     */
    public static void configure(SagaProcessSupervisor sagaProcessSupervisor) {
        SagaSupervisorSupport.sagaProcessSupervisor = sagaProcessSupervisor;
    }

    /**
     * 配置 Saga 管理器
     *
     * @param sagaManager
     */
    public static void configure(SagaManager sagaManager) {
        SagaSupervisorSupport.sagaManager = sagaManager;
    }
}
