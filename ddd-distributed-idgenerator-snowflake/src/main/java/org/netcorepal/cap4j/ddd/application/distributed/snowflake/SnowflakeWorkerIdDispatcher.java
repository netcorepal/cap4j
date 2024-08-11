package org.netcorepal.cap4j.ddd.application.distributed.snowflake;

/**
 * WorkerId调度器
 *
 * @author binking338
 * @date 2024/8/10
 */
public interface SnowflakeWorkerIdDispatcher {
    /**
     * 获取
     * @param workerId 指定workerId
     * @param datacenterId 指定datacenterId
     * @return
     */
    long acquire(Long workerId, Long datacenterId);

    /**
     * 释放占用
     * @return
     */
    void release();

    /**
     * 心跳上报
     * @return
     */
    boolean pong();
}
