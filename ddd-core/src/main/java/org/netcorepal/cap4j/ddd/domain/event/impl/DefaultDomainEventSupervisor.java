package org.netcorepal.cap4j.ddd.domain.event.impl;

import org.netcorepal.cap4j.ddd.domain.event.DomainEventSupervisor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认领域事件管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
public class DefaultDomainEventSupervisor implements DomainEventSupervisor {
    public static DomainEventSupervisor Instance = new DefaultDomainEventSupervisor();
    private static final ThreadLocal<List<Object>> TL_EVENT_PAYLOADS = new ThreadLocal<List<Object>>();
    private static final ThreadLocal<Map<Object, LocalDateTime>> TL_EVENT_SCHEDULE_MAP = new ThreadLocal<Map<Object, LocalDateTime>>();
    private static final List<Object> EMPTY_EVENT_PAYLOADS = Collections.emptyList();

    @Override
    public void attach(Object eventPayload) {
        attach(eventPayload, LocalDateTime.now());
    }

    @Override
    public void attach(Object eventPayload, Duration delay){
        attach(eventPayload, LocalDateTime.now().plus(delay));
    }

    @Override
    public void attach(Object eventPayload, LocalDateTime schedule){
        List<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if(eventPayloads == null){
            eventPayloads = new java.util.ArrayList<Object>();
            TL_EVENT_PAYLOADS.set(eventPayloads);
        }
        eventPayloads.add(eventPayload);

        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if(eventScheduleMap == null){
            eventScheduleMap = new HashMap<>();
            TL_EVENT_SCHEDULE_MAP.set(eventScheduleMap);
        }
        eventScheduleMap.put(eventPayload, schedule);
    }

    @Override
    public void detach(Object eventPayload) {
        List<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        if(eventPayloads != null){
            eventPayloads.remove(eventPayload);
        }
    }

    @Override
    public void reset() {
        TL_EVENT_PAYLOADS.remove();
        TL_EVENT_SCHEDULE_MAP.remove();;
    }

    @Override
    public List<Object> getEvents() {
        List<Object> eventPayloads = TL_EVENT_PAYLOADS.get();
        return eventPayloads != null ? eventPayloads : EMPTY_EVENT_PAYLOADS;
    }

    @Override
    public LocalDateTime getDeliverTime(Object eventPlayload) {
        Map<Object, LocalDateTime> eventScheduleMap = TL_EVENT_SCHEDULE_MAP.get();
        if(eventScheduleMap != null && eventScheduleMap.containsKey(eventPlayload)){
            return eventScheduleMap.get(eventPlayload);
        } else {
            return LocalDateTime.now();
        }
    }
}
