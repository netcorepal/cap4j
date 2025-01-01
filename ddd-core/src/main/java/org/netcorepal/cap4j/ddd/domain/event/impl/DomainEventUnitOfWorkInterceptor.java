package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventManager;

import java.util.Set;

/**
 * UOW拦截器，调用领域事件
 *
 * @author binking338
 * @date 2024/12/29
 */
@RequiredArgsConstructor
public class DomainEventUnitOfWorkInterceptor implements UnitOfWorkInterceptor {
    private final DomainEventManager domainEventManager;

    @Override
    public void beforeTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void preInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void postInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void afterTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void postEntitiesPersisted(Set<Object> entities) {
        domainEventManager.release(entities);
    }
}
