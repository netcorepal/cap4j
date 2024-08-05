package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * @author binking338
 * @date 2024/1/31
 */
public interface PersistListenerManager {
    <Entity> void onChange(Entity entity);

    <Entity> void onCreate(Entity entity);

    <Entity> void onUpdate(Entity entity);

    <Entity> void onDelete(Entity entity);
}
