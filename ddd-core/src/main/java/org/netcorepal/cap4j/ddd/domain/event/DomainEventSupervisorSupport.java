package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 领域事件管理器配置
 *
 * @author binking338
 * @date 2024/8/24
 */
public class DomainEventSupervisorSupport {
    static DomainEventSupervisor instance = null;

    /**
     * 配置领域事件管理器
     * @param domainEventSupervisor {@link DomainEventSupervisor}
     */
    public static void configure(DomainEventSupervisor domainEventSupervisor) {
        DomainEventSupervisorSupport.instance = domainEventSupervisor;
    }

    /**
     * for entity import static
     *
     * @return
     */
    public static DomainEventSupervisor events(){
        return instance;
    }
}
