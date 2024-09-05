package org.netcorepal.cap4j.ddd.domain.aggregate.impl;

import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;

/**
 * 默认实体规约抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
public abstract class AbstractSpecification<Entity> implements Specification<Entity> {

    public abstract Result specify(Entity entity);
}
