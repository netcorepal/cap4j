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
     * 事件扫描包范围
     * 领域事件 & 集成事件
     */
    String eventScanPackage = "";

    /**
     * 发布器线程池大小
     * 用于实现延迟发送
     */
    int publisherThreadPoolSize = 4;
}
