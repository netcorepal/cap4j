package org.netcorepal.cap4j.ddd.application.event.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * http集成事件适配器配置
 *
 * @author binking338
 * @date 2025/5/21
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.integration.event.http")
public class HttpIntegrationEventAdapterProperties {
    /**
     * 异步发送线程池大小
     */
    int publishThreadPoolSize = 4;
    /**
     * 异步发送线程工厂类名
     */
    String publishThreadFactoryClassName = null;
}
