package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

import java.util.List;
import java.util.Optional;

/**
 * 聚合仓储
 *
 * @author binking338
 * @date 2023/8/12
 */
public interface Repository<Entity> {

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @return
     */
    default List<Entity> find(Predicate<Entity> predicate) {
        return find(predicate, null);
    }

    /**
     * 根据条件获取实体列表
     *
     * @param predicate
     * @param orders
     * @return
     */
    List<Entity> find(Predicate<Entity> predicate, List<OrderInfo> orders);

    /**
     * 根据条件获取实体
     *
     * @param predicate
     * @return
     */
    Optional<Entity> findOne(Predicate<Entity> predicate);

    /**
     * 根据条件获取实体分页列表
     *
     * @param predicate
     * @param pageParam
     * @return
     */
    PageData<Entity> findPage(Predicate<Entity> predicate, PageParam pageParam);

    /**
     * 根据条件获取实体计数
     *
     * @param predicate
     * @return
     */
    long count(Predicate<Entity> predicate);

    /**
     * 根据条件判断实体是否存在
     *
     * @param predicate
     * @return
     */
    boolean exists(Predicate<Entity> predicate);

//    /**
//     * 通过ID判断实体是否存在
//     * @param id
//     * @return
//     */
//    boolean existsById(Object id);
}
