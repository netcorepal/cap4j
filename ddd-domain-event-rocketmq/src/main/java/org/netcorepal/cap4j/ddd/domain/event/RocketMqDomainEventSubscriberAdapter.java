package org.netcorepal.cap4j.ddd.domain.event;

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
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;
import org.netcorepal.cap4j.ddd.share.ScanUtils;
import org.netcorepal.cap4j.ddd.share.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_DOMAIN_EVENT_SUB_PACKAGE;
import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * 自动监听集成事件对应的RocketMQ
 *
 * @author binking338
 * @date 2023-02-28
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqDomainEventSubscriberAdapter {
    private static final String CONFIG_KEY_4_ROCKETMQ_NAMESVC = "${rocketmq.name-server:}";
    private static final String CONFIG_KEY_4_ROCKETMQ_MSGCHARTSET = "${rocketmq.msg-charset:UTF-8}";
    private final RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager;

    List<MQPushConsumer> mqPushConsumers = new ArrayList<>();
    @Value(CONFIG_KEY_4_SVC_NAME)
    String applicationName = null;
    @Value(CONFIG_KEY_4_ROCKETMQ_NAMESVC)
    String defaultNameSrv = null;
    @Value(CONFIG_KEY_4_ROCKETMQ_MSGCHARTSET)
    String msgCharset = null;
    @Value(CONFIG_KEY_4_DOMAIN_EVENT_SUB_PACKAGE)
    String scanPath = null;
    @Autowired
    Environment environment;

    @Autowired(required = false)
    MQConsumerConfigure mqConsumerConfigure;

    @Autowired(required = false)
    private DomainEventMessageInterceptor domainEventMessageInterceptor;

    @PostConstruct
    public void init() {
        Set<Class<?>> classes = ScanUtils.scanClass(scanPath, true);
        classes.stream().filter(cls -> {
            DomainEvent domainEvent = cls.getAnnotation(DomainEvent.class);
            if (!Objects.isNull(domainEvent) && StringUtils.isNotEmpty(domainEvent.value())
                    & !DomainEvent.NONE_SUBSCRIBER.equalsIgnoreCase(domainEvent.subscriber())) {
                return true;
            } else {
                return false;
            }
        }).forEach(domainEventClass -> {
            MQPushConsumer mqPushConsumer = null;
            if (mqPushConsumer != null) {
                mqPushConsumer = mqConsumerConfigure.get(domainEventClass);
            }
            if (mqPushConsumer == null) {
                mqPushConsumer = createDefaultConsumer(domainEventClass);
            }
            try {
                if (mqPushConsumer != null) {
                    mqPushConsumer.start();
                    mqPushConsumers.add(mqPushConsumer);
                }
            } catch (MQClientException e) {
                log.error("领域事件消息监听启动失败", e);
            }
        });
    }

    public void shutdown() {
        if (mqPushConsumers == null || mqPushConsumers.isEmpty()) {
            return;
        }
        mqPushConsumers.forEach(mqPushConsumer -> {
            mqPushConsumer.shutdown();
        });
    }

    public DefaultMQPushConsumer createDefaultConsumer(Class domainEventClass) {
        DomainEvent domainEvent = (DomainEvent) domainEventClass.getAnnotation(DomainEvent.class);
        if (Objects.isNull(domainEvent) || StringUtils.isBlank(domainEvent.value())
                || DomainEvent.NONE_SUBSCRIBER.equalsIgnoreCase(domainEvent.subscriber())) {
            // 不是集成事件, 或显式标明无订阅
            return null;
        }
//        if (!rocketMqDomainEventSubscriberManager.hasSubscriber(domainEventClass)) {
//            // 不存在订阅
//            return null;
//        }
        String target = domainEvent.value();
        target = TextUtils.resolvePlaceholderWithCache(target, environment);
        String topic = target.lastIndexOf(':') > 0 ? target.substring(0, target.lastIndexOf(':')) : target;
        String tag = target.lastIndexOf(':') > 0 ? target.substring(target.lastIndexOf(':') + 1) : "";

        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setConsumerGroup(getTopicConsumerGroup(topic, domainEvent.subscriber()));
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        mqPushConsumer.setInstanceName(applicationName);
        String nameServerAddr = getTopicNamesrvAddr(topic, defaultNameSrv);
        mqPushConsumer.setNamesrvAddr(nameServerAddr);
        mqPushConsumer.setUnitName(domainEventClass.getSimpleName());
        mqPushConsumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
            try {
                for (MessageExt msg :
                        msgs) {
                    String strMsg = new String(msg.getBody(), msgCharset);
                    Object event = JSON.parseObject(strMsg, domainEventClass, Feature.SupportNonPublicField);
                    Map<String, Object> headers = new HashMap<>();
                    msg.getProperties().forEach((k,v) -> headers.put(k, v));
                    if (domainEventMessageInterceptor != null) {
                        Message message = new GenericMessage(event, new DomainEventMessageInterceptor.ModifiableMessageHeaders(headers));
                        message = domainEventMessageInterceptor.beforeSubscribe(message);
                        event = message.getPayload();
                    }
                    rocketMqDomainEventSubscriberManager.trigger(event);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception ex) {
                log.error("领域事件消息消费失败", ex);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        try {
            mqPushConsumer.subscribe(topic, tag);
        } catch (MQClientException e) {
            log.error("领域事件消息监听订阅失败", e);
        }
        return mqPushConsumer;
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
