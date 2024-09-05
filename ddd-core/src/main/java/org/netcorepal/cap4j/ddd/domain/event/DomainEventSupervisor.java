package org.netcorepal.cap4j.ddd.domain.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 领域事件管理器
 *
 * @author binking338
 * @date 2023/8/12
 */
public interface DomainEventSupervisor {
    /**
     * 获取领域事件管理器
     * @return 领域事件管理器
     */
     static DomainEventSupervisor getInstance() {
        return DomainEventSupervisorSupport.instance;
     }

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     */
    <DOMAIN_EVENT> void attach(DOMAIN_EVENT domainEventPayload);

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     * @param delay 延迟发送
     */
    <DOMAIN_EVENT> void attach(DOMAIN_EVENT domainEventPayload, Duration delay);

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     * @param schedule 指定时间发送
     */
    <DOMAIN_EVENT> void attach(DOMAIN_EVENT domainEventPayload, LocalDateTime schedule);

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     * @param entity 绑定实体，该实体对象进入持久化上下文且事务提交时才会触发领域事件分发
     */
    <DOMAIN_EVENT, ENTITY> void attach(DOMAIN_EVENT domainEventPayload, ENTITY entity);

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     * @param entity 绑定实体，该实体对象进入持久化上下文且事务提交时才会触发领域事件分发
     * @param delay 延迟发送
     */
    <DOMAIN_EVENT, ENTITY> void attach(DOMAIN_EVENT domainEventPayload, ENTITY entity, Duration delay);

    /**
     * 附加领域事件
     * @param domainEventPayload 领域事件消息体
     * @param entity 绑定实体，该实体对象进入持久化上下文且事务提交时才会触发领域事件分发
     * @param schedule 指定时间发送
     */
    <DOMAIN_EVENT, ENTITY> void attach(DOMAIN_EVENT domainEventPayload, ENTITY entity, LocalDateTime schedule);

    /**
     * 剥离领域事件
     * @param domainEventPayload 领域事件消息体
     */
    <DOMAIN_EVENT> void detach(DOMAIN_EVENT domainEventPayload);
    /**
     * 剥离领域事件
     * @param domainEventPayload 领域事件消息体
     * @param entity 关联实体
     */
    <DOMAIN_EVENT, ENTITY> void detach(DOMAIN_EVENT domainEventPayload, ENTITY entity);

    /**
     * 发布附加到指定实体以及所有未附加到实体的领域事件
     * @param entities 指定实体集合
     */
    void release(Set<Object> entities);
}
