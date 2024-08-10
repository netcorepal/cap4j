package org.netcorepal.cap4j.ddd.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 事件记录实现
 *
 * @author binking338
 * @date
 */
@Slf4j
public class EventRecordImpl implements EventRecord {
    private Event event;

    public EventRecordImpl(){
        event = Event.builder().build();
    }

    public void resume(Event event){
        this.event = event;
    }

    public Event getEvent(){
        return event;
    }

    @Override
    public String toString(){
        return event.toString();
    }

    @Override
    public void init(Object payload, String svcName, LocalDateTime now, Duration expireAfter, int retryTimes) {
        event = Event.builder().build();
        event.init(payload, svcName, now, expireAfter, retryTimes);
    }

    @Override
    public String getId(){
        return event.getEventUuid();
    }

    @Override
    public String getEventTopic() {
        return event.getEventType();
    }

    @Override
    public Object getPayload() {
        return event.getPayload();
    }

    @Override
    public LocalDateTime getScheduleTime() {
        return event.getCreateAt();
    }

    @Override
    public LocalDateTime getNextTryTime() {
        return event.getNextTryTime();
    }

    @Override
    public boolean isDelivering() {
        return event.isDelivering();
    }

    @Override
    public boolean isDelivered() {
        return event.isDelivered();
    }

    @Override
    public boolean beginDelivery(LocalDateTime now) {
        return event.holdState4Delivery(now);
    }

    @Override
    public boolean cancelDelivery(LocalDateTime now) {
        return event.cancelDelivery(now);
    }

    @Override
    public void occuredException(LocalDateTime now, Throwable throwable) {
        event.occuredException(now, throwable);
    }

    @Override
    public void confirmedDelivery(LocalDateTime now) {
        event.confirmedDelivery(now);
    }
}
