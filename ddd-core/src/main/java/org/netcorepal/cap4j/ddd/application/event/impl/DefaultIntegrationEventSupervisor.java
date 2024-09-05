package org.netcorepal.cap4j.ddd.application.event.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventAttachedTransactionCommittedEvent;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptor;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.domain.event.EventRecordRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 默认事件管理器
 *
 * @author binking338
 * @date 2024/8/28
 */
@RequiredArgsConstructor
public class DefaultIntegrationEventSupervisor implements IntegrationEventSupervisor {
    private final EventPublisher eventPublisher;
    private final EventRecordRepository eventRecordRepository;
    private final List<IntegrationEventInterceptor> integrationEventInterceptors;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String svcName;

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

    /**
     * 默认事件过期时间（分钟）
     * 一天 60*24 = 1440
     */
    private static final int DEFAULT_IntegrationEvent_EXPIRE_MINUTES = 1440;
    /**
     * 默认事件重试次数
     */
    private static final int DEFAULT_IntegrationEvent_RETRY_TIMES = 200;

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT eventPayload, LocalDateTime schedule) {
        // 判断集成事件，仅支持集成事件。
        if (eventPayload == null || !eventPayload.getClass().isAnnotationPresent(IntegrationEvent.class)) {
            throw new DomainException("事件类型必须为领域事件");
        }

        getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.onNotify(eventPayload, schedule));

        EventRecord event = eventRecordRepository.create();
        event.init(eventPayload, this.svcName, schedule, Duration.ofMinutes(DEFAULT_IntegrationEvent_EXPIRE_MINUTES), DEFAULT_IntegrationEvent_RETRY_TIMES);
        getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.prePersist(event));
        eventRecordRepository.save(event);
        getOrderedIntegrationEventInterceptors().forEach(interceptor -> interceptor.postPersist(event));
        List<EventRecord> events = new ArrayList<>();
        events.add(event);

        IntegrationEventAttachedTransactionCommittedEvent integrationEventAttachedTransactionCommittedEvent
                = new IntegrationEventAttachedTransactionCommittedEvent(this, events);
        applicationEventPublisher.publishEvent(integrationEventAttachedTransactionCommittedEvent);
    }

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT eventPayload) {
        notify(eventPayload, LocalDateTime.now());
    }

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT eventPayload, Duration delay) {
        notify(eventPayload, LocalDateTime.now().plus(delay));
    }

    @TransactionalEventListener(fallbackExecution = true, classes = IntegrationEventAttachedTransactionCommittedEvent.class)
    public void onTransactionCommitted(IntegrationEventAttachedTransactionCommittedEvent transactionCommittedEvent) {
        List<EventRecord> events = transactionCommittedEvent.getEvents();
        if (events != null && !events.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            events.forEach(event -> {
                event.markPersist(true);
                eventPublisher.publish(event);
            });
        }
    }
}
