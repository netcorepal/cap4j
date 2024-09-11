package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

/**
 * Jpa仓储检索断言
 *
 * @author binking338
 * @date 2024/9/8
 */
@RequiredArgsConstructor
public class JpaPredicate<Entity> implements Predicate<Entity> {
    final Class<Entity> entityClass;
    final Specification<Entity> spec;
    final Object id;
    final Iterable<Object> ids;

    public static <Entity> Predicate<Entity> byId(Class<Entity> entityClass, Object id) {
        return new JpaPredicate<>(entityClass, null, id, null);
    }

    public static <Entity> Predicate<Entity> byIds(Class<Entity> entityClass, Iterable<Object> ids) {
        return new JpaPredicate<>(entityClass, null, null, ids);
    }

    public static <Entity> Predicate<Entity> bySpecification(Class<Entity> entityClass, Specification<Entity> specification) {
        return new JpaPredicate<>(entityClass, specification, null, null);
    }
}
