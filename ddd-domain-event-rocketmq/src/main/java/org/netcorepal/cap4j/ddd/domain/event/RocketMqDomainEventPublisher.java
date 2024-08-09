package org.netcorepal.cap4j.ddd.domain.event;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 基于RocketMq的领域事件发布器
 *
 * @author binking338
 * @date 2023/8/13
 */
@Slf4j
public class RocketMqDomainEventPublisher implements DomainEventPublisher {
    private final RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager;
    private final RocketMQTemplate rocketMQTemplate;
    private final EventRecordRepository eventRecordRepository;

    @Autowired
    Environment environment;

    /**
     * 如下配置需配置好，保障RocketMqTemplate被初始化
     * ## rocketmq
     * #rocketmq.name-server = myrocket.nameserver:9876
     * #rocketmq.producer.group=${spring.application.name}
     *
     * @param rocketMqDomainEventSubscriberManager
     * @param rocketMQTemplate
     * @param eventRecordRepository
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RocketMqDomainEventPublisher(
            @Autowired RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager,
            @Autowired(required = false) RocketMQTemplate rocketMQTemplate,
            @Autowired(required = false) EventRecordRepository eventRecordRepository
    ) {
        this.rocketMqDomainEventSubscriberManager = rocketMqDomainEventSubscriberManager;
        this.rocketMQTemplate = rocketMQTemplate;
        this.eventRecordRepository = eventRecordRepository;
    }

    /**
     * 发布事件
     *
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(Message message, EventRecord event) {
        try {
            String destination = event.getEventTopic();
            destination = environment.resolvePlaceholders(destination);
            // MQ消息
            if (destination != null && !destination.isEmpty()) {
                rocketMQTemplate.asyncSend(destination, message, new DomainEventSendCallback(event, eventRecordRepository));
            } else {
                // 进程内消息
                rocketMqDomainEventSubscriberManager.trigger(event.getPayload());
                event.confirmDelivered(LocalDateTime.now());
                eventRecordRepository.save(event);
            }
        } catch (Exception ex) {
            log.error(String.format("集成事件发布失败: %s", event.toString()), ex);
        }
    }

    @Slf4j
    public static class DomainEventSendCallback implements SendCallback {
        private EventRecord event;
        private final EventRecordRepository eventRecordRepository;

        public DomainEventSendCallback(EventRecord event, EventRecordRepository eventRecordRepository) {
            this.event = event;
            this.eventRecordRepository = eventRecordRepository;
        }

        @Override
        public void onSuccess(SendResult sendResult) {
            // 修改事件消费状态
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                LocalDateTime now = LocalDateTime.now();
                event.confirmDelivered(now);
                eventRecordRepository.save(event);
                log.info(String.format("集成事件发送成功, destination=%s, body=%s", event.getEventTopic(), JSON.toJSONString(event.getPayload())));
            } catch (Exception ex) {
                log.error("本地事件库持久化失败", ex);
            }
        }

        @Override
        public void onException(Throwable throwable) {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                log.error(String.format("集成事件发送失败, destination=%s, body=%s", event.getEventTopic(), JSON.toJSONString(event.getPayload())), throwable);
            } catch (Exception ex) {
                log.error("本地事件库持久化失败", ex);
            }
        }
    }
}
