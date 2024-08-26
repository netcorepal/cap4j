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
        return RepositorySupervisorConfiguration.instance;
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
     * @param entityClass
     * @param condition
     * @param orders
     * @return
     * @param <ENTITY>
     */
    <ENTITY> List<ENTITY> find(Class<ENTITY> entityClass, Object condition, List<OrderInfo> orders);

    /**
     * 根据条件获取单个实体
     * @param entityClass
     * @param condition
     * @return
     * @param <ENTITY>
     */
    <ENTITY> Optional<ENTITY> findOne(Class<ENTITY> entityClass, Object condition);
    /**
     * 根据条件获取实体分页列表
     * @param entityClass
     * @param condition
     * @param pageParam
     * @return
     * @param <ENTITY>
     */
    <ENTITY> PageData<ENTITY> findPage(Class<ENTITY> entityClass, Object condition, PageParam pageParam);

    /**
     * 根据ID获取实体
     * @param entityClass
     * @param id
     * @return
     * @param <ENTITY>
     */
    <ENTITY> Optional<ENTITY> findById(Class<ENTITY> entityClass, Object id);
    /**
     * 根据ID获取实体列表
     * @param entityClass
     * @param ids
     * @return
     * @param <ENTITY>
     */
    <ENTITY> List<ENTITY> findByIds(Class<ENTITY> entityClass, Iterable<Object> ids);

    /**
     * 根据条件获取实体计数
     * @param entityClass
     * @param condition
     * @return
     * @param <ENTITY>
     */
    <ENTITY> long count(Class<ENTITY> entityClass, Object condition);

    /**
     * 根据条件判断实体是否存在
     * @param entityClass
     * @param condition
     * @return
     * @param <ENTITY>
     */
    <ENTITY> boolean exists(Class<ENTITY> entityClass, Object condition);

    /**
     * 通过ID判断实体是否存在
     * @param entityClass
     * @param id
     * @return
     * @param <ENTITY>
     */
    <ENTITY> boolean existsById(Class<ENTITY> entityClass, Object id);

}
