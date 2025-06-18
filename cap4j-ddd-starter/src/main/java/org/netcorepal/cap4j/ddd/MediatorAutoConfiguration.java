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
public class MediatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Mediator.class)
    public DefaultMediator defaultMediator(ApplicationContext applicationContext) {
        DefaultMediator defaultMediator = new DefaultMediator();
        MediatorSupport.configure(defaultMediator);
        MediatorSupport.configure(applicationContext);
        return defaultMediator;
    }

}
