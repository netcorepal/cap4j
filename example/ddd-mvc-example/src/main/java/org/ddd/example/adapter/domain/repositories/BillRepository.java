package org.ddd.example.adapter.domain.repositories;

import org.ddd.example.domain.aggregates.samples.Bill;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 */
public interface BillRepository extends org.ddd.domain.repo.AggregateRepository<Bill, Long> {
    // 【自定义代码开始】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动

    @org.springframework.stereotype.Component
    public static class BillJpaRepositoryAdapter extends org.ddd.domain.repo.AbstractJpaRepository<Bill, Long>
    {
        public BillJpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<Bill> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<Bill, Long> jpaRepository) {
            super(jpaSpecificationExecutor, jpaRepository);
        }
    }

    // 【自定义代码结束】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动
}
