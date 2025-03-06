package org.netcorepal.cap4j.ddd.domain.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 自动监听集成事件对应的RabbitMQ
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqDomainEventSubscriberAdapter {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final MQConsumerConfigure mqConsumerConfigure;
    private final ConnectionFactory connectionFactory;
    private final Environment environment;
    private final String scanPath;
    private final String applicationName;
    private final String msgCharset;

    List<RabbitMQConsumer> mqPushConsumers = new ArrayList<>();

    public void init() {
        Set<Class<?>> classes = ScanUtils.findIntegrationEventClasses(scanPath);
        classes.stream().filter(cls -> {
            IntegrationEvent integrationEvent = cls.getAnnotation(IntegrationEvent.class);
            return !Objects.isNull(integrationEvent) && StringUtils.isNotEmpty(integrationEvent.value())
                    & !IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber());
        }).forEach(domainEventClass -> {
            RabbitMQConsumer mqPushConsumer = mqConsumerConfigure == null ? null : mqConsumerConfigure.get(domainEventClass);
            if (mqPushConsumer == null) {
                mqPushConsumer = createDefaultConsumer(domainEventClass);
            }
            try {
                if (mqPushConsumer != null) {
                    mqPushConsumer.start();
                    mqPushConsumers.add(mqPushConsumer);
                }
            } catch (AmqpException e) {
                log.error("集成事件消息监听启动失败", e);
            }
        });
    }

    private List<EventMessageInterceptor> orderedEventMessageInterceptors = null;

    /**
     * 获取排序后的事件消息拦截器
     * 基于{@link org.springframework.core.annotation.Order}
     *
     * @return
     */
    private List<EventMessageInterceptor> getOrderedEventMessageInterceptors() {
        if (orderedEventMessageInterceptors == null) {
            orderedEventMessageInterceptors = new ArrayList<>(eventMessageInterceptors);
            orderedEventMessageInterceptors.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
        }
        return orderedEventMessageInterceptors;
    }

    public void shutdown() {
        log.info("集成事件消息监听退出...");
        if (mqPushConsumers == null || mqPushConsumers.isEmpty()) {
            return;
        }
        mqPushConsumers.forEach(mqPushConsumer -> {
            try {
                if (mqPushConsumer != null) {
                    mqPushConsumer.shutdown();
                }
            } catch (Exception ex) {
                log.error("集成事件消息监听退出异常", ex);
            }
        });
    }

    public RabbitMQConsumer createDefaultConsumer(Class<?> integrationEventClass) {
        IntegrationEvent integrationEvent = integrationEventClass.getAnnotation(IntegrationEvent.class);
        if (Objects.isNull(integrationEvent) || StringUtils.isBlank(integrationEvent.value())
                || IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber())) {
            // 不是集成事件, 或显式标明无订阅
            return null;
        }
        String target = integrationEvent.value();
        target = TextUtils.resolvePlaceholderWithCache(target, environment);
        String topic = target.lastIndexOf(':') > 0 ? target.substring(0, target.lastIndexOf(':')) : target;
        RabbitMQConsumer mqPushConsumer = new RabbitMQConsumer(this.connectionFactory);
        try {
            mqPushConsumer.subscribe(topic);
            mqPushConsumer.registerMessageListener((msg) -> {
                String strMsg = new String(msg, Charset.forName(msgCharset));
                Object eventPayload = JSON.parseObject(strMsg, integrationEventClass, Feature.SupportNonPublicField);
                if (getOrderedEventMessageInterceptors().isEmpty()) {
                    eventSubscriberManager.dispatch(eventPayload);
                } else {
                    Message<Object> message = new GenericMessage<>(eventPayload);
                    getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.preSubscribe(message));
                    // 拦截器可能修改消息，重新赋值
                    eventPayload = message.getPayload();
                    eventSubscriberManager.dispatch(eventPayload);
                    getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postSubscribe(message));
                }
            });
        } catch (RuntimeException e) {
            log.error("集成事件消息监听订阅失败", e);
        }
        return mqPushConsumer;
    }
}
