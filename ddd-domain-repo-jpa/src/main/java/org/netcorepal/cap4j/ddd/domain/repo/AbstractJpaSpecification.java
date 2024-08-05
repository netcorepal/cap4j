package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 基于Jpa的实体规约抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class AbstractJpaSpecification<Entity> implements Specification<Entity> {
    public abstract Class<Entity> forEntityClass();
    public boolean forceBeforeTransaction(){
        return false;
    }
    public abstract Result specify(Entity entity);
}
