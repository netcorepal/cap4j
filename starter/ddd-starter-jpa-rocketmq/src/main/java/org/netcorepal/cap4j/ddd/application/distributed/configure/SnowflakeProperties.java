package org.netcorepal.cap4j.ddd.application.distributed.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Snowflake配置类
 *
 * @author qiaohe
 * @date 2024/8/12
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.distributed.idgenerator.snowflake")
public class SnowflakeProperties {
    Long workerId = null;
    Long datacenterId = null;
    int maxPongContinuousErrorCount = 10;
}
