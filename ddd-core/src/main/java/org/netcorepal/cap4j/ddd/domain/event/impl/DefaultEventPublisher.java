package org.netcorepal.cap4j.ddd.domain.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
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
public class DefaultEventPublisher implements EventPublisher {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<IntegrationEventPublisher> integrationEventPublisheres;
    private final EventRecordRepository eventRecordRepository;
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final List<DomainEventInterceptor> domainEventInterceptors;
    private final List<IntegrationEventInterceptor> integrationEventInterceptors;
    private final int threadPoolsize;

    private ScheduledExecutorService executor = null;

    public void init() {
        if (null != this.executor) {
            return;
        }
        synchronized (this) {
            if (null != this.executor) {
                return;
            }
            this.executor = Executors.newScheduledThreadPool(threadPoolsize);
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
        getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.initPublish(message));

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

    /**
     * 内部发布实现 - 领域事件
     *
     * @param event
     */
    protected void internalPublish4DomainEvent(EventRecord event) {
        try {
            Message message = event.getMessage();
            boolean persist = (Boolean) message.getHeaders().getOrDefault(HEADER_KEY_CAP4J_PERSIST, false);
            getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.preRelease(event));
            getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.prePublish(message));
            // 进程内消息
            LocalDateTime now = LocalDateTime.now();
            eventSubscriberManager.dispatch(event.getPayload());
            event.confirmedDelivery(now);
            if (persist) {
                getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.prePersist(event));
                eventRecordRepository.save(event);
                getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.postPersist(event));
            }
            getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postPublish(message));
            getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.postRelease(event));
        } catch (Exception ex) {
            getOrderedDomainEventInterceptors().forEach(interceptor -> interceptor.onException(ex, event));
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
            getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.preRelease(event));
            getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.prePublish(event.getMessage()));

            integrationEventPublisheres.forEach(integrationEventPublisher -> integrationEventPublisher.publish(event, new IntegrationEventSendPublishCallback(getOrderedEventMessageInterceptors(), getOrderedIntegrationEventInterceptors(), eventRecordRepository)));

        } catch (Exception ex) {
            getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.onException(ex, event));
            throw new DomainException(String.format("集成事件发布失败: %s", event.getId()), ex);
        }
    }

    @RequiredArgsConstructor
    public static class IntegrationEventSendPublishCallback implements IntegrationEventPublisher.PublishCallback {
        private final List<EventMessageInterceptor> orderedEventMessageInterceptors;
        private final List<IntegrationEventInterceptor> orderedIntegrationEventInterceptor;
        private final EventRecordRepository eventRecordRepository;

        @Override
        public void onSuccess(EventRecord event) {
            LocalDateTime now = LocalDateTime.now();
            // 修改事件消费状态
            event.confirmedDelivery(now);

            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.prePersist(event));
            eventRecordRepository.save(event);
            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.postPersist(event));

            orderedEventMessageInterceptors.forEach(interceptor -> interceptor.postPublish(event.getMessage()));
            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.postRelease(event));
        }

        @Override
        public void onException(EventRecord event, Throwable throwable) {
            LocalDateTime now = LocalDateTime.now();
            // 修改事件异常状态
            event.occuredException(now, throwable);

            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.prePersist(event));
            eventRecordRepository.save(event);
            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.postPersist(event));

            orderedIntegrationEventInterceptor.forEach(interceptor -> interceptor.onException(throwable, event));
        }
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

    private List<DomainEventInterceptor> sortedDomainEventInterceptors = null;

    /**
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     *
     * @return
     */
    protected List<DomainEventInterceptor> getOrderedDomainEventInterceptors() {
        if (sortedDomainEventInterceptors == null) {
            sortedDomainEventInterceptors = new ArrayList<>(domainEventInterceptors);
            sortedDomainEventInterceptors.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
        }
        return sortedDomainEventInterceptors;
    }

    private List<IntegrationEventInterceptor> sortedIntegrationEventInterceptors = null;

    /**
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     *
     * @return
     */
    protected List<IntegrationEventInterceptor> getOrderedIntegrationEventInterceptors() {
        if (sortedIntegrationEventInterceptors == null) {
            sortedIntegrationEventInterceptors = new ArrayList<>(integrationEventInterceptors);
            sortedIntegrationEventInterceptors.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
        }
        return sortedIntegrationEventInterceptors;
    }
}
