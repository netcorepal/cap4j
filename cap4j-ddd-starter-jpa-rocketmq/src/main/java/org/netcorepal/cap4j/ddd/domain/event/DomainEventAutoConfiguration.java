package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptorManager;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventScheduleProperties;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultEventSubscriberManager;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.netcorepal.cap4j.ddd.impl.DefaultEventInterceptorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * 基于RocketMq的领域事件（集成事件）实现自动配置类
 *
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = {
        "org.netcorepal.cap4j.ddd.domain.event.persistence"
})
@EntityScan(basePackages = {
        "org.netcorepal.cap4j.ddd.domain.event.persistence"
})
public class DomainEventAutoConfiguration {
    public static final String CONFIG_KEY_4_EVENT_COMPENSE_LOCKER_KEY = "event_compense[" + CONFIG_KEY_4_SVC_NAME + "]";
    public static final String CONFIG_KEY_4_EVENT_ARCHIVE_LOCKER_KEY = "event_archive[" + CONFIG_KEY_4_SVC_NAME + "]";


    @Bean
    public DefaultDomainEventSupervisor defaultDomainEventSupervisor(
            EventRecordRepository eventRecordRepository,
            DomainEventInterceptorManager domainEventInterceptorManager,
            EventPublisher eventPublisher,
            ApplicationEventPublisher applicationEventPublisher,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName
    ) {
        DefaultDomainEventSupervisor defaultDomainEventSupervisor = new DefaultDomainEventSupervisor(
                eventRecordRepository,
                domainEventInterceptorManager,
                eventPublisher,
                applicationEventPublisher,
                svcName
        );

        DomainEventSupervisorSupport.configure((DomainEventSupervisor) defaultDomainEventSupervisor);
        DomainEventSupervisorSupport.configure((DomainEventManager) defaultDomainEventSupervisor);
        return defaultDomainEventSupervisor;
    }

    @Bean
    @ConditionalOnMissingBean(JpaEventScheduleService.class)
    public JpaEventScheduleService jpaEventScheduleService(
            EventPublisher eventPublisher,
            EventRecordRepository eventRecordRepository,
            EventJpaRepository eventJpaRepository,
            ArchivedEventJpaRepository archivedEventJpaRepository,
            Locker locker,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_EVENT_COMPENSE_LOCKER_KEY)
            String compensationLockerKey,
            @Value(CONFIG_KEY_4_EVENT_ARCHIVE_LOCKER_KEY)
            String archiveLockerKey,
            EventScheduleProperties eventScheduleProperties,
            JdbcTemplate jdbcTemplate) {
        JpaEventScheduleService scheduleService = new JpaEventScheduleService(
                eventPublisher,
                eventRecordRepository,
                eventJpaRepository,
                archivedEventJpaRepository,
                locker,
                svcName,
                compensationLockerKey,
                archiveLockerKey,
                eventScheduleProperties.isAddPartitionEnable(),
                jdbcTemplate);
        scheduleService.init();
        return scheduleService;
    }

    /**
     * 领域事件定时补偿任务
     */
    @RequiredArgsConstructor
    @Service
    @EnableScheduling
    private static class __DomainEventScheduleLoader {

        private static final String CONFIG_KEY_4_COMPENSE_CRON = "${cap4j.ddd.domain.event.schedule.compenseCron:${cap4j.ddd.domain.event.schedule.compense-cron:0 */1 * * * ?}}";
        private static final String CONFIG_KEY_4_ARCHIVE_CRON = "${cap4j.ddd.domain.event.schedule.archiveCron:${cap4j.ddd.domain.event.schedule.archive-cron:0 0 2 * * ?}}";
        private static final String CONFIG_KEY_4_ADD_PARTITION_CRON = "${cap4j.ddd.domain.event.schedule.addPartitionCron:${cap4j.ddd.domain.event.schedule.add-partition-cron:0 0 0 * * ?}}";


        private final EventScheduleProperties eventScheduleProperties;
        private final JpaEventScheduleService scheduleService = null;

        @Scheduled(cron = CONFIG_KEY_4_COMPENSE_CRON)
        public void compensation() {
            if (scheduleService == null) return;
            scheduleService.compense(
                    eventScheduleProperties.getCompenseBatchSize(),
                    eventScheduleProperties.getCompenseMaxConcurrency(),
                    Duration.ofSeconds(eventScheduleProperties.getCompenseIntervalSeconds()),
                    Duration.ofSeconds(eventScheduleProperties.getCompenseMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ARCHIVE_CRON)
        public void archive() {
            if (scheduleService == null) return;
            scheduleService.archive(
                    eventScheduleProperties.getArchiveExpireDays(),
                    eventScheduleProperties.getArchiveBatchSize(),
                    Duration.ofSeconds(eventScheduleProperties.getArchiveMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ADD_PARTITION_CRON)
        public void addTablePartition() {
            scheduleService.addPartition();
        }
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public DefaultEventPublisher defaultEventPublisher(
            EventSubscriberManager eventSubscriberManager,
            List<IntegrationEventPublisher> integrationEventPublisheres,
            EventRecordRepository eventRecordRepository,
            EventMessageInterceptorManager eventMessageInterceptorManager,
            DomainEventInterceptorManager domainEventInterceptorManager,
            IntegrationEventInterceptorManager integrationEventInterceptorManager,
            EventProperties eventProperties
    ) {
        DefaultEventPublisher defaultEventPublisher = new DefaultEventPublisher(
                eventSubscriberManager,
                integrationEventPublisheres,
                eventRecordRepository,
                eventMessageInterceptorManager,
                domainEventInterceptorManager,
                integrationEventInterceptorManager,
                eventProperties.getPublisherThreadPoolSize()
        );
        defaultEventPublisher.init();
        return defaultEventPublisher;
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriberManager.class)
    public DefaultEventSubscriberManager defaultEventSubscriberManager(
            List<EventSubscriber<?>> subscribers,
            ApplicationEventPublisher applicationEventPublisher,
            EventProperties eventProperties
    ) {
        DefaultEventSubscriberManager domainEventSubscriberManager =
                new DefaultEventSubscriberManager(
                        subscribers,
                        applicationEventPublisher,
                        eventProperties.getEventScanPackage()
                );
        domainEventSubscriberManager.init();
        return domainEventSubscriberManager;
    }

    @Bean
    @ConditionalOnMissingBean(DefaultEventInterceptorManager.class)
    public DefaultEventInterceptorManager defaultEventInterceptorManager(
            List<EventMessageInterceptor> eventMessageInterceptors,
            List<EventInterceptor> interceptors
    ) {
        DefaultEventInterceptorManager defaultEventInterceptorManager = new DefaultEventInterceptorManager(
                eventMessageInterceptors,
                interceptors
        );
        return defaultEventInterceptorManager;
    }
}