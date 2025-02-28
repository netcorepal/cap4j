package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.impl.IntergrationEventUnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.EventRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Configuration
@RequiredArgsConstructor
public class IntegrationEventAutoConfiguration {
    @Bean
    @Primary
    public DefaultIntegrationEventSupervisor defaultIntegrationEventSupervisor(
            EventPublisher eventPublisher,
            EventRecordRepository eventRecordRepository,
            IntegrationEventInterceptorManager integrationEventInterceptorManager,
            ApplicationEventPublisher applicationEventPublisher,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName
    ) {
        DefaultIntegrationEventSupervisor defaultIntegrationEventSupervisor = new DefaultIntegrationEventSupervisor(
                eventPublisher,
                eventRecordRepository,
                integrationEventInterceptorManager,
                applicationEventPublisher,
                svcName
        );

        IntegrationEventSupervisorSupport.configure((IntegrationEventSupervisor) defaultIntegrationEventSupervisor);
        IntegrationEventSupervisorSupport.configure((IntegrationEventManager) defaultIntegrationEventSupervisor);
        return defaultIntegrationEventSupervisor;
    }

    @Bean
    public IntergrationEventUnitOfWorkInterceptor intergrationEventUnitOfWorkInterceptor(
            IntegrationEventManager integrationEventManager
    ) {
        IntergrationEventUnitOfWorkInterceptor intergrationEventUnitOfWorkInterceptor = new IntergrationEventUnitOfWorkInterceptor(integrationEventManager);
        return intergrationEventUnitOfWorkInterceptor;
    }
}
