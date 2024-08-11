package org.netcorepal.cap4j.ddd.domain.event.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 领域事件配置类
 *
 * @author binking338
 * @date 2024/8/11
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.domain.event")
public class EventProperties {
    /**
     * 订阅器扫描包范围
     */
    String subscriberScanPackage = "";

    /**
     * 发布器线程池大小
     */
    int publisherThreadPoolSize = 4;
}
