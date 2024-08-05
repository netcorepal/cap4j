package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 领域事件订阅接口
 *
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
