package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.repo.PersistListener;
import org.netcorepal.cap4j.ddd.domain.repo.PersistType;

/**
 * 默认实体持久化监听抽象类
 *
 * @author binking338
 * @date 2024/3/9
 */
@RequiredArgsConstructor
public abstract class AbstractPersistListener<Entity> implements PersistListener<Entity> {

    /**
     * 持久化变更
     *
     * @param entity
     * @param type
     */
    @Override
    public abstract void onChange(Entity entity, PersistType type);
}
