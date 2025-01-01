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
     * @param aggregate
     * @param type
     */
    @Override
    public void onChange(Entity aggregate, PersistType type){
        switch (type){
            case CREATE:
                onCreate(aggregate);
                break;
            case UPDATE:
                onUpdate(aggregate);
                break;
            case DELETE:
                onDelete(aggregate);
                break;
        }
    }

    public abstract void onCreate(Entity entity);

    public abstract void onUpdate(Entity entity);

    public abstract void onDelete(Entity entity);
}
