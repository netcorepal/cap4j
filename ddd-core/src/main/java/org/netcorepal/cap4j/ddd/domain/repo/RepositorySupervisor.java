package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.List;
import java.util.Optional;

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
     * 获取仓储
     *
     * @param entityClass 实体类型
     * @param <ENTITY>    实体类型
     * @return {@link Repository}
     */
    <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass);

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate) {
        return find(predicate, null);
    }

    /**
     * 根据条件获取实体列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    default <ENTITY> List<ENTITY> find4Update(Predicate<ENTITY> predicate) {
        return find4Update(predicate, null);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取实体列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param orders
     * @param <ENTITY>
     * @return
     */
    <ENTITY> List<ENTITY> find4Update(Predicate<ENTITY> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取单个实体
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate);

    /**
     * 根据条件获取单个实体
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    <ENTITY> Optional<ENTITY> findOne4Update(Predicate<ENTITY> predicate);

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @param <ENTITY>
     * @return
     */
    <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam);

    /**
     * 根据条件获取实体分页列表
     * 自动调用 UnitOfWork::persist
     *
     * @param predicate
     * @param pageParam
     * @param <ENTITY>
     * @return
     */
    <ENTITY> PageData<ENTITY> findPage4Update(Predicate<ENTITY> predicate, PageParam pageParam);

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
