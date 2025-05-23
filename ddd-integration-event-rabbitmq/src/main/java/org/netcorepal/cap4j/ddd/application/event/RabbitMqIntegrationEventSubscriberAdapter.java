package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.EventMessageInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.*;

/**
 * 自动监听集成事件对应的RabbitMQ
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqIntegrationEventSubscriberAdapter {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final RabbitMqIntegrationEventConfigure rabbitMqIntegrationEventConfigure;
    private final SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;
    private final ConnectionFactory connectionFactory;
    private final Environment environment;
    private final String scanPath;
    private final String applicationName;
    private final String msgCharset;
    private final boolean autoDeclareQueue;

    List<SimpleMessageListenerContainer> simpleMessageListenerContainers = new ArrayList<>();

    public void init() {
        Set<Class<?>> classes = ScanUtils.findIntegrationEventClasses(scanPath);
        classes.stream().filter(cls -> {
            IntegrationEvent integrationEvent = cls.getAnnotation(IntegrationEvent.class);
            return !Objects.isNull(integrationEvent) && StringUtils.isNotEmpty(integrationEvent.value())
                    & !IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber());
        }).forEach(integrationEventClass -> {
            SimpleMessageListenerContainer simpleMessageListenerContainer = rabbitMqIntegrationEventConfigure == null ? null : rabbitMqIntegrationEventConfigure.get(integrationEventClass);
            if (simpleMessageListenerContainer == null) {
                simpleMessageListenerContainer = createDefaultConsumer(integrationEventClass);
            }
            try {
                if (simpleMessageListenerContainer != null) {
                    simpleMessageListenerContainer.setMessageListener((ChannelAwareMessageListener) (message, channel) -> onMessage(integrationEventClass, message, channel));
                    simpleMessageListenerContainer.start();
                    simpleMessageListenerContainers.add(simpleMessageListenerContainer);
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
        if (simpleMessageListenerContainers == null || simpleMessageListenerContainers.isEmpty()) {
            return;
        }
        simpleMessageListenerContainers.forEach(mqPushConsumer -> {
            try {
                if (mqPushConsumer != null) {
                    mqPushConsumer.shutdown();
                }
            } catch (Exception ex) {
                log.error("集成事件消息监听退出异常", ex);
            }
        });
    }

    public SimpleMessageListenerContainer createDefaultConsumer(Class<?> integrationEventClass) {
        IntegrationEvent integrationEvent = integrationEventClass.getAnnotation(IntegrationEvent.class);
        if (Objects.isNull(integrationEvent) || StringUtils.isBlank(integrationEvent.value())
                || IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber())) {
            // 不是集成事件, 或显式标明无订阅
            return null;
        }
        String target = TextUtils.resolvePlaceholderWithCache(integrationEvent.value(), environment);
        String subscriber = TextUtils.resolvePlaceholderWithCache(integrationEvent.subscriber(), environment);
        String exchange = target.lastIndexOf(':') > 0 ? target.substring(0, target.lastIndexOf(':')) : target;
        String routingKey = target.lastIndexOf(':') > 0 ? target.substring(target.lastIndexOf(':') + 1) : "";
        String queue = getExchangeConsumerQueueName(exchange, subscriber);
        if(autoDeclareQueue){
            tryDeclareQueue(queue, exchange, routingKey);
        }
        SimpleMessageListenerContainer listenerContainer = rabbitListenerContainerFactory.createListenerContainer();
        listenerContainer.setQueueNames(queue);
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return listenerContainer;
    }

    private void onMessage(Class<?> integrationEventClass, org.springframework.amqp.core.Message msg, Channel channel) throws Exception {
        try {
            log.info(String.format("集成事件消费，messageId=%s", msg.getMessageProperties().getMessageId()));
            String strMsg = new String(msg.getBody(), msgCharset);
            Object eventPayload = JSON.parseObject(strMsg, integrationEventClass, Feature.SupportNonPublicField);

            if (getOrderedEventMessageInterceptors().isEmpty()) {
                eventSubscriberManager.dispatch(eventPayload);
            } else {
                Message<Object> message = new GenericMessage<>(eventPayload, new EventMessageInterceptor.ModifiableMessageHeaders(msg.getMessageProperties().getHeaders()));
                getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.preSubscribe(message));
                // 拦截器可能修改消息，重新赋值
                eventPayload = message.getPayload();
                eventSubscriberManager.dispatch(eventPayload);
                getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postSubscribe(message));
            }
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            log.error("集成事件消息消费失败", ex);
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(), true);
        }
    }

    private String getExchangeConsumerQueueName(String exchange, String defaultVal) {
        if (StringUtils.isBlank(defaultVal)) {
            defaultVal = exchange + "-4-" + applicationName;
        }
        return TextUtils.resolvePlaceholderWithCache("${rabbitmq." + exchange + ".consumer.queue:" + defaultVal + "}", environment);
    }

    private void tryDeclareQueue(String queue, String exchange, String routingKey) {
        try {
            String exchangeType = TextUtils.resolvePlaceholderWithCache("${rabbitmq." + exchange + ".type:direct}", environment);
            Connection connection = connectionFactory.createConnection();
            Channel channel = connection.createChannel(false);
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, exchange, routingKey);
            channel.close();
            connection.close();
        } catch (Exception e) {
            log.error("创建消息队列失败", e);
            throw new RuntimeException(e);
        }
    }
}
