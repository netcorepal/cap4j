package org.netcorepal.cap4j.ddd.domain.aggregate.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认聚合工厂管理器
 *
 * @author binking338
 * @date 2024/9/3
 */
@RequiredArgsConstructor
public class DefaultAggregateFactorySupervisor implements AggregateFactorySupervisor {
    private final List<AggregateFactory<?, ?>> factories;
    private final UnitOfWork unitOfWork;

    private Map<Class<?>, AggregateFactory<?, ?>> factoryMap = null;

    public void init() {
        if (null != factoryMap) {
            return;
        }
        synchronized (this) {
            if (null != factoryMap) {
                return;
            }
            factoryMap = new HashMap<>();
            factories.forEach(factory -> {
                factoryMap.put(
                        ClassUtils.resolveGenericTypeClass(
                                factory, 0,
                                AggregateFactory.class
                        ),
                        factory
                );
            });
        }
    }

    @Override
    public <ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> ENTITY create(ENTITY_PAYLOAD entityPayload) {
        init();
        AggregateFactory<?, ?> factory = factoryMap.get(entityPayload.getClass());
        if (null == factory) {
            return null;
        }
        ENTITY instance = ((AggregateFactory<ENTITY_PAYLOAD, ENTITY>) factory).create(entityPayload);
        unitOfWork.persist(instance);
        return instance;
    }
}
