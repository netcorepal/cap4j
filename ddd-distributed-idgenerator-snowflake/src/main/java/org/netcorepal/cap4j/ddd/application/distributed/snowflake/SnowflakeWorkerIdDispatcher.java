package org.netcorepal.cap4j.ddd.application.distributed.snowflake;

/**
 * WorkerId调度器
 *
 * @author binking338
 * @date 2024/8/10
 */
public interface SnowflakeWorkerIdDispatcher {
    /**
     * 获取WorkerId占用
     * @param workerId 指定workerId
     * @param datacenterId 指定datacenterId
     * @return
     */
    long acquire(Long workerId, Long datacenterId);

    /**
     * 释放WorkerId占用
     * @return
     */
    void release();

    /**
     * 心跳上报
     * 如果长期失联，需通知运维介入
     * @return
     */
    boolean pong();

    /**
     * 心跳失败累计到一定次数，提醒运维或相关人员，以便介入处理
     */
    void remind();
}
