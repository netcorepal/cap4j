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

    static IntegrationEventManager getManager(){
        return IntegrationEventSupervisorSupport.manager;
    }


    /**
     * 附加事件
     *
     * @param eventPayload 事件消息体
     */
    default <EVENT> void attach(EVENT eventPayload) {
        attach(eventPayload, LocalDateTime.now());
    }

    /**
     * 附加事件到持久化上下文
     *
     * @param eventPayload 事件消息体
     * @param delay        延迟发送
     */
    default <EVENT> void attach(EVENT eventPayload, Duration delay){
        attach(eventPayload, LocalDateTime.now().plus(delay));
    }

    /**
     * 附加事件到持久化上下文
     *
     * @param eventPayload 事件消息体
     * @param schedule     指定时间发送
     */
    <EVENT> void attach(EVENT eventPayload, LocalDateTime schedule);

    /**
     * 从持久化上下文剥离事件
     *
     * @param eventPayload 事件消息体
     */
    <EVENT> void detach(EVENT eventPayload);
}
