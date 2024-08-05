package org.netcorepal.cap4j.ddd.domain.event;

/**
 * @author binking338
 * @date 2023/8/13
 */
public abstract class RocketMqDomainEventSubscriber<Event> implements DomainEventSubscriber<Event> {
    public abstract Class<Event> forDomainEventClass();

    @Override
    public abstract void onEvent(Event o);
}
