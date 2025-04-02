package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.*;

/**
 * 仓储管理器
 *
 * @author binking338
 * @date 2024/8/25
 */
public interface RepositorySupervisor {
    static RepositorySupervisor getInstance() {
        return RepositorySupervisorSupport.instance;
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate) {
        return find(predicate, (List<OrderInfo>) null, true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param persist
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, boolean persist) {
        return find(predicate, (List<OrderInfo>) null, persist);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders){
        return find(predicate, orders, true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, OrderInfo... orders){
        return find(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param pageParam
     * @return
     */
    default <ENTITY>  List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam) {
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
    <ENTITY>  List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate){
        return findOne(predicate, true);
    }

    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param persist
     * @param <ENTITY>
     * @return
     */
    <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param persist
     * @param <ENTITY>
     * @return
     */
    <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders) {
        return findFirst(predicate, orders, true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, OrderInfo... orders) {
        return findFirst(predicate, Arrays.asList(orders), true);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @param persist
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, boolean persist) {
        return findFirst(predicate, Collections.emptyList(), persist);
    }

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @return
     */
    default <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate) {
        return findFirst(predicate, true);
    }

    /**
     * 根据条件获取实体分页列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param pageParam
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam){
        return findPage(predicate, pageParam, true);
    }

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @param persist
     * @param <ENTITY>
     * @return
     */
    <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist);

    /**
     * 根据条件删除实体
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate);

    /**
     * 根据条件删除实体
     *
     * @param predicate
     * @param limit
     * @param <ENTITY>
     * @return
     */
    <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit);

    /**
     * 根据条件获取实体计数
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    <ENTITY> long count(Predicate<ENTITY> predicate);

    /**
     * 根据条件判断实体是否存在
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    <ENTITY> boolean exists(Predicate<ENTITY> predicate);

}
