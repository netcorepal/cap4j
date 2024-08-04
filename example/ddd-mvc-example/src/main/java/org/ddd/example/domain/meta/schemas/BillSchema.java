package org.ddd.example.domain.meta.schemas;

import org.ddd.example.domain.meta.Schema;
import org.ddd.example.domain.aggregates.samples.Bill;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 账单 
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
@RequiredArgsConstructor
public class BillSchema {
    private final Path<Bill> root;
    private final CriteriaBuilder criteriaBuilder;

    public CriteriaBuilder criteriaBuilder() {
        return criteriaBuilder;
    }

    public Schema.Field<Long> id() {
        return root == null ? new Schema.Field<>("id") : new Schema.Field<>(root.get("id"));
    }

    /**
     * bigint(20)
     */
    public Schema.Field<Long> orderId() {
        return root == null ? new Schema.Field<>("orderId") : new Schema.Field<>(root.get("orderId"));
    }

    /**
     * 账单名称
     * varchar(100)
     */
    public Schema.Field<String> name() {
        return root == null ? new Schema.Field<>("name") : new Schema.Field<>(root.get("name"));
    }

    /**
     * 支付人
     * varchar(100)
     */
    public Schema.Field<String> owner() {
        return root == null ? new Schema.Field<>("owner") : new Schema.Field<>(root.get("owner"));
    }

    /**
     * 账单金额
     * int(11)
     */
    public Schema.Field<Integer> amount() {
        return root == null ? new Schema.Field<>("amount") : new Schema.Field<>(root.get("amount"));
    }

    /**
     * 是否支付
     * bit(1)
     */
    public Schema.Field<Boolean> payed() {
        return root == null ? new Schema.Field<>("payed") : new Schema.Field<>(root.get("payed"));
    }

    /**
     * 是否关闭
     * bit(1)
     */
    public Schema.Field<Boolean> closed() {
        return root == null ? new Schema.Field<>("closed") : new Schema.Field<>(root.get("closed"));
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
    public Predicate spec(Schema.PredicateBuilder<BillSchema> builder){
        return builder.build(this);
    }

    /**
     * 构建查询条件
     * @param builder
     * @param distinct
     * @return
     */
    public static Specification<Bill> specify(Schema.PredicateBuilder<BillSchema> builder, boolean distinct) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            BillSchema bill = new BillSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(bill));
            criteriaQuery.distinct(distinct);
            return null;
        };
    }
    
    /**
     * 构建查询条件
     * @param builder
     * @return
     */
    public static Specification<Bill> specify(Schema.PredicateBuilder<BillSchema> builder) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            BillSchema bill = new BillSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(bill));
            return null;
        };
    }
    
    /**
     * 构建排序
     * @param builders
     * @return
     */
    public static Sort orderBy(Schema.OrderBuilder<BillSchema>... builders) {
        return orderBy(Arrays.asList(builders));
    }

    /**
     * 构建排序
     *
     * @param builders
     * @return
     */
    public static Sort orderBy(Collection<Schema.OrderBuilder<BillSchema>> builders) {
        if(CollectionUtils.isEmpty(builders)) {
            return Sort.unsorted();
        }
        return Sort.by(builders.stream()
                .map(builder -> builder.build(new BillSchema(null, null)))
                .collect(Collectors.toList())
        );
    }

}
