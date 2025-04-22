package org.netcorepal.cap4j.ddd.domain.repo.querydsl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.repo.AggregatePredicate;
import org.netcorepal.cap4j.ddd.domain.repo.JpaAggregatePredicate;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryDsl查询条件
 *
 * @author binking338
 * @date 2025/3/29
 */
@RequiredArgsConstructor
public class QuerydslPredicate<ENTITY> implements org.netcorepal.cap4j.ddd.domain.repo.Predicate<ENTITY> {
    final Class<ENTITY> entityClass;
    final BooleanBuilder predicate;
    final List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    public <AGGREGATE extends Aggregate<ENTITY>> AggregatePredicate<AGGREGATE, ENTITY> toAggregatePredicate(Class<AGGREGATE> aggregateClass) {
        return JpaAggregatePredicate.byPredicate(aggregateClass, this);
    }

    public QuerydslPredicate<ENTITY> where(Predicate filter) {
        predicate.and(filter);
        return this;
    }

    public QuerydslPredicate<ENTITY> orderBy(OrderSpecifier<?>... orderSpecifiers) {
        for (OrderSpecifier<?> orderSpecify :
                orderSpecifiers) {
            this.orderSpecifiers.add(orderSpecify);
        }
        return this;
    }

    public static <ENTITY> QuerydslPredicate<ENTITY> of(Class<ENTITY> entityClass) {
        return new QuerydslPredicate<>(entityClass, new BooleanBuilder());
    }

    public static <ENTITY> QuerydslPredicate<ENTITY> byPredicate(Class<ENTITY> entityClass, Predicate predicate) {
        return new QuerydslPredicate<>(entityClass, new BooleanBuilder(predicate));
    }
}
