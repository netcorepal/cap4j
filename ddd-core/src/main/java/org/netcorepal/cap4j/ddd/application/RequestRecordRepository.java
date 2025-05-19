package org.netcorepal.cap4j.ddd.application;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RequestRecord仓储
 *
 * @author binking338
 * @date 2025/5/15
 */
public interface RequestRecordRepository {
    /**
     * 创建
     *
     * @return {@link RequestRecord}
     */
    RequestRecord create();

    /**
     * 保存
     *
     * @param requestRecord 请求记录
     */
    void save(RequestRecord requestRecord);

    /**
     * 通过id获取
     *
     * @param id 请求id
     * @return {@link RequestRecord}
     */
    RequestRecord getById(String id);

    /**
     * 通过下次尝试时间获取
     *
     * @param svcName        服务名
     * @param maxNextTryTime 最大下次尝试时间
     * @param limit          获取数量限制
     * @return {@link RequestRecord}列表
     */
    List<RequestRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit);

    /**
     * 批量归档
     *
     * @param svcName     服务名
     * @param maxExpireAt 最大过期时间
     * @param limit       限制
     * @return 删除数量
     */
    int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit);
}
