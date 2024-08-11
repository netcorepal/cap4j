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
        DefaultSnowflakeWorkerIdDispatcher dispatcher = new DefaultSnowflakeWorkerIdDispatcher(
                properties.getWorkerId() == null ? 0 : properties.getWorkerId().longValue(),
                properties.getDatacenterId() == null ? 0 : properties.getDatacenterId().longValue()
        );
        snowflakeWorkerIdDispatcher = dispatcher;
        return dispatcher;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void pong() {
        if (!snowflakeWorkerIdDispatcher.pong()) {
            log.error("SnowflakeWorkerIdDispatcher 心跳上报失败");
        }
    }

    public void shutdown() {
        snowflakeWorkerIdDispatcher.release();
    }
}
