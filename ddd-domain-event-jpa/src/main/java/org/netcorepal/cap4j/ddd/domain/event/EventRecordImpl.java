package org.netcorepal.cap4j.ddd.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.netcorepal.cap4j.ddd.share.Constants.*;

/**
 * 事件记录实现
 *
 * @author binking338
 * @date
 */
@Slf4j
public class EventRecordImpl implements EventRecord {
    private Event event;
    private boolean persist = false;
    private Message<Object> message = null;

    public EventRecordImpl() {
        event = Event.builder().build();
    }

    public void resume(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return event.toString();
    }

    @Override
    public void init(Object payload, String svcName, LocalDateTime now, Duration expireAfter, int retryTimes) {
        event = Event.builder().build();
        event.init(payload, svcName, now, expireAfter, retryTimes);
    }

    @Override
    public String getId() {
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
    public void markPersist(boolean persist) {
        this.persist = persist;
    }

    @Override
    public boolean isPersist() {
        return persist;
    }

    @Override
    public Message<Object> getMessage() {
        if (null != this.message) {
            return this.message;
        }
        this.message = new GenericMessage(
                this.getPayload(),
                new EventMessageInterceptor.ModifiableMessageHeaders(null, UUID.fromString(this.getId()), null)
        );
        boolean isIntegrationEvent = this.getPayload().getClass().getAnnotation(IntegrationEvent.class) != null;
        message.getHeaders().put(HEADER_KEY_CAP4J_EVENT_ID, event.getId());
        message.getHeaders().put(HEADER_KEY_CAP4J_EVENT_TYPE, isIntegrationEvent
                ? HEADER_VALUE_CAP4J_EVENT_TYPE_INTEGRATION
                : HEADER_VALUE_CAP4J_EVENT_TYPE_DOMAIN);
        message.getHeaders().put(HEADER_KEY_CAP4J_PERSIST, this.persist);
        LocalDateTime now = LocalDateTime.now();
        message.getHeaders().put(HEADER_KEY_CAP4J_TIMESTAMP, now.toEpochSecond(ZoneOffset.UTC));
        if (this.getScheduleTime().isAfter(now)) {
            message.getHeaders().put(HEADER_KEY_CAP4J_SCHEDULE, this.getScheduleTime().toEpochSecond(ZoneOffset.UTC));
        }
        return message;
    }

    @Override
    public boolean isValid() {
        return event.isValid();
    }

    @Override
    public boolean isInvalid() {
        return event.isInvalid();
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
