package org.netcorepal.cap4j.ddd.application.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventManager;

import java.util.Set;

/**
 * UOW拦截器，调用集成事件
 *
 * @author binking338
 * @date 2024/12/29
 */
@RequiredArgsConstructor
public class IntergrationEventUnitOfWorkInterceptor implements UnitOfWorkInterceptor {
    private final IntegrationEventManager integrationEventManager;

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
        integrationEventManager.release();
    }
}
