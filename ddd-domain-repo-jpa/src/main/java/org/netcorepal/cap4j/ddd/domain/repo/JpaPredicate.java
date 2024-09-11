package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;

/**
 * Jpa仓储检索断言
 *
 * @author binking338
 * @date 2024/9/8
 */
public class JpaPredicate<Entity> implements Predicate<Entity> {
    private Class<Entity> entityClass;
    private Specification<Entity> spec;

    protected JpaPredicate(Class<Entity> entityClass, Specification<Entity> spec) {
        this.entityClass = entityClass;
        this.spec = spec;
    }

    public static <Entity> Predicate<Entity> from(Class<Entity> entityClass, Specification<Entity> specification) {
        return new JpaPredicate<>(entityClass, specification);
    }

    public static <Entity> Specification<Entity> resume(Predicate<Entity> predicate){
        return ((JpaPredicate<Entity>) predicate).spec;
    }

    public static <Entity> Class<Entity> reflectEntityClass(Predicate<Entity> predicate) {
        return  ((JpaPredicate<Entity>) predicate).entityClass;
    }
}
