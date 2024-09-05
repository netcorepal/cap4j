package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 实体持久化监听接口
 *
 * @author binking338
 * @date 2024/1/31
 */
public interface PersistListener<Entity> {

    /**
     * 持久化变更
     * @param entity
     * @param type
     */
    void onChange(Entity entity, PersistType type);

}
