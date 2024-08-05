package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 基于RocketMq的领域事件订阅抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class RocketMqDomainEventSubscriber<Event> implements DomainEventSubscriber<Event> {
    /**
     * 监听的领域事件类型
     * @return
     */
    public abstract Class<Event> forDomainEventClass();

    /**
     * 领域事件消费逻辑
     * @param o
     */
    @Override
    public abstract void onEvent(Event o);
}
