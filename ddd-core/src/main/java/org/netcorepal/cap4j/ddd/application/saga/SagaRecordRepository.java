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
    /**
     * 创建SagaRecord
     *
     * @return SagaRecord
     */
    public SagaRecord create();

    /**
     * 保存SagaRecord
     *
     * @param sagaRecord SagaRecord
     */
    public void save(SagaRecord sagaRecord);

    /**
     * 根据id获取SagaRecord
     *
     * @param id id
     * @return SagaRecord
     */
    public SagaRecord getById(String id);

    /**
     * 根据下次执行时间获取SagaRecord
     *
     * @param svcName        服务名
     * @param maxNextTryTime 最大下次执行时间
     * @param limit          限制数量
     * @return SagaRecord列表
     */
    public List<SagaRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit);

    /**
     * 根据过期时间归档SagaRecord
     *
     * @param svcName      服务名
     * @param maxExpireAt  最大过期时间
     * @param limit        限制数量
     * @return 归档数量
     */
    public int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit);
}
