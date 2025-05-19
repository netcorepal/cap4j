package org.netcorepal.cap4j.ddd.application.request.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mediator配置类
 *
 * @author binking338
 * @date 2025/5/15
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.application")
public class RequestProperties {
    /**
     * 请求调度线程池大小
     */
    int requestScheduleThreadPoolSize = 10;
    /**
     * 请求调度线程工厂类名
     */
    String requestScheduleThreadFactoryClassName = null;
}
