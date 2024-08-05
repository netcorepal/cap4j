package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * @author binking338
 * @date 2024/1/31
 */
public interface PersistListener<Entity> {

    /**
     * 持久化变更
     * @param entity
     */
    void onChange(Entity entity);

    /**
     * 新增实体时
     * @param entity
     */
    void onCreate(Entity entity);

    /**
     * 更新实体时
     * @param entity
     */
    void onUpdate(Entity entity);

    /**
     * 删除实体时
     * @param entity
     */
    void onDelete(Entity entity);

}
