package org.netcorepal.cap4j.ddd.domain.event;

import org.netcorepal.cap4j.ddd.share.ClassUtils;
import org.springframework.core.ResolvableType;

/**
 * 基于RocketMq的领域事件订阅抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class RocketMqDomainEventSubscriber<Event> implements DomainEventSubscriber<Event> {
    /**
     * 监听的领域事件类型
     *
     * @return
     */
    public Class<Event> forDomainEventClass() {
        return (Class<Event>) ClassUtils.findMethod(
                this.getClass(),
                "onEvent",
                m -> m.getParameterCount() == 1
        ).getParameters()[0].getType();
    }

    /**
     * 领域事件消费逻辑
     *
     * @param event
     */
    @Override
    public abstract void onEvent(Event event);
}
