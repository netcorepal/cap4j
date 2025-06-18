package org.netcorepal.cap4j.ddd.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptorManager;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 事件拦截器管理器实现
 *
 * @author binking338
 * @date 2024/9/12
 */
@RequiredArgsConstructor
public class DefaultEventInterceptorManager implements EventMessageInterceptorManager, DomainEventInterceptorManager, IntegrationEventInterceptorManager, IntegrationEventPublisher.PublishCallback {
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final List<EventInterceptor> eventInterceptors;
    private final EventRecordRepository eventRecordRepository;

    private Set<EventMessageInterceptor> orderedEventMessageInterceptors;
    private Set<EventInterceptor> orderedEventInterceptors4DomainEvent;
    private Set<EventInterceptor> orderedEventInterceptors4IntegrationEvent;
    private Set<DomainEventInterceptor> orderedDomainEventInterceptors;
    private Set<IntegrationEventInterceptor> orderedIntegrationEventInterceptors;

    @Override
    public Set<DomainEventInterceptor> getOrderedDomainEventInterceptors() {
        if(orderedDomainEventInterceptors == null){
            orderedDomainEventInterceptors = eventInterceptors.stream()
                    .filter(i -> DomainEventInterceptor.class.isAssignableFrom(i.getClass()))
                    .map(i -> (DomainEventInterceptor) i)
                    .sorted(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return orderedDomainEventInterceptors;
    }

    @Override
    public Set<EventInterceptor> getOrderedEventInterceptors4DomainEvent() {
        if(orderedEventInterceptors4DomainEvent == null){
            orderedEventInterceptors4DomainEvent = eventInterceptors.stream()
                    .filter(i -> DomainEventInterceptor.class.isAssignableFrom(i.getClass())
                            || !IntegrationEventInterceptor.class.isAssignableFrom(i.getClass()))
                    .sorted(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return orderedEventInterceptors4DomainEvent;
    }

    @Override
    public Set<EventMessageInterceptor> getOrderedEventMessageInterceptors() {
        if(orderedEventMessageInterceptors == null){
            orderedEventMessageInterceptors = eventMessageInterceptors.stream()
                    .sorted(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return orderedEventMessageInterceptors;
    }

    @Override
    public Set<IntegrationEventInterceptor> getOrderedIntegrationEventInterceptors() {
        if(orderedIntegrationEventInterceptors == null){
            orderedIntegrationEventInterceptors = eventInterceptors.stream()
                    .filter(i -> IntegrationEventInterceptor.class.isAssignableFrom(i.getClass()))
                    .map(i -> (IntegrationEventInterceptor) i)
                    .sorted(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return orderedIntegrationEventInterceptors;
    }

    @Override
    public Set<EventInterceptor> getOrderedEventInterceptors4IntegrationEvent() {
        if(orderedEventInterceptors4IntegrationEvent == null){
            orderedEventInterceptors4IntegrationEvent = eventInterceptors.stream()
                    .filter(i -> !DomainEventInterceptor.class.isAssignableFrom(i.getClass())
                            || IntegrationEventInterceptor.class.isAssignableFrom(i.getClass()))
                    .sorted(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return orderedEventInterceptors4IntegrationEvent;
    }

    @Override
    public void onSuccess(EventRecord event) {
        LocalDateTime now = LocalDateTime.now();
        // 修改事件消费状态
        event.confirmedDelivery(now);

        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.prePersist(event));
        eventRecordRepository.save(event);
        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.postPersist(event));

        getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postPublish(event.getMessage()));
        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.postRelease(event));
    }

    @Override
    public void onException(EventRecord event, Throwable throwable) {
        LocalDateTime now = LocalDateTime.now();
        // 修改事件异常状态
        event.occuredException(now, throwable);

        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.prePersist(event));
        eventRecordRepository.save(event);
        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.postPersist(event));

        getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.onException(throwable, event));
    }
}
