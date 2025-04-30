package org.netcorepal.cap4j.ddd.domain.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.domain.distributed.configure.SnowflakeProperties;
import org.netcorepal.cap4j.ddd.domain.distributed.snowflake.DefaultSnowflakeWorkerIdDispatcher;
import org.netcorepal.cap4j.ddd.domain.distributed.snowflake.SnowflakeIdGenerator;
import org.netcorepal.cap4j.ddd.domain.distributed.snowflake.SnowflakeWorkerIdDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
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
@ConditionalOnProperty(
        prefix = "cap4j.ddd.distributed.idgenerator.snowflake",
        name = "enable",
        havingValue = "true",
        matchIfMissing = true
)
public class SnowflakeAutoConfiguration {
    private final SnowflakeProperties properties;
    private static final String CONFIG_KEY_4_JPA_SHOW_SQL = "${spring.jpa.show-sql:${spring.jpa.showSql:false}}";


    int pongContinuousErrorCount = 0;
    SnowflakeWorkerIdDispatcher snowflakeWorkerIdDispatcher;

    public void shutdown() {
        snowflakeWorkerIdDispatcher.release();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void pong() {
        if (snowflakeWorkerIdDispatcher.pong()) {
            log.debug("SnowflakeWorkerIdDispatcher 心跳上报成功");
            pongContinuousErrorCount = 0;
        } else {
            log.error("SnowflakeWorkerIdDispatcher 心跳上报失败");
            pongContinuousErrorCount ++;
            if(pongContinuousErrorCount > properties.getMaxPongContinuousErrorCount()){
                snowflakeWorkerIdDispatcher.remind();
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeIdGenerator.class)
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeWorkerIdDispatcher snowflakeWorkerIdDispatcher, SnowflakeProperties properties) {
        long workerId = snowflakeWorkerIdDispatcher.acquire(properties.getWorkerId(), properties.getDatacenterId());
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(workerId % (1 << SnowflakeIdGenerator.workerIdBits), workerId >> 5L);
        SnowflakeIdentifierGenerator.configure(snowflakeIdGenerator);
        return snowflakeIdGenerator;
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeWorkerIdDispatcher.class)
    public DefaultSnowflakeWorkerIdDispatcher defaultSnowflakeWorkerIdDispatcher(
            SnowflakeProperties properties,
            JdbcTemplate jdbcTemplate,
            @Value(CONFIG_KEY_4_JPA_SHOW_SQL)
            boolean showSql) {
//        log.warn("注意！！！默认调度器通过手工配置完成WorkerId、DatacenterId分发，有重复分配风险，请根据项目实际情况自行实现SnowflakeWorkerIdDispatcher。");
        DefaultSnowflakeWorkerIdDispatcher dispatcher = new DefaultSnowflakeWorkerIdDispatcher(
                jdbcTemplate,
                properties.getTable(),
                properties.getFieldDatacenterId(),
                properties.getFieldWorkerId(),
                properties.getFieldDispatchTo(),
                properties.getFieldDispatchAt(),
                properties.getFieldExpireAt(),
                properties.getExpireMinutes(),
                properties.getLocalHostIdentify(),
                showSql
        );
        snowflakeWorkerIdDispatcher = dispatcher;
        return dispatcher;
    }

}
