package org.ddd.domain.repo;

import org.ddd.share.PageData;
import org.ddd.share.OrderInfo;
import org.ddd.share.PageParam;

import java.util.List;
import java.util.Optional;

/**
 * 聚合仓储
 * @author binking338
 * @date 2023/8/12
 */
public interface Repository<Entity> {

    /**
     * 通过ID判断实体是否存在
     * @param id
     * @return
     */
    boolean existsById(Object id);
    /**
     * 通过ID获取实体
     * @param id
     * @return
     */
    Optional<Entity> getById(Object id);

    /**
     * 通过ID获取实体
     * @param ids
     * @return
     */
    List<Entity> listByIds(Iterable<Object> ids);

    /**
     * 根据条件获取实体
     * @param condition
     * @return
     */
    Optional<Entity> getBy(Object condition);
    /**
     * 根据条件获取实体列表
     * @param condition
     * @param orders
     * @return
     */
    List<Entity> listBy(Object condition, List<OrderInfo> orders);
    /**
     * 根据条件获取实体分页列表
     * @param condition
     * @param pageParam
     * @return
     */
    PageData<Entity> pageBy(Object condition, PageParam pageParam);

    /**
     * 根据条件获取实体计数
     * @param condition
     * @return
     */
    long count(Object condition);

    /**
     * 根据条件判断实体是否存在
     * @param condition
     * @return
     */
    boolean exists(Object condition);
}
