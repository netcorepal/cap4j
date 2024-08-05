package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 实体规格约束管理器
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface SpecificationManager {
    /**
     * 校验实体是否符合规格约束
     * @param entity
     * @return
     * @param <Entity>
     */
    <Entity> Specification.Result specify(Entity entity);
    /**
     * 校验实体是否符合规格约束（事务开启前）
     * @param entity
     * @return
     * @param <Entity>
     */
    <Entity> Specification.Result specifyBeforeTransaction(Entity entity);
}
