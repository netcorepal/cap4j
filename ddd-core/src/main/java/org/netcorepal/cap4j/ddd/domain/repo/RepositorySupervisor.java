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
    static RepositorySupervisor getInstance(){
        return RepositorySupervisorSupport.instance;
    }

    /**
     * 获取仓储
     * @param entityClass 实体类型
     * @return {@link Repository}
     * @param <ENTITY> 实体类型
     */
    <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass);

    /**
     * 根据条件获取实体列表
     * @param predicate
     * @param orders
     * @return
     * @param <ENTITY>
     */
    <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取单个实体
     * @param predicate
     * @return
     * @param <ENTITY>
     */
    <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate);
    /**
     * 根据条件获取实体分页列表
     * @param predicate
     * @param pageParam
     * @return
     * @param <ENTITY>
     */
    <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam);

    /**
     * 根据条件删除实体
     *
     * @param predicate
     * @param limit
     * @return
     * @param <ENTITY>
     */
    <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit);

    /**
     * 根据条件获取实体计数
     * @param predicate
     * @return
     * @param <ENTITY>
     */
    <ENTITY> long count(Predicate<ENTITY> predicate);

    /**
     * 根据条件判断实体是否存在
     * @param predicate
     * @return
     * @param <ENTITY>
     */
    <ENTITY> boolean exists(Predicate<ENTITY> predicate);

}
