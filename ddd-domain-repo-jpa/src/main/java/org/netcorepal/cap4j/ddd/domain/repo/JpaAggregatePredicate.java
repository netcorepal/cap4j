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
public class JpaAggregatePredicate<AGGREGATE extends Aggregate<ENTITY>, ENTITY> implements AggregatePredicate<AGGREGATE> {
    final Class<AGGREGATE> aggregateClass;
    final Predicate<ENTITY> predicate;

    private static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> Class<ENTITY> getEntityClass(Class<AGGREGATE> aggregateClass) {
        Class<ENTITY> entityClass = (Class<ENTITY>) ClassUtils.resolveGenericTypeClass(
                aggregateClass, 0,
                Aggregate.class, Aggregate.Default.class);
        return entityClass;
    }

    public static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<AGGREGATE> byId(Class<AGGREGATE> aggregateClass, Object id) {
        return new JpaAggregatePredicate<>(aggregateClass, new JpaPredicate<>(getEntityClass(aggregateClass), null, Collections.singletonList(id), null));
    }

    public static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<AGGREGATE> byIds(Class<AGGREGATE> aggregateClass, Iterable<Object> ids) {
        return new JpaAggregatePredicate<>(aggregateClass, new JpaPredicate<>(getEntityClass(aggregateClass), null, ids, null));
    }

    public static <AGGREGATE extends Aggregate<VALUE_OBJECT>, VALUE_OBJECT extends ValueObject> AggregatePredicate<AGGREGATE> byValueObject(AGGREGATE valueObject) {
        return new JpaAggregatePredicate<>(valueObject.getClass(), new JpaPredicate<>(getEntityClass(valueObject.getClass()), null, Collections.singletonList(valueObject._unwrap().hash()), valueObject._unwrap()));
    }

    public static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<AGGREGATE> bySpecification(Class<AGGREGATE> aggregateClass, Specification<ENTITY> specification) {
        return new JpaAggregatePredicate<>(aggregateClass, new JpaPredicate<>(getEntityClass(aggregateClass), specification, null, null));
    }

    public static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> AggregatePredicate<AGGREGATE> byPredicate(Class<AGGREGATE> aggregateClass, Predicate<ENTITY> predicate) {
        return new JpaAggregatePredicate<>(aggregateClass, predicate);
    }
}
