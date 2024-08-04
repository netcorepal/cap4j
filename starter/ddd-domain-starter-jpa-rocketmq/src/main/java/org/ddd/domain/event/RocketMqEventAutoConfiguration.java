package org.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.ddd.application.distributed.Locker;
import org.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.ddd.domain.event.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.List;

import static org.ddd.share.Constants.*;

/**
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@ConditionalOnProperty(name = "rocketmq.name-server")
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = {"org.ddd.domain.event.persistence"})
@EntityScan(basePackages = {"org.ddd.domain.event.persistence"})
@EnableScheduling
public class RocketMqEventAutoConfiguration {
    private final Locker locker;
    private final EventRepository eventRepository;
    private final ArchivedEventJpaRepository archivedEventJpaRepository;
    private final RocketMQTemplate rocketMQTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final List<RocketMqDomainEventSubscriber> subscribers;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    @ConditionalOnMissingBean(EventRecordRepository.class)
    public JpaEventRecordRepository jpaEventRecordRepository() {
        JpaEventRecordRepository eventRecordRepository = new JpaEventRecordRepository(eventRepository);
        return eventRecordRepository;
    }

    @Bean
    public RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager() {
        RocketMqDomainEventSubscriberManager domainEventSubscriberManager = new RocketMqDomainEventSubscriberManager(subscribers, applicationEventPublisher);
        return domainEventSubscriberManager;
    }

    @Bean
    public RocketMqDomainEventPublisher rocketMqDomainEventPublisher(RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager, JpaEventRecordRepository jpaEventRecordRepository) {
        RocketMqDomainEventPublisher rocketMqDomainEventPublisher = new RocketMqDomainEventPublisher(rocketMqDomainEventSubscriberManager, rocketMQTemplate, jpaEventRecordRepository);
        return rocketMqDomainEventPublisher;
    }

    @Bean
    public RocketMqDomainEventSupervisor rocketMqDomainEventSupervisor() {
        RocketMqDomainEventSupervisor rocketMqDomainEventSupervisor = new RocketMqDomainEventSupervisor();
        return rocketMqDomainEventSupervisor;
    }

    @Bean
    public RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter(RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager) {
        RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter = new RocketMqDomainEventSubscriberAdapter(rocketMqDomainEventSubscriberManager);
        return rocketMqDomainEventSubscriberAdapter;
    }

    @Bean
    public JpaEventScheduleService eventScheduleService(DomainEventPublisher domainEventPublisher) {
        scheduleService = new JpaEventScheduleService(locker, domainEventPublisher, eventRepository, archivedEventJpaRepository, jdbcTemplate);
        scheduleService.addPartition();
        return scheduleService;
    }

    private JpaEventScheduleService scheduleService = null;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_BATCHSIZE)
    private int batchSize;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_MAXCONCURRENT)
    private int maxConcurrency;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_INTERVALSECONDS)
    private int intervalSeconds;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_MAXLOCKSECONDS)
    private int maxLockSeconds;

    @Scheduled(cron = CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_CRON)
    public void compensation() {
        if (scheduleService == null) return;
        scheduleService.compense(batchSize, maxConcurrency, Duration.ofSeconds(intervalSeconds), Duration.ofSeconds(maxLockSeconds));
    }

    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_ARCHIVE_BATCHSIZE)
    private int archiveBatchSize;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_ARCHIVE_EXPIREDAYS)
    private int archiveExpireDays;
    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_ARCHIVE_MAXLOCKSECONDS)
    private int archiveMaxLockSeconds;
    @Scheduled(cron = CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_ARCHIVE_CRON)
    public void archive() {
        if (scheduleService == null) return;
        scheduleService.archive(archiveExpireDays, archiveBatchSize, Duration.ofSeconds(archiveMaxLockSeconds));
    }

    @Scheduled(cron = CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_ADDPARTITION_CRON)
    public void addTablePartition(){
        scheduleService.addPartition();
    }

}
