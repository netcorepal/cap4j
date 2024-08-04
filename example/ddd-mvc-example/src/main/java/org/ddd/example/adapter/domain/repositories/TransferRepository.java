package org.ddd.example.adapter.domain.repositories;

import org.ddd.example.domain.aggregates.samples.Transfer;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 */
public interface TransferRepository extends org.ddd.domain.repo.AggregateRepository<Transfer, Long> {
    // 【自定义代码开始】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动

    @org.springframework.stereotype.Component
    public static class TransferJpaRepositoryAdapter extends org.ddd.domain.repo.AbstractJpaRepository<Transfer, Long>
    {
        public TransferJpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<Transfer> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<Transfer, Long> jpaRepository) {
            super(jpaSpecificationExecutor, jpaRepository);
        }
    }

    // 【自定义代码结束】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动
}
