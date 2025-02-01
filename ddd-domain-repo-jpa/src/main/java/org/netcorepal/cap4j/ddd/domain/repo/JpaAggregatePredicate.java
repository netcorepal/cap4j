package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

/**
 * Jpa聚合检索断言
 *
 * @author binking338
 * @date 2025/1/12
 */
@RequiredArgsConstructor
public class JpaAggregatePredicate<XENTITY extends Aggregate<ENTITY>, ENTITY> implements AggregatePredicate<XENTITY> {
    final Class<XENTITY> xEntityClass;
    final JpaPredicate<ENTITY> jpaPredicate;

    private static <XENTITY extends Aggregate<ENTITY>, ENTITY> Class<ENTITY> getEntityClass(Class<XENTITY> xEntityClass) {
        Class<ENTITY> entityClass = (Class<ENTITY>) ClassUtils.resolveGenericTypeClass(
                xEntityClass, 0,
                Aggregate.class, Aggregate.Default.class);
        return entityClass;
    }

    public static <XENTITY extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<XENTITY> byId(Class<XENTITY> xEntityClass, Object id) {
        return new JpaAggregatePredicate<>(xEntityClass, new JpaPredicate<>(getEntityClass(xEntityClass), null, Collections.singletonList(id), null));
    }

    public static <XENTITY extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<XENTITY> byIds(Class<XENTITY> xEntityClass, Iterable<Object> ids) {
        return new JpaAggregatePredicate<>(xEntityClass, new JpaPredicate<>(getEntityClass(xEntityClass), null, ids, null));
    }

    public static <XENTITY extends Aggregate<VALUE_OBJECT>, VALUE_OBJECT extends ValueObject> AggregatePredicate<XENTITY> byValueObject(XENTITY valueObject) {
        return new JpaAggregatePredicate<>(valueObject.getClass(), new JpaPredicate<>(getEntityClass(valueObject.getClass()), null, Collections.singletonList(valueObject._unwrap().hash()), valueObject._unwrap()));
    }

    public static <XENTITY extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<XENTITY> bySpecification(Class<XENTITY> xEntityClass, Specification<ENTITY> specification) {
        return new JpaAggregatePredicate<>(xEntityClass, new JpaPredicate<>(getEntityClass(xEntityClass), specification, null, null));
    }
}
