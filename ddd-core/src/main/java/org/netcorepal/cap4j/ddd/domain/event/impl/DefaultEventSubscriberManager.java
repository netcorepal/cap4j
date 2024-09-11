package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriber;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoNotify;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoRequest;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 基于RocketMq的领域事件订阅管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
public class DefaultEventSubscriberManager implements EventSubscriberManager {
    private final List<EventSubscriber<?>> subscribers;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String scanPath;
    private Map<Class<?>, List<EventSubscriber<?>>> subscriberMap = null;

    public void init() {
        if (null != subscriberMap) {
            return;
        }
        synchronized (this) {
            if (null != subscriberMap) {
                return;
            }
            subscriberMap = new java.util.HashMap<>();
            subscribers.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE))
            );
            for (EventSubscriber<?> subscriber : subscribers) {
                Class<?> eventClass = ClassUtils.resolveGenericTypeClass(
                        subscriber.getClass(), 0,
                        AbstractEventSubscriber.class, EventSubscriber.class);
                subscribe(eventClass, subscriber);
            }
            // 领域事件
            ScanUtils.findDomainEventClasses(scanPath).forEach(domainEventClass -> {
                // 自动实现 Spring EventListener 适配
                subscribe(domainEventClass, applicationEventPublisher::publishEvent);

                // 自动实现 Event -> Request
                // 领域事件转Request容易导致数据库长时事务，慎用！
                if( null != domainEventClass.getAnnotation(AutoRequest.class)){
                    AutoRequest autoRequest = domainEventClass.getAnnotation(AutoRequest.class);
                    Class<?> converterClass = null;
                    if (Converter.class.isAssignableFrom(autoRequest.converterClass())) {
                        converterClass = autoRequest.converterClass();
                    }
                    Converter<Object, Object> converter = ClassUtils.newConverterInstance(domainEventClass, autoRequest.targetRequestClass(), converterClass);
                    subscribe(domainEventClass, domainEvent -> RequestSupervisor.getInstance().send((RequestParam)converter.convert(domainEvent)));
                }
            });
            // 集成事件
            ScanUtils.findIntegrationEventClasses(scanPath).forEach(integrationEventClass -> {
                subscribe(integrationEventClass, applicationEventPublisher::publishEvent);

                // 自动实现 DomainEvent -> IntegrationEvent 适配
                if (null != integrationEventClass.getAnnotation(AutoNotify.class)) {
                    AutoNotify autoNotify = integrationEventClass.getAnnotation(AutoNotify.class);
                    Class<?> converterClass = null;
                    if (Converter.class.isAssignableFrom(integrationEventClass)) {
                        converterClass = integrationEventClass;
                    }
                    if (Converter.class.isAssignableFrom(autoNotify.converterClass())) {
                        converterClass = autoNotify.converterClass();
                    }
                    Converter<Object, Object> converter = ClassUtils.newConverterInstance(autoNotify.sourceDomainEventClass(), integrationEventClass, converterClass);
                    subscribe(autoNotify.sourceDomainEventClass(), domainEvent -> IntegrationEventSupervisor.getInstance().notify(converter.convert(domainEvent), Duration.ofSeconds(autoNotify.delayInSeconds())));
                }

                // 自动实现 Event -> Request 转发
                if (null != integrationEventClass.getAnnotation(AutoRequest.class)) {
                    AutoRequest autoRequest = integrationEventClass.getAnnotation(AutoRequest.class);
                    Class<?> converterClass = null;
                    if (Converter.class.isAssignableFrom(autoRequest.converterClass())) {
                        converterClass = autoRequest.converterClass();
                    }
                    Converter<Object, Object> converter = ClassUtils.newConverterInstance(integrationEventClass, autoRequest.targetRequestClass(), converterClass);
                    subscribe(integrationEventClass, integrationEvent -> RequestSupervisor.getInstance().send((RequestParam<?>)converter.convert(integrationEvent)));
                }
            });
        }
    }


    @Override
    public boolean subscribe(Class<?> eventPayloadClass, EventSubscriber<?> subscriber) {
        List<EventSubscriber<?>> subscribers =
                subscriberMap.computeIfAbsent(eventPayloadClass, k -> new java.util.ArrayList<EventSubscriber<?>>());
        return subscribers.add(subscriber);
    }

    @Override
    public boolean unsubscribe(Class<?> eventPayloadClass, EventSubscriber<?> subscriber) {
        List<EventSubscriber<?>> subscribers = subscriberMap.get(eventPayloadClass);
        if (subscribers == null) {
            return false;
        }
        return subscribers.remove(subscriber);
    }


    @Override
    public void dispatch(Object eventPayload) {
        init();
        List<EventSubscriber<?>> subscribersForEvent = subscriberMap.get(eventPayload.getClass());
        if (subscribersForEvent == null || subscribersForEvent.isEmpty()) {
            return;
        }
        for (EventSubscriber<?> subscriber : subscribersForEvent) {
            ((EventSubscriber<Object>) subscriber).onEvent(eventPayload);
        }
    }
}
