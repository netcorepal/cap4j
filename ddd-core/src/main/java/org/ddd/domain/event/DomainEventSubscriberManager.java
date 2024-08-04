package org.ddd.domain.event;

/**
 * @author binking338
 * @date 2023/8/13
 */
public interface DomainEventSubscriberManager {
    <Event> void trigger(Event eventPayload);

    boolean hasSubscriber(Class eventClass);
}
