package org.ddd.example.adapter.domain.repositories;

import org.ddd.example.domain.aggregates.samples.Account;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 */
public interface AccountRepository extends org.ddd.domain.repo.AggregateRepository<Account, Long> {
    // 【自定义代码开始】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动

    @org.springframework.stereotype.Component
    public static class AccountJpaRepositoryAdapter extends org.ddd.domain.repo.AbstractJpaRepository<Account, Long>
    {
        public AccountJpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<Account> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<Account, Long> jpaRepository) {
            super(jpaSpecificationExecutor, jpaRepository);
        }
    }

    // 【自定义代码结束】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动
}
