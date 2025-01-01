package org.netcorepal.cap4j.ddd.application;

import java.util.Set;

/**
 * UOW工作单元拦截器
 *
 * @author binking338
 * @date 2024/12/29
 */
public interface UnitOfWorkInterceptor {

    /**
     * 事务开始前
     *
     * @param persistAggregates 待持久化聚合根（新建、更新）
     * @param removeAggregates  待删除聚合根
     */
    void beforeTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates);

    /**
     * 事务执行最初
     *
     * @param persistAggregates 待持久化聚合根（新建、更新）
     * @param removeAggregates  待删除聚合根
     */
    void preInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates);

    /**
     * 事务执行之后
     *
     * @param persistAggregates 待持久化聚合根（新建、更新）
     * @param removeAggregates  待删除聚合根
     */
    void postInTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates);

    /**
     * 事务结束后
     *
     * @param persistAggregates 待持久化聚合根（新建、更新）
     * @param removeAggregates  待删除聚合根
     */
    void afterTransaction(Set<Object> persistAggregates, Set<Object> removeAggregates);

    /**
     * 实体持久化之后
     * @param entities 实体
     */
    void postEntitiesPersisted(Set<Object> entities);
}
