package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.*;

/**
 * 聚合仓储
 *
 * @author binking338
 * @date 2023/8/12
 */
public interface Repository<ENTITY> {

    /**
     * 支持条件类型
     *
     * @return
     */
    Class<?> supportPredicateClass();

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @return
     */
    default List<ENTITY> find(Predicate<ENTITY> predicate) {
        return find(predicate, (Collection<OrderInfo>) null);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param persist
     * @return
     */
    default List<ENTITY> find(Predicate<ENTITY> predicate, boolean persist) {
        return find(predicate, (List<OrderInfo>) null, persist);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @return
     */
    default List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders) {
        return find(predicate, orders, true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @return
     */
    default List<ENTITY> find(Predicate<ENTITY> predicate, OrderInfo... orders) {
        return find(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param persist
     * @return
     */
    List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param pageParam
     * @return
     */
    default List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam) {
        return find(predicate, pageParam, true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param pageParam
     * @param persist
     * @return
     */
    List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param persist
     * @return
     */
    Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @return
     */
    default Optional<ENTITY> findOne(Predicate<ENTITY> predicate) {
        return findOne(predicate, true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param persist
     * @return
     */
    Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @return
     */
    default Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders) {
        return findFirst(predicate, orders, true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @return
     */
    default Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, OrderInfo... orders) {
        return findFirst(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param persist
     * @return
     */
    default Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, boolean persist) {
        return findFirst(predicate, Collections.emptyList(), persist);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @return
     */
    default Optional<ENTITY> findFirst(Predicate<ENTITY> predicate) {
        return findFirst(predicate, true);
    }

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @param persist
     * @return
     */
    PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @return
     */
    default PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam) {
        return findPage(predicate, pageParam, true);
    }

    /**
     * 根据条件获取实体计数
     *
     * @param predicate
     * @return
     */
    long count(Predicate<ENTITY> predicate);

    /**
     * 根据条件判断实体是否存在
     *
     * @param predicate
     * @return
     */
    boolean exists(Predicate<ENTITY> predicate);

//    /**
//     * 通过ID判断实体是否存在
//     * @param id
//     * @return
//     */
//    boolean existsById(Object id);
}
