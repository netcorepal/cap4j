package org.netcorepal.cap4j.ddd.domain.event;


/**
 * 配置领域事件的MQ配置接口
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
public interface MQConsumerConfigure {
    /**
     *
     * @param domainEventClass
     * @return
     */
    RabbitMQConsumer get(Class domainEventClass);
}
