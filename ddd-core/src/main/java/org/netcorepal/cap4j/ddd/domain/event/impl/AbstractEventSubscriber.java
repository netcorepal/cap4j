package org.netcorepal.cap4j.ddd.domain.event.impl;

import org.netcorepal.cap4j.ddd.domain.event.EventSubscriber;

/**
 * 基于RocketMq的领域事件订阅抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class AbstractEventSubscriber<Event> implements EventSubscriber<Event> {

    /**
     * 领域事件消费逻辑
     *
     * @param event
     */
    @Override
    public abstract void onEvent(Event event);
}
