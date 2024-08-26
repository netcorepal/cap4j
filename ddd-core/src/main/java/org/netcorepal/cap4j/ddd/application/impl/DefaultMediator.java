package org.netcorepal.cap4j.ddd.application.impl;

import org.netcorepal.cap4j.ddd.application.Mediator;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.EventSupervisor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 默认中介者
 *
 * @author binking338
 * @date 2024/8/24
 */
public class DefaultMediator implements Mediator {
    @Override
    public <REQUEST> Object request(REQUEST request) {
        return RequestSupervisor.getInstance().request(request);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE request(REQUEST request, Class<RESPONSE> resultClass) {
        return RequestSupervisor.getInstance().request(request, resultClass);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE request(REQUEST request, Class<REQUEST> paramClass, Class<RESPONSE> resultClass) {
        return RequestSupervisor.getInstance().request(request, paramClass, resultClass);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload) {
        EventSupervisor.getInstance().notify(eventPayload);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Duration delay) {
        EventSupervisor.getInstance().notify(eventPayload, delay);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, LocalDateTime schedule) {
        EventSupervisor.getInstance().notify(eventPayload, schedule);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity) {
        EventSupervisor.getInstance().notify(eventPayload, entity);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity, Duration delay) {
        EventSupervisor.getInstance().notify(eventPayload, entity, delay);
    }

    @Override
    public <EVENT> void notify(EVENT eventPayload, Object entity, LocalDateTime schedule) {
        EventSupervisor.getInstance().notify(eventPayload, entity, schedule);
    }
}
