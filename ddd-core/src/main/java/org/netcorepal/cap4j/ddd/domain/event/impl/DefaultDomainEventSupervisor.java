package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventAttachedTransactionPreCommitEvent;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventAttachedTransactionPostCommitEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.transaction.event.TransactionalEventListener;

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
public class DefaultDomainEventSupervisor implements DomainEventSupervisor {
    private final EventRecordRepository eventRecordRepository;
    private final List<DomainEventInterceptor> domainEventInterceptors;
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String svcName;


    private static final ThreadLocal<Set<Object>> TL_EVENT_PAYLOADS = new ThreadLocal<Set<Object>>();
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

    private List<DomainEventInterceptor> sortedDomainEventInterceptors = null;
    /**
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     * @return
     */
    protected List<DomainEventInterceptor> getOrderedDomainEventInterceptors() {
        if(sortedDomainEventInterceptors == null){
            sortedDomainEventInterceptors = new ArrayList<>(domainEventInterceptors);
            sortedDomainEventInterceptors.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
        }
        return sortedDomainEventInterceptors;
    }

    @Override
    public void attach(Object eventPayload) {
        attach(eventPayload, LocalDateTime.now());
    }

    @Override
    public void attach(Object eventPayload, Duration delay) {
        attach(eventPayload, LocalDateTime.now().plus(delay));
    }

    @Override
    public void attach(Object eventPayload, LocalDateTime schedule) {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if (eventPayloads == null) {
            eventPayloads = new HashSet<>();
            TL_EVENT_PAYLOADS.set(eventPayloads);
        }
        eventPayloads.add(eventPayload);
        putDeliverTime(eventPayload, schedule);
        getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onAttach(eventPayload, null, schedule));
    }

    @Override
    public void attach(Object eventPayload, Object entity) {
        attach(eventPayload, entity, LocalDateTime.now());
    }

    @Override
    public void attach(Object eventPayload, Object entity, Duration delay) {
        attach(eventPayload, entity, LocalDateTime.now().plus(delay));
    }

    @Override
    public void attach(Object eventPayload, Object entity, LocalDateTime schedule) {
        getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onAttach(eventPayload, entity, schedule));

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
    }

    @Override
    public void detach(Object eventPayload) {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if (eventPayloads == null) {
            return;
        }
        eventPayloads.remove(eventPayload);
        getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onDetach(eventPayload, null));
    }

    @Override
    public void detach(Object eventPayload, Object entity) {
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if (entityEventPayloads == null) {
            return;
        }
        Set<Object> eventPayloads = entityEventPayloads.containsKey(entity) ? entityEventPayloads.get(entity) : null;
        if (eventPayloads == null) {
            return;
        }

        eventPayloads.remove(eventPayload);
        getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onDetach(eventPayload, null));
    }

    @Override
    public void release(Set<Object> entities) {
        Set<Object> eventPayloads = new HashSet<>();
        eventPayloads.addAll(this.popEvents());
        for (Object entity : entities) {
            eventPayloads.addAll(this.popEvents(entity));
        }
        List<EventRecord> persistedEvents = new ArrayList<>(eventPayloads.size());
        List<EventRecord> transientEvents = new ArrayList<>(eventPayloads.size());
        LocalDateTime now = LocalDateTime.now();
        for (Object eventPayload : eventPayloads) {
            LocalDateTime deliverTime = this.getDeliverTime(eventPayload);
            EventRecord event = eventRecordRepository.create();
            event.init(eventPayload, this.svcName, deliverTime, Duration.ofMinutes(DEFAULT_EVENT_EXPIRE_MINUTES), DEFAULT_EVENT_RETRY_TIMES);
            boolean isDelayDeliver = !deliverTime.isAfter(now);
            if (!isDomainEventNeedPersist(eventPayload) && isDelayDeliver) {
                event.markPersist(false);
                transientEvents.add(event);
            } else {
                event.markPersist(true);
                getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.prePersist(event));
                eventRecordRepository.save(event);
                getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.postPersist(event));
                persistedEvents.add(event);
            }
        }
        DomainEventAttachedTransactionPreCommitEvent domainEventAttachedTransactionPreCommitEvent = new DomainEventAttachedTransactionPreCommitEvent(this, transientEvents);
        DomainEventAttachedTransactionPostCommitEvent domainEventAttachedTransactionPostCommitEvent = new DomainEventAttachedTransactionPostCommitEvent(this, persistedEvents);
        onTransactionCommiting(domainEventAttachedTransactionPreCommitEvent);
        applicationEventPublisher.publishEvent(domainEventAttachedTransactionPreCommitEvent);
        applicationEventPublisher.publishEvent(domainEventAttachedTransactionPostCommitEvent);
    }

    /**
     * 判断事件是否需要持久化
     * - 延迟或定时领域事件视情况进行持久化
     * - 显式指定persist=true的领域事件必须持久化
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

    protected void onTransactionCommiting(DomainEventAttachedTransactionPreCommitEvent domainEventAttachedTransactionPreCommitEvent) {
        List<EventRecord> events = domainEventAttachedTransactionPreCommitEvent.getEvents();
        publish(events);
    }

    @TransactionalEventListener(fallbackExecution = true, classes = DomainEventAttachedTransactionPostCommitEvent.class)
    public void onTransactionCommitted(DomainEventAttachedTransactionPostCommitEvent domainEventAttachedTransactionPostCommitEvent) {
        List<EventRecord> events = domainEventAttachedTransactionPostCommitEvent.getEvents();
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
        TL_EVENT_PAYLOADS.remove();
        TL_ENTITY_EVENT_PAYLOADS.remove();
        TL_EVENT_SCHEDULE_MAP.remove();
    }

    /**
     * 弹出事件列表
     * @return 事件列表
     */
    protected Set<Object> popEvents() {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        TL_EVENT_PAYLOADS.remove();
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    /**
     * 弹出实体绑定的事件列表
     * @param entity 关联实体
     * @return 事件列表
     */
    protected Set<Object> popEvents(Object entity) {
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if(entityEventPayloads == null || !entityEventPayloads.containsKey(entity)){
            return EMPTY_EVENT_PAYLOADS;
        }
        Set<Object> eventPayloads = entityEventPayloads.remove(entity);
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    /**
     * 记录事件发送时间
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
