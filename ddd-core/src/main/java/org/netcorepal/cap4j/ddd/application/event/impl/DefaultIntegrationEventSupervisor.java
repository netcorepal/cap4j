package org.netcorepal.cap4j.ddd.application.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.*;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.domain.event.EventRecordRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 默认事件管理器
 *
 * @author binking338
 * @date 2024/8/28
 */
@RequiredArgsConstructor
public class DefaultIntegrationEventSupervisor implements IntegrationEventSupervisor, IntegrationEventManager {
    private final EventPublisher eventPublisher;
    private final EventRecordRepository eventRecordRepository;
    private final IntegrationEventInterceptorManager integrationEventInterceptorManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String svcName;

    private static final ThreadLocal<Set<Object>> TL_EVENT_PAYLOADS = new ThreadLocal<Set<Object>>();
    private static final ThreadLocal<Map<Object, LocalDateTime>> TL_EVENT_SCHEDULE_MAP = new ThreadLocal<Map<Object, LocalDateTime>>();
    private static final Set<Object> EMPTY_EVENT_PAYLOADS = Collections.emptySet();

    /**
     * 默认事件过期时间（分钟）
     * 一天 60*24 = 1440
     */
    private static final int DEFAULT_EVENT_EXPIRE_MINUTES = 1440;
    /**
     * 默认事件重试次数
     */
    private static final int DEFAULT_EVENT_RETRY_TIMES = 200;

    @Override
    public <EVENT> void attach(EVENT eventPayload, LocalDateTime schedule) {
        // 判断集成事件，仅支持集成事件。
        if (eventPayload == null) {
            throw new DomainException("事件负载不能为空");
        }
        if (!eventPayload.getClass().isAnnotationPresent(IntegrationEvent.class)) {
            throw new DomainException("事件类型必须为集成事件");
        }
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if (eventPayloads == null) {
            eventPayloads = new HashSet<>();
            TL_EVENT_PAYLOADS.set(eventPayloads);
        }
        eventPayloads.add(eventPayload);
        putDeliverTime(eventPayload, schedule);
        integrationEventInterceptorManager.getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.onAttach(eventPayload, schedule));

    }

    @Override
    public <EVENT> void detach(EVENT eventPayload) {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if (eventPayloads == null) {
            return;
        }
        eventPayloads.remove(eventPayload);
        integrationEventInterceptorManager.getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.onDetach(eventPayload));
    }

    @Override
    public void release() {
        Set<Object> eventPayloads = new HashSet<>();
        eventPayloads.addAll(this.popEvents());
        List<EventRecord> persistedEvents = new ArrayList<>(eventPayloads.size());
        for (Object eventPayload : eventPayloads) {
            LocalDateTime deliverTime = this.getDeliverTime(eventPayload);
            EventRecord event = eventRecordRepository.create();
            event.init(eventPayload, this.svcName, deliverTime, Duration.ofMinutes(DEFAULT_EVENT_EXPIRE_MINUTES), DEFAULT_EVENT_RETRY_TIMES);
            event.markPersist(true);
            integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.prePersist(event));
            eventRecordRepository.save(event);
            integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.postPersist(event));
            persistedEvents.add(event);
        }
        IntegrationEventAttachedTransactionCommittedEvent integrationEventAttachedTransactionCommittedEvent = new IntegrationEventAttachedTransactionCommittedEvent(this, persistedEvents);
        applicationEventPublisher.publishEvent(integrationEventAttachedTransactionCommittedEvent);
    }

    @Override
    public <EVENT> void publish(EVENT eventPayload, LocalDateTime schedule){
        List<EventRecord> persistedEvents = new ArrayList<>(1);
        EventRecord event = eventRecordRepository.create();
        event.init(eventPayload, this.svcName, schedule, Duration.ofMinutes(DEFAULT_EVENT_EXPIRE_MINUTES), DEFAULT_EVENT_RETRY_TIMES);
        event.markPersist(true);
        integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.prePersist(event));
        eventRecordRepository.save(event);
        integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.postPersist(event));
        persistedEvents.add(event);
        IntegrationEventAttachedTransactionCommittedEvent integrationEventAttachedTransactionCommittedEvent = new IntegrationEventAttachedTransactionCommittedEvent(this, persistedEvents);
        applicationEventPublisher.publishEvent(integrationEventAttachedTransactionCommittedEvent);
    }

    @TransactionalEventListener(fallbackExecution = true, classes = IntegrationEventAttachedTransactionCommittedEvent.class)
    public void onTransactionCommitted(IntegrationEventAttachedTransactionCommittedEvent integrationEventAttachedTransactionCommittedEvent) {
        List<EventRecord> events = integrationEventAttachedTransactionCommittedEvent.getEvents();

        if (events != null && !events.isEmpty()) {
            events.forEach(event -> {
                eventPublisher.publish(event);
            });
        }
    }


    public static void reset() {
        TL_EVENT_PAYLOADS.remove();
        TL_EVENT_SCHEDULE_MAP.remove();
    }

    /**
     * 弹出事件列表
     *
     * @return 事件列表
     */
    protected Set<Object> popEvents() {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        TL_EVENT_PAYLOADS.remove();
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

    protected LocalDateTime getDeliverTime(Object eventPayload) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if (eventScheduleMap != null && eventScheduleMap.containsKey(eventPayload)) {
            return eventScheduleMap.get(eventPayload);
        } else {
            return LocalDateTime.now();
        }
    }
}
