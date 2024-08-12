package org.netcorepal.cap4j.ddd.application.distributed.snowflake;

import lombok.RequiredArgsConstructor;

/**
 * 默认WorkerId分配器
 *
 * @author binking338
 * @date 2024/8/10
 */
@RequiredArgsConstructor
public class DefaultSnowflakeWorkerIdDispatcher implements SnowflakeWorkerIdDispatcher {
    private final long workerId;
    private final long datacenterId;

    @Override
    public long acquire(Long workerId, Long datacenterId) {
        if(workerId == null){
            workerId = this.workerId;
        }
        if(datacenterId == null){
            datacenterId = this.datacenterId;
        }
        return datacenterId << 5 + workerId;
    }

    @Override
    public void release() {
    }

    @Override
    public boolean pong() {
        return true;
    }

    @Override
    public void remind(){

    }
}
