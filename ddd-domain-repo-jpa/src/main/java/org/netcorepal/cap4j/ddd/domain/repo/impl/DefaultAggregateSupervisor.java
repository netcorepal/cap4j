package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.repo.*;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

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
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> findWithoutPersist(AggregatePredicate<AGGREGATE> aggregatePredicate, List<OrderInfo> orders) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entitys = repositorySupervisor.findWithoutPersist(predicate, orders);
        return entitys.stream().map(o -> newInstance(clazz, o)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> aggregatePredicate, List<OrderInfo> orders) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        List<?> entities = repositorySupervisor.find(predicate, orders);
        return entities.stream().map(e -> newInstance(clazz, e)).collect(Collectors.toList());
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOneWithoutPersist(AggregatePredicate<AGGREGATE> aggregatePredicate) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        Optional<?> entity = repositorySupervisor.findOneWithoutPersist(predicate);
        return entity.map(o -> newInstance(clazz, o));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> aggregatePredicate) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        Optional<?> entity = repositorySupervisor.findOne(predicate);
        return entity.map(o -> newInstance(clazz, o));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPageWithoutPersist(AggregatePredicate<AGGREGATE> aggregatePredicate, PageParam pageParam) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        PageData<?> pageData = repositorySupervisor.findPageWithoutPersist(predicate, pageParam);
        return pageData.transform(o -> newInstance(clazz, o));
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> aggregatePredicate, PageParam pageParam) {
        Class<AGGREGATE> clazz = JpaAggregatePredicateSupport.reflectXEntityClass(aggregatePredicate);
        Predicate predicate = JpaAggregatePredicateSupport.getPredicate((AggregatePredicate) aggregatePredicate);
        PageData<?> pageData = repositorySupervisor.findPage(predicate, pageParam);
        return pageData.transform(o -> newInstance(clazz, o));
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
