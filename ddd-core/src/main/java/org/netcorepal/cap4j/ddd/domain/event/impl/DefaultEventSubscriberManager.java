package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoRelease;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoReleases;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoRequest;
import org.netcorepal.cap4j.ddd.application.event.annotation.AutoRequests;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriber;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;
import java.util.Arrays;
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
                List<AutoRequest> autoRequests = null;
                if (null != domainEventClass.getAnnotation(AutoRequest.class)) {
                    autoRequests = Arrays.asList(domainEventClass.getAnnotation(AutoRequest.class));
                }
                if (null != domainEventClass.getAnnotation(AutoRequests.class)) {
                    autoRequests = Arrays.asList(domainEventClass.getAnnotation(AutoRequests.class).value());
                }
                if (null == autoRequests) {
                    return;
                }
                for (AutoRequest autoRequest : autoRequests) {
                    Class<?> converterClass = null;
                    if (Converter.class.isAssignableFrom(autoRequest.converterClass())) {
                        converterClass = autoRequest.converterClass();
                    }
                    Converter<Object, Object> converter = ClassUtils.newConverterInstance(domainEventClass, autoRequest.targetRequestClass(), converterClass);
                    subscribe(domainEventClass, domainEvent -> RequestSupervisor.getInstance().send((RequestParam) converter.convert(domainEvent)));
                }
            });
            // 集成事件
            ScanUtils.findIntegrationEventClasses(scanPath).forEach(integrationEventClass -> {
                subscribe(integrationEventClass, applicationEventPublisher::publishEvent);

                // 自动实现 DomainEvent -> IntegrationEvent 适配
                List<AutoRelease> autoReleases = null;
                if (null != integrationEventClass.getAnnotation(AutoRelease.class)) {
                    autoReleases = Arrays.asList(integrationEventClass.getAnnotation(AutoRelease.class));
                }
                if (null != integrationEventClass.getAnnotation(AutoReleases.class)) {
                    autoReleases = Arrays.asList(integrationEventClass.getAnnotation(AutoReleases.class).value());
                }
                if (null != autoReleases) {
                    for (AutoRelease autoRelease : autoReleases) {
                        Class<?> converterClass = null;
                        if (Converter.class.isAssignableFrom(integrationEventClass)) {
                            converterClass = integrationEventClass;
                        }
                        if (Converter.class.isAssignableFrom(autoRelease.converterClass())) {
                            converterClass = autoRelease.converterClass();
                        }
                        Converter<Object, Object> converter = ClassUtils.newConverterInstance(autoRelease.sourceDomainEventClass(), integrationEventClass, converterClass);
                        subscribe(autoRelease.sourceDomainEventClass(), domainEvent -> {
                            IntegrationEventSupervisor.getInstance().attach(converter.convert(domainEvent), Duration.ofSeconds(autoRelease.delayInSeconds()));
                            IntegrationEventSupervisor.getManager().release();
                        });
                    }
                }

                // 自动实现 Event -> Request 转发
                List<AutoRequest> autoRequests = null;
                if (null != integrationEventClass.getAnnotation(AutoRequest.class)) {
                    autoRequests = Arrays.asList(integrationEventClass.getAnnotation(AutoRequest.class));
                }
                if (null != integrationEventClass.getAnnotation(AutoRequests.class)) {
                    autoRequests = Arrays.asList(integrationEventClass.getAnnotation(AutoRequests.class).value());
                }
                if (null != autoRequests) {
                    for (AutoRequest autoRequest : autoRequests) {
                        Class<?> converterClass = null;
                        if (Converter.class.isAssignableFrom(autoRequest.converterClass())) {
                            converterClass = autoRequest.converterClass();
                        }
                        Converter<Object, Object> converter = ClassUtils.newConverterInstance(integrationEventClass, autoRequest.targetRequestClass(), converterClass);
                        subscribe(integrationEventClass, integrationEvent -> RequestSupervisor.getInstance().send((RequestParam<?>) converter.convert(integrationEvent)));
                    }
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
