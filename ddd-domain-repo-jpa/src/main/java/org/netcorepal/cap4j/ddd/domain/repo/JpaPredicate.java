package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

/**
 * Jpa仓储检索断言
 *
 * @author binking338
 * @date 2024/9/8
 */
@RequiredArgsConstructor
public class JpaPredicate<ENTITY> implements Predicate<ENTITY> {
    final Class<ENTITY> entityClass;
    final Specification<ENTITY> spec;
    final Iterable<Object> ids;
    final ValueObject valueObject;

    public <AGGREGATE extends Aggregate<ENTITY>> AggregatePredicate<AGGREGATE> toAggregatePredicate(Class<AGGREGATE> aggregateClass) {
        return JpaAggregatePredicate.byPredicate(aggregateClass, this);
    }

    public static <ENTITY> JpaPredicate<ENTITY> byId(Class<ENTITY> entityClass, Object id) {
        return new JpaPredicate<>(entityClass, null, Collections.singletonList(id), null);
    }

    public static <ENTITY> JpaPredicate<ENTITY> byIds(Class<ENTITY> entityClass, Iterable<Object> ids) {
        return new JpaPredicate<>(entityClass, null, ids, null);
    }

    public static <VALUE_OBJECT extends ValueObject<?>> JpaPredicate<VALUE_OBJECT> byValueObject(VALUE_OBJECT valueObject){
        return new JpaPredicate<>((Class<VALUE_OBJECT>) valueObject.getClass(), null, Collections.singletonList(valueObject.hash()), valueObject);
    }

    public static <ENTITY> JpaPredicate<ENTITY> bySpecification(Class<ENTITY> entityClass, Specification<ENTITY> specification) {
        return new JpaPredicate<>(entityClass, specification, null, null);
    }
}
