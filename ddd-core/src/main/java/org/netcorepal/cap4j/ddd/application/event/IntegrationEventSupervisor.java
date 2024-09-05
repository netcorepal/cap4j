package org.netcorepal.cap4j.ddd.application.event;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 集成事件管理器
 *
 * @author binking338
 * @date 2024/8/25
 */
public interface IntegrationEventSupervisor {

    static IntegrationEventSupervisor getInstance() {
        return IntegrationEventSupervisorSupport.instance;
    }

    /**
     * 通知集成事件
     *
     * @param integrationEventPayload 集成事件消息体
     * @param <INTEGRATION_EVENT>     事件消息类型
     */
    <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload);

    /**
     * 延迟通知集成事件
     *
     * @param integrationEventPayload 集成事件消息体
     * @param delay                   延迟时长
     * @param <INTEGRATION_EVENT>     事件消息类型
     */
    <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload, Duration delay);

    /**
     * 定时通知集成事件
     *
     * @param integrationEventPayload 集成事件消息体
     * @param schedule                定时时间
     * @param <INTEGRATION_EVENT>     事件消息类型
     */
    <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload, LocalDateTime schedule);
}
