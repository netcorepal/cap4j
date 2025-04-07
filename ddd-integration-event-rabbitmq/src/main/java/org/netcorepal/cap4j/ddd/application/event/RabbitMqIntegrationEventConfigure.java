package org.netcorepal.cap4j.ddd.application.event;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * 配置领域事件的MQ配置接口
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
public interface RabbitMqIntegrationEventConfigure {
    /**
     * @param integrationEventClass
     * @return
     */
    SimpleMessageListenerContainer get(Class<?> integrationEventClass);
}
