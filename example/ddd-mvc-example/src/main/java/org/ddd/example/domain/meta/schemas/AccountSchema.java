package org.ddd.example.domain.meta.schemas;

import org.ddd.example.domain.meta.Schema;
import org.ddd.example.domain.aggregates.samples.Account;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 账户 
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
@RequiredArgsConstructor
public class AccountSchema {
    private final Path<Account> root;
    private final CriteriaBuilder criteriaBuilder;

    public CriteriaBuilder criteriaBuilder() {
        return criteriaBuilder;
    }

    public Schema.Field<Long> id() {
        return root == null ? new Schema.Field<>("id") : new Schema.Field<>(root.get("id"));
    }

    /**
     * 账户名称
     * varchar(100)
     */
    public Schema.Field<String> name() {
        return root == null ? new Schema.Field<>("name") : new Schema.Field<>(root.get("name"));
    }

    /**
     * 账户余额
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
    public Predicate spec(Schema.PredicateBuilder<AccountSchema> builder){
        return builder.build(this);
    }

    /**
     * 构建查询条件
     * @param builder
     * @param distinct
     * @return
     */
    public static Specification<Account> specify(Schema.PredicateBuilder<AccountSchema> builder, boolean distinct) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            AccountSchema account = new AccountSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(account));
            criteriaQuery.distinct(distinct);
            return null;
        };
    }
    
    /**
     * 构建查询条件
     * @param builder
     * @return
     */
    public static Specification<Account> specify(Schema.PredicateBuilder<AccountSchema> builder) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            AccountSchema account = new AccountSchema(root, criteriaBuilder);
            criteriaQuery.where(builder.build(account));
            return null;
        };
    }
    
    /**
     * 构建排序
     * @param builders
     * @return
     */
    public static Sort orderBy(Schema.OrderBuilder<AccountSchema>... builders) {
        return orderBy(Arrays.asList(builders));
    }

    /**
     * 构建排序
     *
     * @param builders
     * @return
     */
    public static Sort orderBy(Collection<Schema.OrderBuilder<AccountSchema>> builders) {
        if(CollectionUtils.isEmpty(builders)) {
            return Sort.unsorted();
        }
        return Sort.by(builders.stream()
                .map(builder -> builder.build(new AccountSchema(null, null)))
                .collect(Collectors.toList())
        );
    }

}
