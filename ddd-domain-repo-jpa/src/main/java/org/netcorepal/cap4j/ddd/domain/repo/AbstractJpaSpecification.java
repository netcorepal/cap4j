package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.ClassUtils;

/**
 * 基于Jpa的实体规约抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class AbstractJpaSpecification<Entity> implements Specification<Entity> {
    public Class<Entity> forEntityClass(){
        return ((Class<Entity>) ClassUtils.findMethod(
                this.getClass(),
                "specify",
                m -> m.getParameterCount() == 1).getParameters()[0].getType());
    }

    public boolean forceBeforeTransaction(){
        return false;
    }

    public abstract Result specify(Entity entity);
}
