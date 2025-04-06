package org.netcorepal.cap4j.ddd.application.event;

import org.apache.rocketmq.client.consumer.MQPushConsumer;

/**
 * 配置领域事件的MQ配置接口
 *
 * @author binking338
 * @date 2024/3/28
 */
public interface RocketMqIntegrationEventConfigure {
    /**
     *
     * @param integrationEventClass
     * @return
     */
    MQPushConsumer get(Class<?> integrationEventClass);
}
