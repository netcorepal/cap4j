package org.netcorepal.cap4j.ddd.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author binking338
 * @date 2024/4/17
 */
@NoRepositoryBean
public interface AggregateRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}