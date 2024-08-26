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
        return DomainEventSupervisorConfiguration.domainEventSupervisor;
     }

    /**
     * 附加事件
     * @param eventPayload 事件对象
     */
    void attach(Object eventPayload);

    /**
     * 附加事件
     * @param eventPayload 事件对象
     * @param delay 延迟发送
     */
    void attach(Object eventPayload, Duration delay);

    /**
     * 附加事件
     * @param eventPayload 事件对象
     * @param schedule 指定时间发送
     */
    void attach(Object eventPayload, LocalDateTime schedule);

    /**
     * 附加事件
     * @param eventPayload 事件对象
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     */
    void attach(Object eventPayload, Object entity);

    /**
     * 附加事件
     * @param eventPayload 事件对象
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     * @param delay 延迟发送
     */
    void attach(Object eventPayload, Object entity, Duration delay);

    /**
     * 附加事件
     * @param eventPayload 事件对象
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     * @param schedule 指定时间发送
     */
    void attach(Object eventPayload, Object entity, LocalDateTime schedule);

    /**
     * 剥离事件
     * @param eventPayload 事件对象
     */
    void detach(Object eventPayload);
    /**
     * 剥离事件
     * @param eventPayload 事件对象
     * @param entity 关联实体
     */
    void detach(Object eventPayload, Object entity);
    /**
     * 重置事件
     */
    void reset();

    /**
     * 弹出事件列表
     * @return 事件列表
     */
    Set<Object> popEvents();

    /**
     * 弹出实体绑定的事件列表
     * @param entity 关联实体
     * @return 事件列表
     */
    public Set<Object> popEvents(Object entity);

    /**
     * 获取发送事件
     * @param eventPayload 事件对象
     * @return 发送时间
     */
    LocalDateTime getDeliverTime(Object eventPayload);
}
