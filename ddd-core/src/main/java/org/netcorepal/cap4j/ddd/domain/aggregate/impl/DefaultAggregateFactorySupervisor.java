package org.netcorepal.cap4j.ddd.domain.aggregate.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.share.DomainException;
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
    private final List<AggregateFactory<?>> factories;

    private Map<Class<?>, AggregateFactory<?>> factoryMap = null;

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
                                factory.getClass(), 0,
                                AggregateFactory.class
                        ),
                        factory
                );

            });
        }
    }

    @Override
    public <ENTITY> ENTITY create(Class<ENTITY> entityClass) {
        return create(entityClass, null);
    }

    @Override
    public <ENTITY> ENTITY create(Class<ENTITY> entityClass, AggregateFactory.InitHandler<ENTITY> initHandler) {
        AggregateFactory<?> factory = factoryMap.computeIfAbsent(entityClass, (cls) -> (i) -> {
            try {
                ENTITY entity = (ENTITY) entityClass.newInstance();
                if (null != i) {
                    i.init(entity);
                }
                return entity;
            } catch (Exception e) {
                throw new DomainException("聚合实例创建异常", e);
            }
        });

        Object instance = ((AggregateFactory<Object>) factory).create(
                null != initHandler
                        ? (AggregateFactory.InitHandler<Object>) initHandler
                        : AggregateFactory.InitHandler.getDefault()
        );
        return (ENTITY) instance;
    }
}
