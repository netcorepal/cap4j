package org.netcorepal.cap4j.ddd.domain.event;

import java.time.LocalDateTime;

/**
 * 事件发布接口
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface EventPublisher {

    /**
     * 发布事件
     *
     * @param event
     */
    void publish(EventRecord event);

    /**
     * 重试事件
     *
     * @param event
     * @param minNextTryTime
     */
    void retry(EventRecord event, LocalDateTime minNextTryTime);
}
