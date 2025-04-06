package org.netcorepal.cap4j.ddd.domain.distributed.configure;

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
    boolean enable = true;

    String table = "`__worker_id`";
    String fieldDatacenterId = "`datacenter_id`";
    String fieldWorkerId = "`worker_id`";
    String fieldDispatchTo = "`dispatch_to`";
    String fieldDispatchAt = "`dispatch_at`";
    String fieldExpireAt = "`expire_at`";

    Long workerId = null;
    Long datacenterId = null;
    int expireMinutes = 10;
    /**
     * 最大连续错误次数
     */
    int maxPongContinuousErrorCount = 5;
}
