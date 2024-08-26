package org.netcorepal.cap4j.ddd.domain.event.impl;

import org.netcorepal.cap4j.ddd.domain.event.DomainEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.EventSupervisor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 默认领域事件管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
public class DefaultDomainEventSupervisor implements DomainEventSupervisor, EventSupervisor {
    private static final ThreadLocal<Set<Object>> TL_EVENT_PAYLOADS = new ThreadLocal<Set<Object>>();
    private static final ThreadLocal<Map<Object, Set<Object>>> TL_ENTITY_EVENT_PAYLOADS = new ThreadLocal<Map<Object, Set<Object>>>();
    private static final ThreadLocal<Map<Object, LocalDateTime>> TL_EVENT_SCHEDULE_MAP = new ThreadLocal<Map<Object, LocalDateTime>>();
    private static final Set<Object> EMPTY_EVENT_PAYLOADS = Collections.emptySet();

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
    }

    @Override
    public void reset() {
        TL_EVENT_PAYLOADS.remove();
        TL_ENTITY_EVENT_PAYLOADS.remove();
        TL_EVENT_SCHEDULE_MAP.remove();
    }

    @Override
    public Set<Object> popEvents() {
        Set<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        TL_EVENT_PAYLOADS.remove();
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    @Override
    public Set<Object> popEvents(Object entity) {
        Map<Object, Set<Object>> entityEventPayloads = TL_ENTITY_EVENT_PAYLOADS.get();
        if(entityEventPayloads == null || !entityEventPayloads.containsKey(entity)){
            return EMPTY_EVENT_PAYLOADS;
        }
        Set<Object> eventPayloads = entityEventPayloads.remove(entity);
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    protected void putDeliverTime(Object eventPayload, LocalDateTime schedule) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if (eventScheduleMap == null) {
            eventScheduleMap = new HashMap<>();
            TL_EVENT_SCHEDULE_MAP.set(eventScheduleMap);
        }
        eventScheduleMap.put(eventPayload, schedule);
    }

    @Override
    public LocalDateTime getDeliverTime(Object eventPayload) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if (eventScheduleMap != null && eventScheduleMap.containsKey(eventPayload)) {
            return eventScheduleMap.get(eventPayload);
        } else {
            return LocalDateTime.now();
        }
    }


    @Override
    public <EVENT> void notify(EVENT eventPayload) {
        attach(eventPayload);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Duration delay) {
        attach(eventPayload, delay);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, LocalDateTime schedule) {
        attach(eventPayload, schedule);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity) {
        attach(eventPayload, entity);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity, Duration delay) {
        attach(eventPayload, entity, delay);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity, LocalDateTime schedule) {
        attach(eventPayload, entity, schedule);
    }
}
