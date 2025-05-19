package org.netcorepal.cap4j.ddd;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.*;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.application.impl.DefaultRequestSupervisor;
import org.netcorepal.cap4j.ddd.application.persistence.ArchivedRequestJpaRepository;
import org.netcorepal.cap4j.ddd.application.persistence.RequestJpaRepository;
import org.netcorepal.cap4j.ddd.application.request.configure.RequestProperties;
import org.netcorepal.cap4j.ddd.application.request.configure.RequestScheduleProperties;
import org.netcorepal.cap4j.ddd.impl.DefaultMediator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
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
 * CQS自动配置类
 *
 * @author binking338
 * @date 2024/8/24
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = {
        "org.netcorepal.cap4j.ddd.application.persistence"
})
@EntityScan(basePackages = {
        "org.netcorepal.cap4j.ddd.application.persistence"
})
public class MediatorAutoConfiguration {
    public static final String CONFIG_KEY_4_REQUEST_COMPENSE_LOCKER_KEY = "request_compense[" + CONFIG_KEY_4_SVC_NAME + "]";
    public static final String CONFIG_KEY_4_REQUEST_ARCHIVE_LOCKER_KEY = "request_archive[" + CONFIG_KEY_4_SVC_NAME + "]";

    @Bean
    @ConditionalOnMissingBean(RequestRecordRepository.class)
    public JpaRequestRecordRepository jpaRequestRecordRepository(
            RequestJpaRepository requestJpaRepository,
            ArchivedRequestJpaRepository archivedRequestJpaRepository
    ) {
        JpaRequestRecordRepository requestRecordRepository = new JpaRequestRecordRepository(requestJpaRepository, archivedRequestJpaRepository);
        return requestRecordRepository;
    }

    @Bean
    public DefaultRequestSupervisor defaultRequestSupervisor(
            List<RequestHandler<?, ?>> requestHandlers,
            List<RequestInterceptor<?, ?>> requestInterceptors,
            Validator validator,
            RequestProperties requestProperties,
            RequestRecordRepository requestRecordRepository,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName
    ) {
        DefaultRequestSupervisor defaultRequestSupervisor = new DefaultRequestSupervisor(
                requestHandlers,
                requestInterceptors,
                validator,
                requestRecordRepository,
                svcName,
                requestProperties.getRequestScheduleThreadPoolSize(),
                requestProperties.getRequestScheduleThreadFactoryClassName()
        );
        defaultRequestSupervisor.init();
        RequestSupervisorSupport.configure((RequestSupervisor) defaultRequestSupervisor);
        RequestSupervisorSupport.configure((RequestManager) defaultRequestSupervisor);
        return defaultRequestSupervisor;
    }

    @Bean
    @ConditionalOnMissingBean(Mediator.class)
    public DefaultMediator defaultMediator(ApplicationContext applicationContext) {
        DefaultMediator defaultMediator = new DefaultMediator();
        MediatorSupport.configure(defaultMediator);
        MediatorSupport.configure(applicationContext);
        return defaultMediator;
    }


    @Bean
    public JpaRequestScheduleService jpaRequestScheduleService(
            RequestManager requestManager,
            Locker locker,
            @Value(CONFIG_KEY_4_REQUEST_COMPENSE_LOCKER_KEY)
            String compensationLockerKey,
            @Value(CONFIG_KEY_4_REQUEST_ARCHIVE_LOCKER_KEY)
            String archiveLockerKey,
            RequestScheduleProperties requestScheduleProperties,
            JdbcTemplate jdbcTemplate
    ) {
        JpaRequestScheduleService jpaRequestScheduleService = new JpaRequestScheduleService(
                requestManager,
                locker,
                compensationLockerKey,
                archiveLockerKey,
                requestScheduleProperties.isAddPartitionEnable(),
                jdbcTemplate
        );
        jpaRequestScheduleService.init();
        return jpaRequestScheduleService;
    }

    /**
     * Request定时补偿任务
     */
    @RequiredArgsConstructor
    @Service
    @EnableScheduling
    private static class __RequestScheduleLoader {

        private static final String CONFIG_KEY_4_COMPENSE_CRON = "${cap4j.ddd.application.request.schedule.compenseCron:${cap4j.ddd.application.request.schedule.compense-cron:0 * * * * ?}}";
        private static final String CONFIG_KEY_4_ARCHIVE_CRON = "${cap4j.ddd.application.request.schedule.archiveCron:${cap4j.ddd.application.request.schedule.archive-cron:0 0 2 * * ?}}";
        private static final String CONFIG_KEY_4_ADD_PARTITION_CRON = "${cap4j.ddd.application.request.schedule.addPartitionCron:${cap4j.ddd.application.request.schedule.add-partition-cron:0 0 0 * * ?}}";


        private final RequestScheduleProperties requestScheduleProperties;
        private final JpaRequestScheduleService scheduleService;

        @Scheduled(cron = CONFIG_KEY_4_COMPENSE_CRON)
        public void compensation() {
            if (scheduleService == null) return;
            scheduleService.compense(
                    requestScheduleProperties.getCompenseBatchSize(),
                    requestScheduleProperties.getCompenseMaxConcurrency(),
                    Duration.ofSeconds(requestScheduleProperties.getCompenseIntervalSeconds()),
                    Duration.ofSeconds(requestScheduleProperties.getCompenseMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ARCHIVE_CRON)
        public void archive() {
            if (scheduleService == null) return;
            scheduleService.archive(
                    requestScheduleProperties.getArchiveExpireDays(),
                    requestScheduleProperties.getArchiveBatchSize(),
                    Duration.ofSeconds(requestScheduleProperties.getArchiveMaxLockSeconds())
            );
        }

        @Scheduled(cron = CONFIG_KEY_4_ADD_PARTITION_CRON)
        public void addTablePartition() {
            scheduleService.addPartition();
        }
    }

}
