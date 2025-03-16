package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> findWithoutPersist(AggregatePredicate<AGGREGATE> predicate) {
        return findWithoutPersist(predicate, (List<OrderInfo>) null);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> findWithoutPersist(AggregatePredicate<AGGREGATE> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> findWithoutPersist(AggregatePredicate<AGGREGATE> predicate, OrderInfo... orders){
        return findWithoutPersist(predicate, Arrays.asList(orders));
    }
    
    /**
     * 根据条件获取实体列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate) {
        return find(predicate, (List<OrderInfo>) null);
    }

    /**
     * 根据条件获取实体列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取实体列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param orders
     * @param <AGGREGATE>
     * @return
     */
    default <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, OrderInfo... orders) {
        return find(predicate, Arrays.asList(orders));
    }
    
    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOneWithoutPersist(AggregatePredicate<AGGREGATE> predicate);

    /**
     * 根据条件获取单个实体
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> predicate);

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPageWithoutPersist(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam);

    /**
     * 根据条件获取实体分页列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param pageParam
     * @param <AGGREGATE>
     * @return
     */
    <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam);

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
