package org.netcorepal.cap4j.ddd.domain.event;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * todo: 类描述
 *
 * @author binking338
 * @date 2024/8/25
 */
public interface EventSupervisor {

    static EventSupervisor getInstance() {
        return DomainEventSupervisorConfiguration.eventSupervisor;
    }

    /**
     * 通知事件
     *
     * @param eventPayload 事件消息体
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload);

    /**
     * 延迟通知事件
     * @param eventPayload 事件消息体
     * @param delay 延迟时长
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Duration delay);

    /**
     * 定时通知事件
     * @param eventPayload 事件消息体
     * @param schedule 定时时间
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, LocalDateTime schedule);

    /**
     * 通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity);

    /**
     * 延迟通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param delay 延迟时长
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity, Duration delay);

    /**
     * 定时通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param schedule 定时时间
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity, LocalDateTime schedule);
}
