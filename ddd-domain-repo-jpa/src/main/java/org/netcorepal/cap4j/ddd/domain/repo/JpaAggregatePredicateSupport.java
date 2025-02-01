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
     * @return
     * @param <XENTITY>
     * @param <ENTITY>
     */
    public static <XENTITY extends Aggregate<ENTITY>, ENTITY> Predicate<ENTITY> getPredicate(AggregatePredicate<XENTITY> predicate) {
        return ((JpaAggregatePredicate<XENTITY, ENTITY>) predicate).jpaPredicate;
    }


    /**
     * 获取断言聚合类型
     *
     * @param predicate
     * @return
     * @param <XENTITY>
     */
    public static <XENTITY extends Aggregate<?>> Class<XENTITY> reflectXEntityClass(AggregatePredicate<XENTITY> predicate) {
        return ((JpaAggregatePredicate<XENTITY, ?>) predicate).xEntityClass;
    }
}
