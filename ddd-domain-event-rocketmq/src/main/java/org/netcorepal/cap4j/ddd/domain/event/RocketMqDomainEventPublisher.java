package org.netcorepal.cap4j.ddd.domain.event;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.TextUtils;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.netcorepal.cap4j.ddd.share.Constants.HEADER_KEY_CAP4J_SCHEDULE;

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
    private final Environment environment;
    private final ScheduledExecutorService executor;

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
            RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager,
            RocketMQTemplate rocketMQTemplate,
            EventRecordRepository eventRecordRepository,
            int threadPoolsize,
            Environment environment
    ) {
        this.rocketMqDomainEventSubscriberManager = rocketMqDomainEventSubscriberManager;
        this.rocketMQTemplate = rocketMQTemplate;
        this.eventRecordRepository = eventRecordRepository;
        this.executor = Executors.newScheduledThreadPool(threadPoolsize);
        this.environment = environment;

    }

    /**
     * 发布事件
     *
     * @param event
     */
    public void publish(Message message, EventRecord event) {
        Duration delay = Duration.ZERO;
        if (message.getHeaders().containsKey(HEADER_KEY_CAP4J_SCHEDULE)) {
            LocalDateTime scheduleAt = (LocalDateTime) message.getHeaders().get(HEADER_KEY_CAP4J_SCHEDULE);
            if(scheduleAt!=null) {
                delay = Duration.between(LocalDateTime.now(), scheduleAt);
            }
        }
        if (delay.isNegative() || delay.isZero()) {
            executor.submit(() -> {
                internalPublish(message, event);
            });
        } else {
            executor.schedule(() -> {
                internalPublish(message, event);
            }, delay.getSeconds(), TimeUnit.SECONDS);
        }
    }

    private void internalPublish(Message message, EventRecord event) {
        try {
            String destination = event.getEventTopic();
            destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
            // MQ消息
            if (destination != null && !destination.isEmpty()) {
                rocketMQTemplate.asyncSend(destination, message, new DomainEventSendCallback(event, eventRecordRepository, environment));
            } else {
                // 进程内消息
                rocketMqDomainEventSubscriberManager.trigger(event.getPayload());
                event.confirmedDelivery(LocalDateTime.now());
                eventRecordRepository.save(event);
            }
        } catch (Exception ex) {
            log.error(String.format("集成事件发布失败: %s", event.toString()), ex);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class DomainEventSendCallback implements SendCallback {
        private final EventRecord event;
        private final EventRecordRepository eventRecordRepository;
        private final Environment environment;

        @Override
        public void onSuccess(SendResult sendResult) {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                LocalDateTime now = LocalDateTime.now();
                // 修改事件消费状态
                event.confirmedDelivery(now);
                eventRecordRepository.save(event);
                log.info(String.format("集成事件发送成功, destination=%s, body=%s", TextUtils.resolvePlaceholderWithCache(event.getEventTopic(), environment), JSON.toJSONString(event.getPayload())));
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
                LocalDateTime now = LocalDateTime.now();
                // 修改事件异常状态
                event.occuredException(now, throwable);
                eventRecordRepository.save(event);
                log.error(String.format("集成事件发送失败, destination=%s, body=%s", TextUtils.resolvePlaceholderWithCache(event.getEventTopic(), environment), JSON.toJSONString(event.getPayload())), throwable);
            } catch (Exception ex) {
                log.error("本地事件库持久化失败", ex);
            }
        }
    }
}
