package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 领域事件订阅管理器接口
 *
 * @author binking338
 * @date 2023/8/13
 */
public interface DomainEventSubscriberManager {
    <Event> void trigger(Event eventPayload);

    boolean hasSubscriber(Class eventClass);
}
