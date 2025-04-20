package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.domain.aggregate.Id;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.*;

/**
 * 聚合管理器
 *
 * @author binking338
 * @date 2025/1/12
 */
public interface AggregateSupervisor {
    static AggregateSupervisor getInstance() {
        return AggregateSupervisorSupport.instance;
    }

    <AGGREGATE extends Aggregate<ENTITY>, ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> AGGREGATE create(Class<AGGREGATE> clazz, ENTITY_PAYLOAD payload);

    /**
     * 根据id获取聚合
     *
     * @param id
     * @return
     * @param <AGGREGATE>
     * @param <ENTITY>
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AGGREGATE getById(Id<AGGREGATE, ?> id) {
        return getByIds(Collections.singletonList(id), true).stream().findFirst().orElse(null);
    }

    /**
     * 根据id获取聚合
     *
     * @param id
     * @param persist
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AGGREGATE getById(Id<AGGREGATE, ?> id, boolean persist) {
        return getByIds(Collections.singletonList(id), persist).stream().findFirst().orElse(null);
    }

    /**
     * 根据id获取聚合
     *
     * @param ids
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> List<AGGREGATE> getByIds(Iterable<Id<AGGREGATE, ?>> ids) {
        return getByIds(ids, true);
    }

    /**
     * 根据id获取聚合
     *
     * @param ids
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> List<AGGREGATE> getByIds(Id<AGGREGATE, ?>... ids) {
        return getByIds(Arrays.asList(ids), true);
    }

    /**
     * 根据id获取聚合
     *
     * @param ids
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    <AGGREGATE extends Aggregate<ENTITY>, ENTITY> List<AGGREGATE> getByIds(Iterable<Id<AGGREGATE, ?>> ids, boolean persist);

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate) {
        return find(predicate, (List<OrderInfo>) null, true);
    }

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param persist
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, boolean persist) {
        return find(predicate, (List<OrderInfo>) null, persist);
    }

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders) {
        return find(predicate, orders, true);
    }

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, OrderInfo... orders) {
        return find(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param pageParam
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam) {
        return find(predicate, pageParam, true);
    }

    /**
     * 根据条件获取聚合列表
     *
     * @param predicate
     * @param pageParam
     * @param persist
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> predicate) {
        return findOne(predicate, true);
    }

    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param persist
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> predicate, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param persist
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders) {
        return findFirst(predicate, orders, true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate, OrderInfo... orders) {
        return findFirst(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param persist
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate, boolean persist) {
        return findFirst(predicate, Collections.emptyList(), persist);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate) {
        return findFirst(predicate, true);
    }

    /**
     * 根据条件获取实体分页列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param pageParam
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam) {
        return findPage(predicate, pageParam, true);
    }

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @param persist
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据id删除聚合
     *
     * @param id
     * @return
     * @param <AGGREGATE>
     * @param <ENTITY>
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AGGREGATE removeById(Id<AGGREGATE, ?> id) {
        return removeByIds(Collections.singletonList(id)).stream().findFirst().orElse(null);
    }

    /**
     * 根据id删除聚合
     *
     * @param ids
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    default <AGGREGATE extends Aggregate<ENTITY>, ENTITY> List<AGGREGATE> removeByIds(Id<AGGREGATE, ?>... ids) {
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 根据id删除聚合
     *
     * @param ids
     * @param <AGGREGATE>
     * @param <ENTITY>
     * @return
     */
    <AGGREGATE extends Aggregate<ENTITY>, ENTITY> List<AGGREGATE> removeByIds(Iterable<Id<AGGREGATE, ?>> ids);

    /**
     * 根据条件删除实体
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> predicate);

    /**
     * 根据条件删除实体
     *
     * @param predicate
     * @param limit
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> predicate, int limit);

    /**
     * 根据条件获取实体计数
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> long count(AggregatePredicate<AGGREGATE> predicate);

    /**
     * 根据条件判断实体是否存在
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> boolean exists(AggregatePredicate<AGGREGATE> predicate);
}
