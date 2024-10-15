package org.netcorepal.cap4j.ddd.application.saga;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SagaRecord仓储
 *
 * @author binking338
 * @date 2024/10/12
 */
public interface SagaRecordRepository {
    public SagaRecord create();
    public void save(SagaRecord sagaRecord);
    public SagaRecord getById(String id);
    public List<SagaRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit);
    public int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit);
}
