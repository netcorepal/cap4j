package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;

/**
 * Jpa聚合检索断言Support
 *
 * @author binking338
 * @date 2025/1/12
 */
public class JpaAggregatePredicateSupport {

    /**
     * 获取实体仓储检索断言
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    public static <AGGREGATE extends Aggregate<ENTITY>, ENTITY> Predicate<ENTITY> getPredicate(AggregatePredicate<AGGREGATE, ENTITY> predicate) {
        return ((JpaAggregatePredicate<AGGREGATE, ENTITY>) predicate).predicate;
    }


    /**
     * 获取断言聚合类型
     *
     * @param predicate
     * @param <AGGREGATE>
     * @return
     */
    public static <AGGREGATE extends Aggregate<?>> Class<AGGREGATE> reflectAggregateClass(AggregatePredicate<AGGREGATE, ?> predicate) {
        return ((JpaAggregatePredicate<AGGREGATE, ?>) predicate).aggregateClass;
    }
}
