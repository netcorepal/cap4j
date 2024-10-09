package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 实体规格约束管理器
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface SpecificationManager {
    /**
     * 校验实体是否符合规格约束
     *
     * @param entity
     * @param <Entity>
     * @return
     */
    <Entity> Specification.Result specifyInTransaction(Entity entity);

    /**
     * 校验实体是否符合规格约束（事务开启前）
     *
     * @param entity
     * @param <Entity>
     * @return
     */
    <Entity> Specification.Result specifyBeforeTransaction(Entity entity);
}
