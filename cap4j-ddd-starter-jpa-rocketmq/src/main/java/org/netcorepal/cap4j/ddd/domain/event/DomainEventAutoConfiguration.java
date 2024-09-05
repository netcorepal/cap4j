package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.application.event.*;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultEventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultEventSubscriberManager;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventScheduleProperties;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.List;

import static org.netcorepal.cap4j.ddd.share.Constants.*;

/**
 * 基于RocketMq的领域事件（集成事件）实现自动配置类
 *
 *
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
@EnableScheduling
public class DomainEventAutoConfiguration {
    public static final String CONFIG_KEY_4_EVENT_COMPENSE_LOCKER_KEY = "event_compense[" + CONFIG_KEY_4_SVC_NAME + "]";
    public static final String CONFIG_KEY_4_EVENT_ARCHIVE_LOCKER_KEY = "event_archive[" + CONFIG_KEY_4_SVC_NAME + "]";


    private static final String CONFIG_KEY_4_COMPENSE_CRON = "${cap4j.ddd.domain.event.schedule.compenseCron:${cap4j.ddd.domain.event.schedule.compense-cron:0 */1 * * * ?}}";
    private static final String CONFIG_KEY_4_ARCHIVE_CRON = "${cap4j.ddd.domain.event.schedule.archiveCron:${cap4j.ddd.domain.event.schedule.archive-cron:0 0 2 * * ?}}";
    private static final String CONFIG_KEY_4_ADD_PARTITION_CRON = "${cap4j.ddd.domain.event.schedule.addPartitionCron:${cap4j.ddd.domain.event.schedule.add-partition-cron:0 0 0 * * ?}}";


    private final EventScheduleProperties eventScheduleProperties;
    private JpaEventScheduleService scheduleService = null;

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

    @Bean
    public JpaEventScheduleService eventScheduleService(
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
        scheduleService = new JpaEventScheduleService(
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

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher defaultEventPublisher(
            EventSubscriberManager eventSubscriberManager,
            List<IntegrationEventPublisher> integrationEventPublisheres,
            EventRecordRepository eventRecordRepository,
            List<EventMessageInterceptor> eventMessageInterceptors,
            List<DomainEventInterceptor> domainEventInterceptors,
            List<IntegrationEventInterceptor> integrationEventInterceptors,
            EventProperties eventProperties
    ){
        DefaultEventPublisher defaultEventPublisher = new DefaultEventPublisher(
                eventSubscriberManager,
                integrationEventPublisheres,
                eventRecordRepository,
                eventMessageInterceptors,
                domainEventInterceptors,
                integrationEventInterceptors,
                eventProperties.getPublisherThreadPoolSize()
        );
        defaultEventPublisher.init();
        return defaultEventPublisher;
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriberManager.class)
    public EventSubscriberManager defaultEventSubscriberManager(
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
    @Primary
    public DomainEventSupervisor defaultDomainEventSupervisor(
            EventRecordRepository eventRecordRepository,
            List<DomainEventInterceptor> domainEventInterceptors,
            EventPublisher eventPublisher,
            ApplicationEventPublisher applicationEventPublisher,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName
    ) {
        DefaultDomainEventSupervisor defaultDomainEventSupervisor = new DefaultDomainEventSupervisor(
                eventRecordRepository,
                domainEventInterceptors,
                eventPublisher,
                applicationEventPublisher,
                svcName
        );

        DomainEventSupervisorSupport.configure(defaultDomainEventSupervisor);
        return defaultDomainEventSupervisor;
    }

}