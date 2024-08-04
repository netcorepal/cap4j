package org.ddd.domain.event;

import com.alibaba.fastjson.JSON;
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
import org.ddd.domain.event.annotation.DomainEvent;
import org.ddd.share.ScanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.ddd.share.Constants.CONFIG_KEY_4_DOMAIN_EVENT_SUB_PACKAGE;
import static org.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * 自动监听集成事件对应的RocketMQ
 *
 * @author <template/>
 * @date 2023-02-28
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqDomainEventSubscriberAdapter {
    private static final String CONFIG_KEY_4_ROCKETMQ_NAMESVC = "${rocketmq.name-server:}";
    private final RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager;

    List<MQPushConsumer> mqPushConsumers = new ArrayList<>();
    @Value(CONFIG_KEY_4_SVC_NAME)
    String applicationName = null;
    @Value(CONFIG_KEY_4_ROCKETMQ_NAMESVC)
    String defaultNameSrv = null;
    @Value(CONFIG_KEY_4_DOMAIN_EVENT_SUB_PACKAGE)
    String scanPath = null;
    @Autowired
    Environment environment;

    @Autowired(required = false)
    MQConsumerConfigure mqConsumerConfigure;

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
                createDefaultConsumer(domainEventClass);
            }
            try {
                mqPushConsumer.start();
            } catch (MQClientException e) {
                log.error("领域事件消息监听启动失败", e);
            }
            if (mqPushConsumer != null) {
                mqPushConsumers.add(mqPushConsumer);
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
        if (!rocketMqDomainEventSubscriberManager.hasSubscriber(domainEventClass)) {
            // 不存在订阅
            return null;
        }
        String target = domainEvent.value();
        target = environment.resolvePlaceholders(target);
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
                    String strMsg = new String(msg.getBody(), "UTF-8");
                    Object event = JSON.parseObject(strMsg, domainEventClass);
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
        String group = environment.resolvePlaceholders("${rocketmq." + topic + ".consumer.group:" + defaultVal + "}");
        return group;
    }

    private String getTopicNamesrvAddr(String topic, String defaultVal) {
        if (StringUtils.isBlank(defaultVal)) {
            defaultVal = defaultNameSrv;
        }
        String nameServer = environment.resolvePlaceholders("${rocketmq." + topic + ".name-server:" + defaultVal + "}");
        return nameServer;
    }
}
