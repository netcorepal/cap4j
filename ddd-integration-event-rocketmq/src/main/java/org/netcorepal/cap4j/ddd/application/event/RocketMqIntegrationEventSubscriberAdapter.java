package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.EventMessageInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.*;

/**
 * 自动监听集成事件对应的RocketMQ
 *
 * @author binking338
 * @date 2023-02-28
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqIntegrationEventSubscriberAdapter {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final RocketMqIntegrationEventConfigure rocketMqIntegrationEventConfigure;

    private final Environment environment;
    private final String scanPath;
    private final String applicationName;
    private final String defaultNameSrv;
    private final String msgCharset;

    List<MQPushConsumer> mqPushConsumers = new ArrayList<>();

    public void init() {
        Set<Class<?>> classes = ScanUtils.findIntegrationEventClasses(scanPath);
        classes.stream().filter(cls -> {
            IntegrationEvent integrationEvent = cls.getAnnotation(IntegrationEvent.class);
            return !Objects.isNull(integrationEvent) && StringUtils.isNotEmpty(integrationEvent.value())
                    & !IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber());
        }).forEach(integrationEventClass -> {
            MQPushConsumer mqPushConsumer = rocketMqIntegrationEventConfigure == null ? null : rocketMqIntegrationEventConfigure.get(integrationEventClass);
            if (mqPushConsumer == null) {
                mqPushConsumer = createDefaultConsumer(integrationEventClass);
            }
            try {
                if (mqPushConsumer != null) {
                    if(mqPushConsumer instanceof DefaultMQPushConsumer && ((DefaultMQPushConsumer)mqPushConsumer).getMessageListener() == null) {
                        mqPushConsumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> onMessage(integrationEventClass, msgs, context));
                    }
                    mqPushConsumer.start();
                    mqPushConsumers.add(mqPushConsumer);
                }
            } catch (MQClientException e) {
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

    public DefaultMQPushConsumer createDefaultConsumer(Class<?> integrationEventClass) {
        IntegrationEvent integrationEvent = integrationEventClass.getAnnotation(IntegrationEvent.class);
        if (Objects.isNull(integrationEvent) || StringUtils.isBlank(integrationEvent.value())
                || IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber())) {
            // 不是集成事件, 或显式标明无订阅
            return null;
        }
        String target = integrationEvent.value();
        target = TextUtils.resolvePlaceholderWithCache(target, environment);
        String topic = target.lastIndexOf(':') > 0 ? target.substring(0, target.lastIndexOf(':')) : target;
        String tag = target.lastIndexOf(':') > 0 ? target.substring(target.lastIndexOf(':') + 1) : "";

        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setConsumerGroup(getTopicConsumerGroup(topic, integrationEvent.subscriber()));
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        mqPushConsumer.setInstanceName(applicationName);
        String nameServerAddr = getTopicNamesrvAddr(topic, defaultNameSrv);
        mqPushConsumer.setNamesrvAddr(nameServerAddr);
        mqPushConsumer.setUnitName(integrationEventClass.getSimpleName());
        try {
            mqPushConsumer.subscribe(topic, tag);
        } catch (MQClientException e) {
            log.error("集成事件消息监听订阅失败", e);
        }
        return mqPushConsumer;
    }

    private ConsumeConcurrentlyStatus onMessage(Class<?> integrationEventClass, List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                log.info(String.format("集成事件消费，msgId=%s", msg.getMsgId()));
                String strMsg = new String(msg.getBody(), msgCharset);
                Object eventPayload = JSON.parseObject(strMsg, integrationEventClass, Feature.SupportNonPublicField);

                if (getOrderedEventMessageInterceptors().isEmpty()) {
                    eventSubscriberManager.dispatch(eventPayload);
                } else {
                    Map<String, Object> headers = new HashMap<>();
                    headers.putAll(msg.getProperties());
                    Message<Object> message = new GenericMessage<>(eventPayload, new EventMessageInterceptor.ModifiableMessageHeaders(headers));
                    getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.preSubscribe(message));
                    // 拦截器可能修改消息，重新赋值
                    eventPayload = message.getPayload();
                    eventSubscriberManager.dispatch(eventPayload);
                    getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postSubscribe(message));
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception ex) {
            log.error("集成事件消息消费异常", ex);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private String getTopicConsumerGroup(String topic, String defaultVal) {
        if (StringUtils.isBlank(defaultVal)) {
            defaultVal = topic + "-4-" + applicationName;
        }
        String group = TextUtils.resolvePlaceholderWithCache("${rocketmq." + topic + ".consumer.group:" + defaultVal + "}", environment);
        return group;
    }

    private String getTopicNamesrvAddr(String topic, String defaultVal) {
        if (StringUtils.isBlank(defaultVal)) {
            defaultVal = defaultNameSrv;
        }
        String nameServer = TextUtils.resolvePlaceholderWithCache("${rocketmq." + topic + ".name-server:" + defaultVal + "}", environment);
        return nameServer;
    }
}
