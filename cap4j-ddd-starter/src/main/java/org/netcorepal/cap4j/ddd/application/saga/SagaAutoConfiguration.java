package org.netcorepal.cap4j.ddd.application.saga;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestInterceptor;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.application.saga.configure.SagaProperties;
import org.netcorepal.cap4j.ddd.application.saga.configure.SagaScheduleProperties;
import org.netcorepal.cap4j.ddd.application.saga.impl.DefaultSagaSupervisor;
import org.netcorepal.cap4j.ddd.application.saga.persistence.ArchivedSagaJpaRepository;
import org.netcorepal.cap4j.ddd.application.saga.persistence.SagaJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.time.Duration;
import java.util.List;

import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * Saga 自动配置
 *
 * @author binking338
 * @date 2024/10/15
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = {
        "org.netcorepal.cap4j.ddd.application.saga.persistence"
})
@EntityScan(basePackages = {
        "org.netcorepal.cap4j.ddd.application.saga.persistence"
})
public class SagaAutoConfiguration {
    public static final String CONFIG_KEY_4_SAGA_COMPENSE_LOCKER_KEY = "saga_compense[" + CONFIG_KEY_4_SVC_NAME + "]";
    public static final String CONFIG_KEY_4_SAGA_ARCHIVE_LOCKER_KEY = "saga_archive[" + CONFIG_KEY_4_SVC_NAME + "]";

    @Bean
    public DefaultSagaSupervisor defaultSagaSupervisor(
            List<RequestHandler<?, ?>> requestHandlers,
            List<RequestInterceptor<?, ?>> requestInterceptors,
            Validator validator,
            SagaRecordRepository sagaRecordRepository,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            SagaProperties sagaProperties
    ) {
        DefaultSagaSupervisor defaultSagaSupervisor = new DefaultSagaSupervisor(
                requestHandlers,
                requestInterceptors,
                validator,
                sagaRecordRepository,
                svcName,
                sagaProperties.getAsyncThreadPoolSize()
        );
        SagaSupervisorSupport.configure((SagaSupervisor) defaultSagaSupervisor);
        SagaSupervisorSupport.configure((SagaProcessSupervisor) defaultSagaSupervisor);
        defaultSagaSupervisor.init();
        return defaultSagaSupervisor;
    }

    @Bean
    @ConditionalOnMissingBean(SagaRecordRepository.class)
    public JpaSagaRecordRepository jpaSagaRecordRepository(
            SagaJpaRepository sagaJpaRepository,
            ArchivedSagaJpaRepository archivedSagaJpaRepository
    ) {
        JpaSagaRecordRepository jpaSagaRecordRepository = new JpaSagaRecordRepository(
                sagaJpaRepository,
                archivedSagaJpaRepository
        );
        return jpaSagaRecordRepository;
    }

    @Bean
    @ConditionalOnBean(JpaSagaRecordRepository.class)
    public JpaSagaScheduleService jpaSagaScheduleService(
            SagaRecordRepository sagaRecordRepository,
            Locker locker,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_SAGA_COMPENSE_LOCKER_KEY)
            String compensationLockerKey,
            @Value(CONFIG_KEY_4_SAGA_ARCHIVE_LOCKER_KEY)
            String archiveLockerKey,
            SagaScheduleProperties sagaScheduleProperties,
            JdbcTemplate jdbcTemplate
    ) {
        JpaSagaScheduleService jpaSagaScheduleService = new JpaSagaScheduleService(
                sagaRecordRepository,
                locker,
                svcName,
                compensationLockerKey,
                archiveLockerKey,
                sagaScheduleProperties.isAddPartitionEnable(),
                jdbcTemplate
        );
        jpaSagaScheduleService.init();
        return jpaSagaScheduleService;
    }

    /**
     * Saga定时补偿任务
     */
    @RequiredArgsConstructor
    @Service
    @EnableScheduling
    @ConditionalOnBean(JpaSagaScheduleService.class)
    private static class __SagaScheduleLoader {

        private static final String CONFIG_KEY_4_COMPENSE_CRON = "${cap4j.ddd.application.saga.schedule.compenseCron:${cap4j.ddd.application.saga.schedule.compense-cron:0 */1 * * * ?}}";
        private static final String CONFIG_KEY_4_ARCHIVE_CRON = "${cap4j.ddd.application.saga.schedule.archiveCron:${cap4j.ddd.application.saga.schedule.archive-cron:0 0 2 * * ?}}";
        private static final String CONFIG_KEY_4_ADD_PARTITION_CRON = "${cap4j.ddd.application.saga.schedule.addPartitionCron:${cap4j.ddd.application.saga.schedule.add-partition-cron:0 0 0 * * ?}}";


        private final SagaScheduleProperties sagaScheduleProperties;
        private final JpaSagaScheduleService scheduleService;

        @Scheduled(cron = CONFIG_KEY_4_COMPENSE_CRON)
        public void compensation() {
            if (scheduleService == null) return;
            scheduleService.compense(
                    sagaScheduleProperties.getCompenseBatchSize(),
                    sagaScheduleProperties.getCompenseMaxConcurrency(),
                    Duration.ofSeconds(sagaScheduleProperties.getCompenseIntervalSeconds()),
                    Duration.ofSeconds(sagaScheduleProperties.getCompenseMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ARCHIVE_CRON)
        public void archive() {
            if (scheduleService == null) return;
            scheduleService.archive(
                    sagaScheduleProperties.getArchiveExpireDays(),
                    sagaScheduleProperties.getArchiveBatchSize(),
                    Duration.ofSeconds(sagaScheduleProperties.getArchiveMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ADD_PARTITION_CRON)
        public void addTablePartition() {
            scheduleService.addPartition();
        }
    }
}
