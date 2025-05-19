package org.netcorepal.cap4j.ddd.application.event.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMq集成事件适配器配置
 *
 * @author binking338
 * @date 2025/4/4
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.integration.event.rabbitmq")
public class RabbitMqIntegrationEventAdapterProperties {
    /**
     * 异步发送线程池大小
     */
    int publishThreadPoolSize = 4;
    /**
     * 异步发送线程工厂类名
     */
    String publishThreadFactoryClassName = null;
    /**
     * 是否自动声明交换机
     */
    boolean autoDeclareExchange = true;
    /**
     * 是否自动声明队列
     */
    boolean autoDeclareQueue = true;
    /**
     * 默认交换机类型
     */
    String defaultExchangeType = "direct";
}
