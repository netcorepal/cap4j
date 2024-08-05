package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 基于Jpq的实体持久化监听抽象类
 *
 * @author binking338
 * @date 2024/3/9
 */
public abstract class AbstractJpaPersistListener<Entity> implements PersistListener<Entity> {
    public abstract Class<Entity> forEntityClass();

    public boolean throwOnException() {
        return true;
    }
}
