package org.netcorepal.cap4j.ddd.application.saga.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Saga仓储jpa实现
 *
 * @author binking338
 * @date 2024/10/14
 */
public interface ArchivedSagaJpaRepository extends JpaRepository<ArchivedSaga, Long>, JpaSpecificationExecutor<ArchivedSaga> {
}
