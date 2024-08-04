package org.ddd.example.domain.meta;

import com.google.common.collect.Lists;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.Collection;

/**
 * Schema
 *
 * @author <template>
 * @date 
 */
public class Schema {

    /**
     * 断言构建器
     */
    public static interface PredicateBuilder<S> {
        public Predicate build(S schema);
    }

    /**
     * 排序构建器
     */
    public static interface OrderBuilder<S> {
        public Sort.Order build(S schema);
    }

    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }

    /**
     * 字段
     *
     * @param <T>
     */
    public static class Field<T> {
        private String name;
        private SingularAttributePath<T> path;

        public Field(Path<T> path) {
            this.path = new SingularAttributePath<>(((SingularAttributePath<T>) path).criteriaBuilder(), ((SingularAttributePath<T>) path).getJavaType(), ((SingularAttributePath<T>) path).getPathSource(), ((SingularAttributePath<T>) path).getAttribute());
            this.name = this.path.getAttribute().getName();
        }

        public Field(String name) {
            this.name = name;
        }

        protected CriteriaBuilder criteriaBuilder() {
            return path == null ? null : path.criteriaBuilder();
        }

        public Path<T> path(){
            return path;
        }

        public Sort.Order asc() {
            return Sort.Order.asc(this.name);
        }

        public Sort.Order desc() {
            return Sort.Order.desc(this.name);
        }

        public Predicate isTrue() {
            return criteriaBuilder().isTrue((Expression<Boolean>) this.path);
        }

        public Predicate isFalse() {
            return criteriaBuilder().isTrue((Expression<Boolean>) this.path);

        }

        public Predicate equal(Object val) {
            return criteriaBuilder().equal(this.path, val);
        }

        public Predicate equal(Expression<?> val) {
            return criteriaBuilder().equal(this.path, val);
        }

        public Predicate notEqual(Object val) {
            return criteriaBuilder().notEqual(this.path, val);
        }

        public Predicate notEqual(Expression<?> val) {
            return criteriaBuilder().notEqual(this.path, val);
        }

        public Predicate isNull() {
            return criteriaBuilder().isNull(this.path);
        }

        public Predicate isNotNull() {
            return criteriaBuilder().isNotNull(this.path);
        }

        public <Y extends Comparable<? super Y>> Predicate greaterThan(Y val) {
            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> val) {
            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Y val) {
            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> val) {
            return criteriaBuilder().greaterThanOrEqualTo((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate lessThan(Y val) {
            return criteriaBuilder().lessThan((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> val) {
            return criteriaBuilder().lessThan((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Y val) {
            return criteriaBuilder().lessThanOrEqualTo((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> val) {
            return criteriaBuilder().lessThanOrEqualTo((Expression<Y>) this.path, val);
        }

        public <Y extends Comparable<? super Y>> Predicate between(Y val1, Y val2) {
            return criteriaBuilder().between((Expression<Y>) this.path, val1, val2);
        }

        public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> val1, Expression<? extends Y> val2) {
            return criteriaBuilder().between((Expression<Y>) this.path, val1, val2);
        }

        public Predicate in(Object... vals) {
            return in(Lists.newArrayList(vals));
        }

        public Predicate in(Collection<Object> vals) {
            CriteriaBuilder.In predicate = criteriaBuilder().in(this.path);
            for (Object o : vals) {
                predicate.value(o);
            }
            return predicate;
        }

        public Predicate notIn(Object... vals) {
            return notIn(Lists.newArrayList(vals));
        }

        public Predicate notIn(Collection<Object> vals) {
            return criteriaBuilder().not(in(vals));
        }


        public Predicate like(String val) {
            return criteriaBuilder().like((Expression<String>) this.path, val);
        }

        public Predicate like(Expression<String> val) {
            return criteriaBuilder().like((Expression<String>) this.path, val);
        }

        public Predicate notLike(String val) {
            return criteriaBuilder().notLike((Expression<String>) this.path, val);
        }

        public Predicate notLike(Expression<String> val) {
            return criteriaBuilder().notLike((Expression<String>) this.path, val);
        }


        public Predicate eq(Object val) {
            return equal(val);
        }

        public Predicate eq(Expression<?> val) {
            return equal(val);
        }

        public Predicate neq(Object val) {
            return notEqual(val);
        }

        public Predicate neq(Expression<?> val) {
            return notEqual(val);
        }

        public <Y extends Comparable<? super Y>> Predicate gt(Y val) {
            return greaterThan(val);
        }

        public <Y extends Comparable<? super Y>> Predicate gt(Expression<? extends Y> val) {
            return greaterThan(val);
        }

        public <Y extends Comparable<? super Y>> Predicate ge(Y val) {
            return greaterThanOrEqualTo(val);
        }

        public <Y extends Comparable<? super Y>> Predicate ge(Expression<? extends Y> val) {

            return greaterThanOrEqualTo(val);
        }

        public <Y extends Comparable<? super Y>> Predicate lt(Y val) {

            return lessThan(val);
        }

        public <Y extends Comparable<? super Y>> Predicate lt(Expression<? extends Y> val) {
            return lessThan(val);
        }

        public <Y extends Comparable<? super Y>> Predicate le(Y val) {
            return lessThanOrEqualTo(val);
        }

        public <Y extends Comparable<? super Y>> Predicate le(Expression<? extends Y> val) {
            return lessThanOrEqualTo(val);
        }
    }
}

