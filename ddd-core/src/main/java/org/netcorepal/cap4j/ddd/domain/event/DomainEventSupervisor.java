package org.netcorepal.cap4j.ddd.domain.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
     * 剥离事件
     * @param eventPayload
     */
    void detach(Object eventPayload);
    /**
     * 重置事件
     */
    void reset();

    /**
     * 获取事件列表
     * @return
     */
    List<Object> getEvents();

    /**
     * 获取发送事件
     * @param eventPlayload
     * @return
     */
    LocalDateTime getDeliverTime(Object eventPlayload);
}
