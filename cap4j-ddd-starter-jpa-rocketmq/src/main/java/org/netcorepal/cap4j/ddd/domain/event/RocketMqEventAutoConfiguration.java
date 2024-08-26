package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventScheduleProperties;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@ConditionalOnProperty(name = "rocketmq.name-server")
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = {"org.netcorepal.cap4j.ddd.domain.event.persistence"})
@EntityScan(basePackages = {"org.netcorepal.cap4j.ddd.domain.event.persistence"})
@EnableScheduling
public class RocketMqEventAutoConfiguration {
    private final Locker locker;
    private final EventRepository eventRepository;
    private final ArchivedEventJpaRepository archivedEventJpaRepository;
    private final RocketMQTemplate rocketMQTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final List<RocketMqDomainEventSubscriber> subscribers;
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    private final EventScheduleProperties eventScheduleProperties;

    @Bean
    @ConditionalOnMissingBean(EventRecordRepository.class)
    public JpaEventRecordRepository jpaEventRecordRepository() {
        JpaEventRecordRepository eventRecordRepository = new JpaEventRecordRepository(eventRepository);
        return eventRecordRepository;
    }

    @Bean
    public RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager() {
        RocketMqDomainEventSubscriberManager domainEventSubscriberManager = new RocketMqDomainEventSubscriberManager(
                subscribers,
                applicationEventPublisher);
        return domainEventSubscriberManager;
    }

    @Bean
    public RocketMqDomainEventPublisher rocketMqDomainEventPublisher(
            RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager,
            JpaEventRecordRepository jpaEventRecordRepository,
            EventProperties eventProperties) {
        RocketMqDomainEventPublisher rocketMqDomainEventPublisher = new RocketMqDomainEventPublisher(
                rocketMqDomainEventSubscriberManager,
                rocketMQTemplate,
                jpaEventRecordRepository,
                eventProperties.getPublisherThreadPoolSize(),
                environment);
        return rocketMqDomainEventPublisher;
    }

    @Bean
    public DefaultDomainEventSupervisor defaultDomainEventSupervisor() {
        DefaultDomainEventSupervisor defaultDomainEventSupervisor = new DefaultDomainEventSupervisor();
        DomainEventSupervisorConfiguration.configure((DomainEventSupervisor)defaultDomainEventSupervisor);
        DomainEventSupervisorConfiguration.configure((EventSupervisor)defaultDomainEventSupervisor);
        return defaultDomainEventSupervisor;
    }

    @Bean
    public RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter(
            RocketMqDomainEventSubscriberManager rocketMqDomainEventSubscriberManager,
            @Autowired(required = false)
            DomainEventMessageInterceptor domainEventMessageInterceptor,
            @Autowired(required = false)
            MQConsumerConfigure mqConsumerConfigure,
            EventProperties eventProperties,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String applicationName,
            @Value("${rocketmq.name-server:}")
            String defaultNameSrv,
            @Value("${rocketmq.msg-charset:UTF-8}")
            String msgCharset
    ) {
        RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter = new RocketMqDomainEventSubscriberAdapter(
                rocketMqDomainEventSubscriberManager,
                environment,
                domainEventMessageInterceptor,
                mqConsumerConfigure,
                applicationName,
                defaultNameSrv,
                msgCharset,
                eventProperties.getSubscriberScanPackage()
        );
        return rocketMqDomainEventSubscriberAdapter;
    }

    @Bean
    public JpaEventScheduleService eventScheduleService(
            DomainEventPublisher domainEventPublisher,
            JpaEventRecordRepository jpaEventRecordRepository,
            @Autowired(required = false)
            DomainEventMessageInterceptor domainEventMessageInterceptor,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value("event_compense[" + CONFIG_KEY_4_SVC_NAME + "]")
            String compensationLockerKey,
            @Value("event_archive[" + CONFIG_KEY_4_SVC_NAME + "]")
            String archiveLockerKey) {
        scheduleService = new JpaEventScheduleService(
                locker,
                domainEventPublisher,
                domainEventMessageInterceptor,
                jpaEventRecordRepository,
                eventRepository,
                archivedEventJpaRepository,
                svcName,
                compensationLockerKey,
                archiveLockerKey,
                eventScheduleProperties.isAddPartitionEnable(),
                jdbcTemplate);
        return scheduleService;
    }

    private JpaEventScheduleService scheduleService = null;

    @Scheduled(cron = "${cap4j.ddd.domain.event.schedule.compenseCron:0 */1 * * * ?}")
    public void compensation() {
        if (scheduleService == null) return;
        scheduleService.compense(
                eventScheduleProperties.getCompenseBatchSize(),
                eventScheduleProperties.getCompenseMaxConcurrency(),
                Duration.ofSeconds(eventScheduleProperties.getCompenseIntervalSeconds()),
                Duration.ofSeconds(eventScheduleProperties.getCompenseMaxLockSeconds())
        );
    }

    @Scheduled(cron = "${cap4j.ddd.domain.event.schedule.archiveCron:0 0 2 * * ?}")
    public void archive() {
        if (scheduleService == null) return;
        scheduleService.archive(
                eventScheduleProperties.getArchiveExpireDays(),
                eventScheduleProperties.getArchiveBatchSize(),
                Duration.ofSeconds(eventScheduleProperties.getArchiveMaxLockSeconds())
        );
    }

    @Scheduled(cron = "${cap4j.ddd.domain.event.schedule.addPartitionCron:0 0 0 * * ?}")
    public void addTablePartition() {
        scheduleService.addPartition();
    }

}
