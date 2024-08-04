package org.ddd.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.ddd.domain.event.EventRecord;
import org.ddd.domain.event.persistence.Event;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author <template/>
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
    public String getEventTopic() {
        return event.getEventType();
    }

    @Override
    public Object getPayload() {
        return event.getPayload();
    }

    @Override
    public boolean beginDelivery(LocalDateTime now) {
        return event.holdState4Delivery(now);
    }

    @Override
    public void confirmDelivered(LocalDateTime now) {
        event.confirmDelivered(now);
    }
}
