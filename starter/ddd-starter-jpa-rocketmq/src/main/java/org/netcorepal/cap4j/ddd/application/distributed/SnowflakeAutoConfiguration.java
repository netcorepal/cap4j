package org.netcorepal.cap4j.ddd.application.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.distributed.configure.SnowflakeProperties;
import org.netcorepal.cap4j.ddd.application.distributed.snowflake.DefaultSnowflakeWorkerIdDispatcher;
import org.netcorepal.cap4j.ddd.application.distributed.snowflake.SnowflakeIdGenerator;
import org.netcorepal.cap4j.ddd.application.distributed.snowflake.SnowflakeWorkerIdDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Snowflake自动配置类
 *
 * @author binking338
 * @date 2024/8/10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SnowflakeAutoConfiguration {
    private final SnowflakeProperties properties;

    int pongContinuousErrorCount = 0;
    SnowflakeWorkerIdDispatcher snowflakeWorkerIdDispatcher;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeWorkerIdDispatcher snowflakeWorkerIdDispatcher, SnowflakeProperties properties) {
        long workerId = snowflakeWorkerIdDispatcher.acquire(properties.getWorkerId(), properties.getDatacenterId());
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(workerId % (1 << SnowflakeIdGenerator.workerIdBits), workerId >> 5L);
        SnowflakeIdentifierGenerator.snowflakeIdGenerator = snowflakeIdGenerator;
        return snowflakeIdGenerator;
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeWorkerIdDispatcher.class)
    public DefaultSnowflakeWorkerIdDispatcher defaultSnowflakeWorkerIdDispatcher(SnowflakeProperties properties) {
        log.warn("默认调度器需通过手工配置完成WorkerId、DatacenterId分发，有重复分配风险！！！请根据项目实际情况自行实现SnowflakeWorkerIdDispatcher。");
        DefaultSnowflakeWorkerIdDispatcher dispatcher = new DefaultSnowflakeWorkerIdDispatcher(
                properties.getWorkerId() == null ? 0 : properties.getWorkerId().longValue(),
                properties.getDatacenterId() == null ? 0 : properties.getDatacenterId().longValue()
        );
        snowflakeWorkerIdDispatcher = dispatcher;
        return dispatcher;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void pong() {
        if (snowflakeWorkerIdDispatcher.pong()) {
            log.info("SnowflakeWorkerIdDispatcher 心跳上报成功");
            pongContinuousErrorCount = 0;
        } else {
            log.error("SnowflakeWorkerIdDispatcher 心跳上报失败");
            pongContinuousErrorCount ++;
            if(pongContinuousErrorCount > properties.getMaxPongContinuousErrorCount()){
                snowflakeWorkerIdDispatcher.remind();
            }
        }
    }

    public void shutdown() {
        snowflakeWorkerIdDispatcher.release();
    }
}
