package org.netcorepal.cap4j.ddd.domain.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 领域事件管理器
 *
 * @author binking338
 * @date 2023/8/12
 */
public interface DomainEventSupervisor {
    /**
     * 附加事件
     * @param eventPayload
     */
    void attach(Object eventPayload);

    /**
     * 附加事件
     * @param eventPayload
     * @param delay 延迟发送
     */
    void attach(Object eventPayload, Duration delay);

    /**
     * 附加事件
     * @param eventPayload
     * @param schedule 指定时间发送
     */
    void attach(Object eventPayload, LocalDateTime schedule);

    /**
     * 附加事件
     * @param eventPayload
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     */
    void attach(Object eventPayload, Object entity);

    /**
     * 附加事件
     * @param eventPayload
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     * @param delay 延迟发送
     */
    void attach(Object eventPayload, Object entity, Duration delay);

    /**
     * 附加事件
     * @param eventPayload
     * @param entity 绑定实体，该实体对象进入持久化上下文才会触发事件分发
     * @param schedule 指定时间发送
     */
    void attach(Object eventPayload, Object entity, LocalDateTime schedule);

    /**
     * 剥离事件
     * @param eventPayload
     */
    void detach(Object eventPayload);
    /**
     * 剥离事件
     * @param eventPayload
     * @param entity
     */
    void detach(Object eventPayload, Object entity);
    /**
     * 重置事件
     */
    void reset();

    /**
     * 获取事件列表
     * @return
     */
    Set<Object> getEvents();

    /**
     * 获取实体绑定的事件列表
     * @param entity
     * @return
     */
    public Set<Object> getEvents(Object entity);

    /**
     * 获取发送事件
     * @param eventPayload
     * @return
     */
    LocalDateTime getDeliverTime(Object eventPayload);
}
