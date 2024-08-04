package org.ddd.example.adapter.domain.repositories;

import org.ddd.example.domain.aggregates.samples.Order;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 */
public interface OrderRepository extends org.ddd.domain.repo.AggregateRepository<Order, Long> {
    // 【自定义代码开始】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动

    @org.springframework.stereotype.Component
    public static class OrderJpaRepositoryAdapter extends org.ddd.domain.repo.AbstractJpaRepository<Order, Long>
    {
        public OrderJpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<Order> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<Order, Long> jpaRepository) {
            super(jpaSpecificationExecutor, jpaRepository);
        }
    }

    // 【自定义代码结束】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动
}
