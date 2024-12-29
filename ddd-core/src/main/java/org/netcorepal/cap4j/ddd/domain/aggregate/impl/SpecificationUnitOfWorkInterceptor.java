package org.netcorepal.cap4j.ddd.domain.aggregate.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;
import org.netcorepal.cap4j.ddd.domain.aggregate.SpecificationManager;
import org.netcorepal.cap4j.ddd.share.DomainException;

import java.util.Set;

/**
 * UOW拦截器，调用聚合的规约
 *
 * @author binking338
 * @date 2024/12/29
 */
@RequiredArgsConstructor
public class SpecificationUnitOfWorkInterceptor implements UnitOfWorkInterceptor {
    private final SpecificationManager specificationManager;

    @Override
    public void beforeTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {
        if (persistAggregates != null && !persistAggregates.isEmpty()) {
            for (Object entity : persistAggregates) {
                Specification.Result result = specificationManager.specifyBeforeTransaction(entity);
                if (!result.isPassed()) {
                    throw new DomainException(result.getMessage());
                }
            }
        }
    }

    @Override
    public void preInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {
        if (persistAggregates != null && !persistAggregates.isEmpty()) {
            for (Object entity : persistAggregates) {
                Specification.Result result = specificationManager.specifyInTransaction(entity);
                if (!result.isPassed()) {
                    throw new DomainException(result.getMessage());
                }
            }
        }
    }

    @Override
    public void postInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void afterTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates) {

    }

    @Override
    public void postEntitiesPersisted(Set<Object> entities) {

    }
}
