package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.domain.repo.*;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认聚合管理器
 *
 * @author binking338
 * @date 2025/1/12
 */
@RequiredArgsConstructor
public class DefaultAggregateSupervisor implements AggregateSupervisor {
    final RepositorySupervisor repositorySupervisor;

    private static <AGGREGATE extends Aggregate<?>> AGGREGATE newInstance(Class<AGGREGATE> clazz, Object entity) {
        try {
            Aggregate aggregate = clazz.getConstructor().newInstance();
            aggregate._wrap(entity);
            return (AGGREGATE) aggregate;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <AGGREGATE extends Aggregate<ENTITY>, ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> AGGREGATE create(Class<AGGREGATE> clazz, ENTITY_PAYLOAD payload) {
        ENTITY entity = AggregateFactorySupervisor.getInstance().create(payload);
        return newInstance(clazz, entity);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> aggregatePredicate, Collection<OrderInfo> orders, boolean persist) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entities = repositorySupervisor.find(predicate, orders, persist);
        return entities.stream().map(e -> newInstance(clazz, e)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> aggregatePredicate, PageParam pageParam, boolean persist) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entities = repositorySupervisor.find(predicate, pageParam, persist);
        return entities.stream().map(e -> newInstance(clazz, e)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> aggregatePredicate, boolean persist) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        Optional<?> entity = repositorySupervisor.findOne(predicate, persist);
        return entity.map(e -> newInstance(clazz, e));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> aggregatePredicate, Collection<OrderInfo> orders, boolean persist) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        Optional<?> entity = repositorySupervisor.findFirst(predicate, orders, persist);
        return entity.map(e -> newInstance(clazz, e));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> aggregatePredicate, PageParam pageParam, boolean persist) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        PageData<?> entities = repositorySupervisor.findPage(predicate, pageParam, persist);
        return entities.transform(e -> newInstance(clazz, e));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> aggregatePredicate) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entities = repositorySupervisor.remove(predicate);
        return entities.stream().map(e -> newInstance(clazz, e)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> aggregatePredicate, int limit) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entities = repositorySupervisor.remove(predicate, limit);
        return entities.stream().map(e -> newInstance(clazz, e)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> long count(AggregatePredicate<AGGREGATE> aggregatePredicate) {
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        long count = repositorySupervisor.count(predicate);
        return count;
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> boolean exists(AggregatePredicate<AGGREGATE> aggregatePredicate) {
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        boolean exists = repositorySupervisor.exists(predicate);
        return exists;
    }
}
