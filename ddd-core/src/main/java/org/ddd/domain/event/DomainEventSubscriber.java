package org.ddd.domain.event;

/**
 * @author binking338
 * @date 2023/8/5
 */
public interface DomainEventSubscriber<Event> {
    /**
     * 领域事件消费逻辑
     *
     * @param event
     */
    void onEvent(Event event);
}
