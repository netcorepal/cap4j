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
     * @param aggregate
     * @param type
     */
    void onChange(Entity aggregate, PersistType type);

    /**
     * 异常
     * @param aggregate
     * @param type
     * @param e
     */
    default void onExcepton(Entity aggregate, PersistType type, Exception e){

    }
}
