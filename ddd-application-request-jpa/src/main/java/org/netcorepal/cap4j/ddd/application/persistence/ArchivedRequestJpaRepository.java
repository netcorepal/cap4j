package org.netcorepal.cap4j.ddd.application.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 归档请求实体仓储
 *
 * @author binking338
 * @date 2025/5/16
 */
public interface ArchivedRequestJpaRepository extends JpaRepository<ArchivedRequest, Long>, JpaSpecificationExecutor<ArchivedRequest> {
}
