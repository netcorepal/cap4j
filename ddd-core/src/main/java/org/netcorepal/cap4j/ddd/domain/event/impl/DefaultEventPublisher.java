package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptorManager;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.netcorepal.cap4j.ddd.share.Constants.*;

/**
 * 默认事件发布器
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultEventPublisher implements EventPublisher {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<IntegrationEventPublisher> integrationEventPublisheres;
    private final EventRecordRepository eventRecordRepository;
    private final EventMessageInterceptorManager eventMessageInterceptorManager;
    private final DomainEventInterceptorManager domainEventInterceptorManager;
    private final IntegrationEventInterceptorManager integrationEventInterceptorManager;
    private final IntegrationEventPublisher.PublishCallback integrationEventPublisherCallback;
    private final int threadPoolSize;

    private ScheduledExecutorService executor = null;

    public void init() {
        if (null != this.executor) {
            return;
        }
        synchronized (this) {
            if (null != this.executor) {
                return;
            }
            this.executor = Executors.newScheduledThreadPool(threadPoolSize);
        }
    }

    /**
     * 发布事件
     *
     * @param event
     */
    public void publish(EventRecord event) {
        init();

        Message<?> message = event.getMessage();
        // 事件消息拦截器 - 初始化
        eventMessageInterceptorManager.getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.initPublish(message));

        // 填入消息头
        String eventType = ((String) message.getHeaders().getOrDefault(HEADER_KEY_CAP4J_EVENT_TYPE, null));
        Duration delay = Duration.ZERO;
        if (message.getHeaders().containsKey(HEADER_KEY_CAP4J_SCHEDULE)) {
            LocalDateTime scheduleAt = LocalDateTime.ofEpochSecond((Long) message.getHeaders().get(HEADER_KEY_CAP4J_SCHEDULE), 0, ZoneOffset.UTC);
            if (scheduleAt != null) {
                delay = Duration.between(LocalDateTime.now(), scheduleAt);
            }
        }

        // 根据事件类型，选择不同的发布方式
        switch (eventType) {
            case HEADER_VALUE_CAP4J_EVENT_TYPE_INTEGRATION:
                if (delay.isNegative() || delay.isZero()) {
                    internalPublish4IntegrationEvent(event);
                } else {
                    executor.schedule(() -> {
                        internalPublish4IntegrationEvent(event);
                    }, delay.getSeconds(), TimeUnit.SECONDS);
                }
                break;
            case HEADER_VALUE_CAP4J_EVENT_TYPE_DOMAIN:
            default:
                if (delay.isNegative() || delay.isZero()) {
                    boolean persist = (Boolean) message.getHeaders().getOrDefault(HEADER_KEY_CAP4J_PERSIST, false);
                    if (persist) {
                        executor.submit(() -> {
                            internalPublish4DomainEvent(event);
                        });
                    } else {
                        internalPublish4DomainEvent(event);
                    }
                } else {
                    executor.schedule(() -> {
                        internalPublish4DomainEvent(event);
                    }, delay.getSeconds(), TimeUnit.SECONDS);
                }
                break;
        }
    }

    @Override
    public void retry(EventRecord eventRecord, LocalDateTime minNextTryTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliverTime = eventRecord.getNextTryTime().isAfter(now)
                ? eventRecord.getNextTryTime()
                : now;

        boolean delivering = eventRecord.beginDelivery(deliverTime);

        int maxTry = 65535;
        while (eventRecord.getNextTryTime().isBefore(minNextTryTime)
                && eventRecord.isValid()
        ) {
            eventRecord.beginDelivery(eventRecord.getNextTryTime());
            if (maxTry-- <= 0) {
                throw new DomainException("疑似死循环");
            }
        }

        eventRecordRepository.save(eventRecord);
        if (delivering) {
            eventRecord.markPersist(true);
            publish(eventRecord);
        }
    }

    /**
     * 内部发布实现 - 领域事件
     *
     * @param event
     */
    protected void internalPublish4DomainEvent(EventRecord event) {
        try {
            Message message = event.getMessage();
            boolean persist = (Boolean) message.getHeaders().getOrDefault(HEADER_KEY_CAP4J_PERSIST, false);
            domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.preRelease(event));
            eventMessageInterceptorManager.getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.prePublish(message));
            // 进程内消息
            LocalDateTime now = LocalDateTime.now();
            eventSubscriberManager.dispatch(event.getPayload());
            event.confirmedDelivery(now);
            if (persist) {
                domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.prePersist(event));
                eventRecordRepository.save(event);
                domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.postPersist(event));
            }
            eventMessageInterceptorManager.getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postPublish(message));
            domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.postRelease(event));
        } catch (Exception ex) {
            domainEventInterceptorManager.getOrderedEventInterceptors4DomainEvent().forEach(interceptor -> interceptor.onException(ex, event));
            log.error(String.format("领域事件发布失败：%s", event.getId()), ex);
            throw new DomainException(String.format("领域事件发布失败：%s", event.getId()), ex);
        }
    }

    /**
     * 内部发布实现 - 集成事件
     *
     * @param event
     */
    protected void internalPublish4IntegrationEvent(EventRecord event) {
        try {
            integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.preRelease(event));
            eventMessageInterceptorManager.getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.prePublish(event.getMessage()));

            integrationEventPublisheres.forEach(integrationEventPublisher -> integrationEventPublisher.publish(
                    event,
                    integrationEventPublisherCallback
            ));

        } catch (Exception ex) {
            integrationEventInterceptorManager.getOrderedEventInterceptors4IntegrationEvent().forEach(interceptor -> interceptor.onException(ex, event));
            log.error(String.format("集成事件发布失败：%s", event.getId()), ex);
            throw new DomainException(String.format("集成事件发布失败: %s", event.getId()), ex);
        }
    }
}
