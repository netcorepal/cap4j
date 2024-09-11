package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

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
@ConditionalOnProperty(name = "rocketmq.name-server")
@RequiredArgsConstructor
public class RocketMqEventAutoConfiguration {

    @Bean
    @Primary
    public DefaultIntegrationEventSupervisor defaultIntegrationEventSupervisor(
        EventPublisher eventPublisher,
        EventRecordRepository eventRecordRepository,
        List<IntegrationEventInterceptor> integrationEventInterceptors,
        ApplicationEventPublisher applicationEventPublisher,
        @Value(CONFIG_KEY_4_SVC_NAME)
        String svcName
    ){
        DefaultIntegrationEventSupervisor defaultIntegrationEventSupervisor = new DefaultIntegrationEventSupervisor(
                eventPublisher,
                eventRecordRepository,
                integrationEventInterceptors,
                applicationEventPublisher,
                svcName
        );

        IntegrationEventSupervisorSupport.configure((IntegrationEventSupervisor) defaultIntegrationEventSupervisor);
        IntegrationEventSupervisorSupport.configure((IntegrationEventManager) defaultIntegrationEventSupervisor);
        return defaultIntegrationEventSupervisor;
    }

    @Bean
    public RocketMqIntegrationEventPublisher rocketMqIntegrationEventPublisher(
        RocketMQTemplate rocketMQTemplate,
        Environment environment
    ) {
        RocketMqIntegrationEventPublisher rocketMqDomainEventPublisher = new RocketMqIntegrationEventPublisher(
                rocketMQTemplate,
                environment);
        return rocketMqDomainEventPublisher;
    }

    @Bean
    public RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter(
            EventSubscriberManager eventSubscriberManager,
            List<EventMessageInterceptor> eventMessageInterceptors,
            @Autowired(required = false)
            MQConsumerConfigure mqConsumerConfigure,
            Environment environment,
            EventProperties eventProperties,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_ROCKETMQ_NAME_SERVER)
            String defaultNameSrv,
            @Value(CONFIG_KEY_4_ROCKETMQ_MSG_CHARSET)
            String msgCharset
    ) {
        RocketMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter = new RocketMqDomainEventSubscriberAdapter(
                eventSubscriberManager,
                eventMessageInterceptors,
                mqConsumerConfigure,
                environment,
                eventProperties.getEventScanPackage(),
                svcName,
                defaultNameSrv,
                msgCharset
        );
        rocketMqDomainEventSubscriberAdapter.init();
        return rocketMqDomainEventSubscriberAdapter;
    }
}
