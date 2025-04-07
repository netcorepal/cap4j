package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 默认领域事件管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
public class DefaultDomainEventSupervisor implements DomainEventSupervisor, DomainEventManager {
    private final EventRecordRepository eventRecordRepository;
    private final DomainEventInterceptorManager domainEventInterceptorManager;
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String svcName;

    private static final ThreadLocal<Map<Object, Set<Object>>> TL_ENTITY_EVENT_PAYLOADS = new ThreadLocal<Map<Object, Set<Object>>>();
    private static final ThreadLocal<Map<Object, LocalDateTime>> TL_EVENT_SCHEDULE_MAP = new ThreadLocal<Map<Object, LocalDateTime>>();
    private static final Set<Object> EMPTY_EVENT_PAYLOADS = Collections.emptySet();
    /**
     * 默认事件过期时间（分钟）
     */
    private static final int DEFAULT_EVENT_EXPIRE_MINUTES = 30;
    /**
     * 默认事件重试次数
     */
    private static final int DEFAULT_EVENT_RETRY_TIMES = 16;

    private Object unwrapEntity(Object entity) {
        return entity instanceof Aggregate
                ? ((Aggregate) entity)._unwrap()
                : entity;
    }

    @Override
    public void attach(Object eventPayload, Object entity, LocalDateTime schedule) {
        // 判断领域事件，不支持集成事件。
        if (eventPayload == null) {
            throw new DomainException("事件负载不能为空");
        }
        if (eventPayload.getClass().isAnnotationPresent(IntegrationEvent.class)) {
            throw new DomainException("事件类型不能为集成事件");
        }
        entity = unwrapEntity(entity);
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if (entityEventPayloads == null) {
            entityEventPayloads = new HashMap<>();
            TL_ENTITY_EVENT_PAYLOADS.set(entityEventPayloads);
        }
        if (!entityEventPayloads.containsKey(entity)) {
            entityEventPayloads.put(entity, new HashSet<>());
        }
        entityEventPayloads.get(entity).add(eventPayload);

        putDeliverTime(eventPayload, schedule);
        Object finalEntity = entity;
        domainEventInterceptorManager.getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onAttach(eventPayload, finalEntity, schedule));
    }

    @Override
    public void detach(Object eventPayload, Object entity) {
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if (entityEventPayloads == null) {
            return;
        }
        entity = unwrapEntity(entity);
        Set<Object> eventPayloads = entityEventPayloads.containsKey(entity) ? entityEventPayloads.get(entity) : null;
        if (eventPayloads == null) {
            return;
        }

        eventPayloads.remove(eventPayload);
        Object finalEntity = entity;
        domainEventInterceptorManager.getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onDetach(eventPayload, finalEntity));
    }

    @Override
    public void release(Set<Object> entities) {
        Set<Object> eventPayloads = new HashSet<>();
        if (null != entities && !entities.isEmpty()) {
            for (Object entity : entities) {
                eventPayloads.addAll(this.popEvents(entity));
                if (entity instanceof AbstractAggregateRoot) {
                    Method domainEventsMethod = ClassUtils.findMethod(AbstractAggregateRoot.class, "domainEvents", m -> m.getParameterCount() == 0);
                    if (domainEventsMethod == null) {
                        continue;
                    }
                    domainEventsMethod.setAccessible(true);
                    try {
                        Object domainEvents = domainEventsMethod.invoke(entity);
                        if (domainEvents != null && Collection.class.isAssignableFrom(domainEvents.getClass())) {
                            eventPayloads.addAll((Collection<Object>) domainEvents);
                        }
                    } catch (Throwable throwable) {
                        /* don't care */
                        continue;
                    }

                    Method clearDomainEventsMethod = ClassUtils.findMethod(AbstractAggregateRoot.class, "clearDomainEvents", m -> m.getParameterCount() == 0);
                    try {
                        clearDomainEventsMethod.invoke(entity);
                    } catch (Throwable throwable) {
                        /* don't care */
                        continue;
                    }
                }
            }
        }
        List<EventRecord> persistedEvents = new ArrayList<>(eventPayloads.size());
        List<EventRecord> transientEvents = new ArrayList<>(eventPayloads.size());
        LocalDateTime now = LocalDateTime.now();
        for (Object eventPayload : eventPayloads) {
            LocalDateTime deliverTime = this.getDeliverTime(eventPayload);
            EventRecord event = eventRecordRepository.create();
            event.init(eventPayload, this.svcName, deliverTime, Duration.ofMinutes(DEFAULT_EVENT_EXPIRE_MINUTES), DEFAULT_EVENT_RETRY_TIMES);
            boolean isDelayDeliver = deliverTime.isAfter(now);
            if (!isDomainEventNeedPersist(eventPayload) && !isDelayDeliver) {
                event.markPersist(false);
                transientEvents.add(event);
            } else {
                event.markPersist(true);
                domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.prePersist(event));
                eventRecordRepository.save(event);
                domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.postPersist(event));
                persistedEvents.add(event);
            }
        }
        DomainEventAttachedTransactionCommittingEvent domainEventAttachedTransactionCommittingEvent = new DomainEventAttachedTransactionCommittingEvent(this, transientEvents);
        DomainEventAttachedTransactionCommittedEvent domainEventAttachedTransactionCommittedEvent = new DomainEventAttachedTransactionCommittedEvent(this, persistedEvents);
        onTransactionCommiting(domainEventAttachedTransactionCommittingEvent);
        applicationEventPublisher.publishEvent(domainEventAttachedTransactionCommittingEvent);
        applicationEventPublisher.publishEvent(domainEventAttachedTransactionCommittedEvent);
    }

    /**
     * 判断事件是否需要持久化
     * - 延迟或定时领域事件视情况进行持久化
     * - 显式指定persist=true的领域事件必须持久化
     *
     * @param payload
     * @return
     */
    protected boolean isDomainEventNeedPersist(Object payload) {
        DomainEvent domainEvent = payload == null
                ? null
                : payload.getClass().getAnnotation(DomainEvent.class);
        if (domainEvent != null) {
            return domainEvent.persist();
        } else {
            return false;
        }
    }

    protected void onTransactionCommiting(DomainEventAttachedTransactionCommittingEvent domainEventAttachedTransactionCommittingEvent) {
        List<EventRecord> events = domainEventAttachedTransactionCommittingEvent.getEvents();
        publish(events);
    }

    @TransactionalEventListener(fallbackExecution = true, classes = DomainEventAttachedTransactionCommittedEvent.class)
    public void onTransactionCommitted(DomainEventAttachedTransactionCommittedEvent domainEventAttachedTransactionCommittedEvent) {
        List<EventRecord> events = domainEventAttachedTransactionCommittedEvent.getEvents();
        publish(events);
    }

    private void publish(List<EventRecord> events) {
        if (events != null && !events.isEmpty()) {
            events.forEach(event -> {
                eventPublisher.publish(event);
            });
        }
    }

    public static void reset() {
        TL_ENTITY_EVENT_PAYLOADS.remove();
        TL_EVENT_SCHEDULE_MAP.remove();
    }

    /**
     * 弹出实体绑定的事件列表
     *
     * @param entity 关联实体
     * @return 事件列表
     */
    protected Set<Object> popEvents(Object entity) {
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if (entityEventPayloads == null || !entityEventPayloads.containsKey(entity)) {
            return EMPTY_EVENT_PAYLOADS;
        }
        Set<Object> eventPayloads = entityEventPayloads.remove(entity);
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    /**
     * 记录事件发送时间
     *
     * @param eventPayload
     * @param schedule
     */
    protected void putDeliverTime(Object eventPayload, LocalDateTime schedule) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if (eventScheduleMap == null) {
            eventScheduleMap = new HashMap<>();
            TL_EVENT_SCHEDULE_MAP.set(eventScheduleMap);
        }
        eventScheduleMap.put(eventPayload, schedule);
    }

    public LocalDateTime getDeliverTime(Object eventPayload) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if (eventScheduleMap != null && eventScheduleMap.containsKey(eventPayload)) {
            return eventScheduleMap.get(eventPayload);
        } else {
            return LocalDateTime.now();
        }
    }
}
