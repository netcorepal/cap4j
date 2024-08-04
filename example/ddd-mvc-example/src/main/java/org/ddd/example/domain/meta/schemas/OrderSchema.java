package org.ddd.example.domain.meta.schemas;

import org.ddd.example.domain.meta.Schema;
import org.ddd.example.domain.aggregates.samples.Order;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 订单  
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
@RequiredArgsConstructor
public class OrderSchema {
    private final Path<Order> root;
    private final CriteriaBuilder criteriaBuilder;

    public CriteriaBuilder criteriaBuilder() {
        return criteriaBuilder;
    }

    public Schema.Field<Long> id() {
        return root == null ? new Schema.Field<>("id") : new Schema.Field<>(root.get("id"));
    }

    /**
     * 订单金额
     * int(11)
     */
    public Schema.Field<Integer> amount() {
        return root == null ? new Schema.Field<>("amount") : new Schema.Field<>(root.get("amount"));
    }

    /**
     * 订单标题
     * varchar(100)
     */
    public Schema.Field<String> name() {
        return root == null ? new Schema.Field<>("name") : new Schema.Field<>(root.get("name"));
    }

    /**
     * 下单人
     * varchar(100)
     */
    public Schema.Field<String> owner() {
        return root == null ? new Schema.Field<>("owner") : new Schema.Field<>(root.get("owner"));
    }

    /**
     * 订单状态
     * 0:INIT:待支付;-1:CLOSE:已关闭;1:FINISH:已完成
     * int(11)
     */
    public Schema.Field<org.ddd.example.domain.aggregates.samples.enums.OrderStatus> status() {
        return root == null ? new Schema.Field<>("status") : new Schema.Field<>(root.get("status"));
    }

    /**
     * 是否完成
     * bit(1)
     */
    public Schema.Field<Boolean> finished() {
        return root == null ? new Schema.Field<>("finished") : new Schema.Field<>(root.get("finished"));
    }

    /**
     * 是否关闭
     * bit(1)
     */
    public Schema.Field<Boolean> closed() {
        return root == null ? new Schema.Field<>("closed") : new Schema.Field<>(root.get("closed"));
    }

    /**
     * datetime
     */
    public Schema.Field<java.util.Date> updateAt() {
        return root == null ? new Schema.Field<>("updateAt") : new Schema.Field<>(root.get("updateAt"));
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
    public Predicate spec(Schema.PredicateBuilder<OrderSchema> builder){
        return builder.build(this);
    }

    /**
     * OrderItem 关联查询条件定义
     *
     * @param joinType
     * @return
     */
    public OrderItemSchema joinOrderItem(Schema.JoinType joinType) {
        JoinType type = transformJoinType(joinType);
        Join<Order, org.ddd.example.domain.aggregates.samples.OrderItem> join = ((Root<Order>) root).join("orderItems", type);
        OrderItemSchema schema = new OrderItemSchema(join, criteriaBuilder);
        return schema;
    }


    private JoinType transformJoinType(Schema.JoinType joinType){
        if(joinType == Schema.JoinType.INNER){
            return JoinType.INNER;
        } else if(joinType == Schema.JoinType.LEFT){
            return JoinType.LEFT;
        } else if(joinType == Schema.JoinType.RIGHT){
            return JoinType.RIGHT;
        }
        return JoinType.LEFT;
    }

    /**
     * 构建查询条件
     * @param builder
     * @param distinct
     * @return
     */
    public static Specification<Order> specify(Schema.PredicateBuilder<OrderSchema> builder, boolean distinct) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            OrderSchema order = new OrderSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(order));
            criteriaQuery.distinct(distinct);
            return null;
        };
    }
    
    /**
     * 构建查询条件
     * @param builder
     * @return
     */
    public static Specification<Order> specify(Schema.PredicateBuilder<OrderSchema> builder) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            OrderSchema order = new OrderSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(order));
            return null;
        };
    }
    
    /**
     * 构建排序
     * @param builders
     * @return
     */
    public static Sort orderBy(Schema.OrderBuilder<OrderSchema>... builders) {
        return orderBy(Arrays.asList(builders));
    }

    /**
     * 构建排序
     *
     * @param builders
     * @return
     */
    public static Sort orderBy(Collection<Schema.OrderBuilder<OrderSchema>> builders) {
        if(CollectionUtils.isEmpty(builders)) {
            return Sort.unsorted();
        }
        return Sort.by(builders.stream()
                .map(builder -> builder.build(new OrderSchema(null, null)))
                .collect(Collectors.toList())
        );
    }

}
