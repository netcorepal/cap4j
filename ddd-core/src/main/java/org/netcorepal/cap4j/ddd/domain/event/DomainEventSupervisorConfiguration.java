package org.netcorepal.cap4j.ddd.domain.event;

import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;

/**
 * 领域事件管理器配置
 *
 * @author binking338
 * @date 2024/8/24
 */
public class DomainEventSupervisorConfiguration {
    static DomainEventSupervisor domainEventSupervisor = null;

    static EventSupervisor eventSupervisor = null;

    /**
     * 配置领域事件管理器
     * @param domainEventSupervisor {@link DomainEventSupervisor}
     */
    public static void configure(DomainEventSupervisor domainEventSupervisor) {
        DomainEventSupervisorConfiguration.domainEventSupervisor = domainEventSupervisor;
    }

    /**
     * 配置事件管理器
     * @param eventSupervisor {@link EventSupervisor}
     */
    public static void configure(EventSupervisor eventSupervisor) {
        DomainEventSupervisorConfiguration.eventSupervisor = eventSupervisor;
    }
}
