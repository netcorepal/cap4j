package org.netcorepal.cap4j.ddd.domain.event;

import org.netcorepal.cap4j.ddd.domain.event.EventRecord;

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
}
