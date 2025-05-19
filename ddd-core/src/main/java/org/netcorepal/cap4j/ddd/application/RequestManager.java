package org.netcorepal.cap4j.ddd.application;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 请求管理器
 *
 * @author binking338
 * @date 2025/5/17
 */
public interface RequestManager {

    /**
     * 获取请求管理器
     *
     * @return 请求管理器
     */
    static RequestManager getInstance() {
        return RequestSupervisorSupport.requestManager;
    }

    /**
     * 重新执行Saga流程
     *
     * @param request
     * @return
     */
    void resume(RequestRecord request);

    /**
     * 获取指定时间前需重试的请求
     *
     * @param maxNextTryTime 指定时间
     * @param limit          限制数量
     * @return
     */
    List<RequestRecord> getByNextTryTime(LocalDateTime maxNextTryTime, int limit);

    /**
     * 归档指定时间前需重试的请求
     *
     * @param maxExpireAt   指定时间
     * @param limit         限制数量
     * @return
     */
    int archiveByExpireAt(LocalDateTime maxExpireAt, int limit);
}
