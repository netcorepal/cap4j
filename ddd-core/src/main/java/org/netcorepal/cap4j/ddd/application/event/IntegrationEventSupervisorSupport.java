package org.netcorepal.cap4j.ddd.application.event;

/**
 * 事件管理器配置
 *
 * @author binking338
 * @date 2024/8/26
 */
public class IntegrationEventSupervisorSupport {
    static IntegrationEventSupervisor instance = null;

    static IntegrationEventManager manager = null;


    /**
     * 配置事件管理器
     *
     * @param integrationEventSupervisor {@link IntegrationEventSupervisor}
     */
    public static void configure(IntegrationEventSupervisor integrationEventSupervisor) {
        IntegrationEventSupervisorSupport.instance = integrationEventSupervisor;
    }
    /**
     * 配置事件管理器
     *
     * @param integrationEventManager {@link IntegrationEventManager}
     */
    public static void configure(IntegrationEventManager integrationEventManager) {
        IntegrationEventSupervisorSupport.manager = integrationEventManager;
    }
}
