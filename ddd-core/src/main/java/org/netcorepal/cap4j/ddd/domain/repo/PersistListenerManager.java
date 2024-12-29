package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 持久化监听管理器
 *
 * @author binking338
 * @date 2024/1/31
 */
public interface PersistListenerManager {
    <Entity> void onChange(Entity aggregate, PersistType type);
}
