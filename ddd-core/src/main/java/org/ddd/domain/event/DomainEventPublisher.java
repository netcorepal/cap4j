package org.ddd.domain.event;

/**
 * @author binking338
 * @date 2023/8/5
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     * @param eventPayload
     */
    void publish(Object eventPayload);
}
