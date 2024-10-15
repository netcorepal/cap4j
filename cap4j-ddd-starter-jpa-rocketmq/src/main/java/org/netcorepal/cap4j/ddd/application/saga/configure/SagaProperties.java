package org.netcorepal.cap4j.ddd.application.saga.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Saga配置类
 *
 * @author binking338
 * @date 2024/10/15
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.application.saga")
public class SagaProperties {
    /**
     * Saga异步线程池大小
     * 用于实现Saga异步执行
     */
    int asyncThreadPoolSize = 4;
}
