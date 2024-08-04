package org.ddd.example.domain.meta.schemas;

import org.ddd.example.domain.meta.Schema;
import org.ddd.example.domain.aggregates.samples.OrderItem;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 订单项 
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
@RequiredArgsConstructor
public class OrderItemSchema {
    private final Path<OrderItem> root;
    private final CriteriaBuilder criteriaBuilder;

    public CriteriaBuilder criteriaBuilder() {
        return criteriaBuilder;
    }

    public Schema.Field<Long> id() {
        return root == null ? new Schema.Field<>("id") : new Schema.Field<>(root.get("id"));
    }

    /**
     * 订单项名称
     * varchar(100)
     */
    public Schema.Field<String> name() {
        return root == null ? new Schema.Field<>("name") : new Schema.Field<>(root.get("name"));
    }

    /**
     * 单价
     * int(11)
     */
    public Schema.Field<Integer> price() {
        return root == null ? new Schema.Field<>("price") : new Schema.Field<>(root.get("price"));
    }

    /**
     * 数量
     * int(11)
     */
    public Schema.Field<Integer> num() {
        return root == null ? new Schema.Field<>("num") : new Schema.Field<>(root.get("num"));
    }

    /**
     * 满足所有条件
     * @param restrictions
     * @return
     */
    public Predicate all(Predicate... restrictions) {
        return criteriaBuilder().and(restrictions);
    }

    /**
     * 满足任一条件
     * @param restrictions
     * @return
     */
    public Predicate any(Predicate... restrictions) {
        return criteriaBuilder().or(restrictions);
    }

    /**
     * 指定条件
     * @param builder
     * @return
     */
    public Predicate spec(Schema.PredicateBuilder<OrderItemSchema> builder){
        return builder.build(this);
    }

    /**
     * 构建查询条件
     * @param builder
     * @param distinct
     * @return
     */
    public static Specification<OrderItem> specify(Schema.PredicateBuilder<OrderItemSchema> builder, boolean distinct) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            OrderItemSchema orderItem = new OrderItemSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(orderItem));
            criteriaQuery.distinct(distinct);
            return null;
        };
    }
    
    /**
     * 构建查询条件
     * @param builder
     * @return
     */
    public static Specification<OrderItem> specify(Schema.PredicateBuilder<OrderItemSchema> builder) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            OrderItemSchema orderItem = new OrderItemSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(orderItem));
            return null;
        };
    }
    
    /**
     * 构建排序
     * @param builders
     * @return
     */
    public static Sort orderBy(Schema.OrderBuilder<OrderItemSchema>... builders) {
        return orderBy(Arrays.asList(builders));
    }

    /**
     * 构建排序
     *
     * @param builders
     * @return
     */
    public static Sort orderBy(Collection<Schema.OrderBuilder<OrderItemSchema>> builders) {
        if(CollectionUtils.isEmpty(builders)) {
            return Sort.unsorted();
        }
        return Sort.by(builders.stream()
                .map(builder -> builder.build(new OrderItemSchema(null, null)))
                .collect(Collectors.toList())
        );
    }

}
