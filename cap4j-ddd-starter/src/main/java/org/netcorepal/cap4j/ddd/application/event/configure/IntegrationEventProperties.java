package org.netcorepal.cap4j.ddd.application.event.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 集成事件
 *
 * @author binking338
 * @date 2025/4/6
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.integration.event")
public class IntegrationEventProperties {
    /**
     * 集成事件适配类型
     */
    AdapterType adapterType = AdapterType.rocketmq;

    public static enum AdapterType {
        rocketmq,
        rabbitmq,
        ;
    }
}
