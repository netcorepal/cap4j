package org.netcorepal.cap4j.ddd.domain.event;

import org.springframework.messaging.Message;

/**
 * 领域事件发布接口
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     * @param message
     * @param event
     */
    void publish(Message message, EventRecord event);
}
