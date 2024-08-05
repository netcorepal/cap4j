package org.netcorepal.cap4j.ddd.domain.event;

import org.apache.rocketmq.client.consumer.MQPushConsumer;

/**
 * 配置领域事件的MQ配置接口
 *
 * @author binking338
 * @date 2024/3/28
 */
public interface MQConsumerConfigure {
    /**
     *
     * @param domainEventClass
     * @return
     */
    MQPushConsumer get(Class domainEventClass);
}
