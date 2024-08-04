package org.ddd.example.domain.meta.schemas;

import org.ddd.example.domain.meta.Schema;
import org.ddd.example.domain.aggregates.samples.Transfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 转账记录 
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
@RequiredArgsConstructor
public class TransferSchema {
    private final Path<Transfer> root;
    private final CriteriaBuilder criteriaBuilder;

    public CriteriaBuilder criteriaBuilder() {
        return criteriaBuilder;
    }

    public Schema.Field<Long> id() {
        return root == null ? new Schema.Field<>("id") : new Schema.Field<>(root.get("id"));
    }

    /**
     * 关联账户
     * bigint(100)
     */
    public Schema.Field<Long> accountId() {
        return root == null ? new Schema.Field<>("accountId") : new Schema.Field<>(root.get("accountId"));
    }

    /**
     * 时间
     * datetime
     */
    public Schema.Field<java.util.Date> time() {
        return root == null ? new Schema.Field<>("time") : new Schema.Field<>(root.get("time"));
    }

    /**
     * 业务类型
     * int(11)
     */
    public Schema.Field<Integer> bizType() {
        return root == null ? new Schema.Field<>("bizType") : new Schema.Field<>(root.get("bizType"));
    }

    /**
     * 业务编码
     * varchar(20)
     */
    public Schema.Field<String> bizId() {
        return root == null ? new Schema.Field<>("bizId") : new Schema.Field<>(root.get("bizId"));
    }

    /**
     * 转账金额
     * int(11)
     */
    public Schema.Field<Integer> amount() {
        return root == null ? new Schema.Field<>("amount") : new Schema.Field<>(root.get("amount"));
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
    public Predicate spec(Schema.PredicateBuilder<TransferSchema> builder){
        return builder.build(this);
    }

    /**
     * 构建查询条件
     * @param builder
     * @param distinct
     * @return
     */
    public static Specification<Transfer> specify(Schema.PredicateBuilder<TransferSchema> builder, boolean distinct) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            TransferSchema transfer = new TransferSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(transfer));
            criteriaQuery.distinct(distinct);
            return null;
        };
    }
    
    /**
     * 构建查询条件
     * @param builder
     * @return
     */
    public static Specification<Transfer> specify(Schema.PredicateBuilder<TransferSchema> builder) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            TransferSchema transfer = new TransferSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(transfer));
            return null;
        };
    }
    
    /**
     * 构建排序
     * @param builders
     * @return
     */
    public static Sort orderBy(Schema.OrderBuilder<TransferSchema>... builders) {
        return orderBy(Arrays.asList(builders));
    }

    /**
     * 构建排序
     *
     * @param builders
     * @return
     */
    public static Sort orderBy(Collection<Schema.OrderBuilder<TransferSchema>> builders) {
        if(CollectionUtils.isEmpty(builders)) {
            return Sort.unsorted();
        }
        return Sort.by(builders.stream()
                .map(builder -> builder.build(new TransferSchema(null, null)))
                .collect(Collectors.toList())
        );
    }

}
