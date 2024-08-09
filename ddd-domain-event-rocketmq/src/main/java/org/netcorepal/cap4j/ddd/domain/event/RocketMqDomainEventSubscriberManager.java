package org.netcorepal.cap4j.ddd.domain.event;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

import java.util.List;
import java.util.Map;

/**
 * 基于RocketMq的领域事件订阅管理器
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
@Slf4j
public class RocketMqDomainEventSubscriberManager implements DomainEventSubscriberManager {
    private final List<RocketMqDomainEventSubscriber> subscribers;
    private final ApplicationEventPublisher applicationEventPublisher;
    private Map<Class, List<RocketMqDomainEventSubscriber>> subscriberMap = null;

    @Override
    public <Event> void trigger(Event eventPayload) {
        try {
            applicationEventPublisher.publishEvent(eventPayload);
        } catch (Exception e) {
            log.error("领域事件处理失败 eventPayload=" + JSON.toJSONString(eventPayload), e);
            throw new DomainException("领域事件处理失败 eventPayload=" + JSON.toJSONString(eventPayload), e);
        }
        if (subscriberMap == null) {
            synchronized (this) {
                if (subscriberMap == null) {
                    subscriberMap = new java.util.HashMap<Class, List<RocketMqDomainEventSubscriber>>();
                    subscribers.sort((a, b) ->
                            OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE) - OrderUtils.getOrder(b.getClass(), Ordered.LOWEST_PRECEDENCE)
                    );
                    for (RocketMqDomainEventSubscriber subscriber : subscribers) {
                        if (subscriberMap.get(subscriber.forDomainEventClass()) == null) {
                            subscriberMap.put(subscriber.forDomainEventClass(), new java.util.ArrayList<RocketMqDomainEventSubscriber>());
                        }
                        subscriberMap.get(subscriber.forDomainEventClass()).add(subscriber);
                    }
                }
            }
        }
        List<RocketMqDomainEventSubscriber> subscribersForEvent = subscriberMap.get(eventPayload.getClass());
        if (subscribersForEvent == null || subscribersForEvent.isEmpty()) {
            return;
        }
        for (RocketMqDomainEventSubscriber<Event> subscriber : subscribersForEvent) {
            try {
                subscriber.onEvent(eventPayload);
            } catch (Exception e) {
                log.error("领域事件处理失败 eventPayload=" + JSON.toJSONString(eventPayload), e);
                throw new DomainException("领域事件处理失败 eventPayload=" + JSON.toJSONString(eventPayload), e);
            }
        }
    }

    public boolean hasSubscriber(Class eventClass) {
        return subscriberMap.containsKey(eventClass) && subscriberMap.get(eventClass).size() > 0;
    }
}
