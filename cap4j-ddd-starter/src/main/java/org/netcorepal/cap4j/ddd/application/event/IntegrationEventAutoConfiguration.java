package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.event.configure.RabbitMqProperties;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.impl.IntergrationEventUnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventMessageInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.domain.event.EventRecordRepository;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * @author binking338
 * @date 2023/9/10
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

    @Bean
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @ConditionalOnMissingBean(IntegrationEventPublisher.class)
    public RocketMqIntegrationEventPublisher rocketMqIntegrationEventPublisher(
            RocketMQTemplate rocketMQTemplate,
            Environment environment
    ) {
        RocketMqIntegrationEventPublisher rocketMqIntegrationEventPublisher = new RocketMqIntegrationEventPublisher(
                rocketMQTemplate,
                environment);
        return rocketMqIntegrationEventPublisher;
    }

    @Bean
    @ConditionalOnProperty(name = "rocketmq.name-server")
    public RocketMqIntegrationEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter(
            EventSubscriberManager eventSubscriberManager,
            List<EventMessageInterceptor> eventMessageInterceptors,
            @Autowired(required = false)
            RocketMqIntegrationEventConfigure rocketMqIntegrationEventConfigure,
            Environment environment,
            EventProperties eventProperties,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_ROCKETMQ_NAME_SERVER)
            String defaultNameSrv,
            @Value(CONFIG_KEY_4_ROCKETMQ_MSG_CHARSET)
            String msgCharset
    ) {
        RocketMqIntegrationEventSubscriberAdapter rocketMqIntegrationEventSubscriberAdapter = new RocketMqIntegrationEventSubscriberAdapter(
                eventSubscriberManager,
                eventMessageInterceptors,
                rocketMqIntegrationEventConfigure,
                environment,
                eventProperties.getEventScanPackage(),
                svcName,
                defaultNameSrv,
                msgCharset
        );
        rocketMqIntegrationEventSubscriberAdapter.init();
        return rocketMqIntegrationEventSubscriberAdapter;
    }


    @Bean
    @ConditionalOnProperty(name = "spring.rabbitmq.host")
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
    public RabbitMqIntegrationEventPublisher rabbitMqIntegrationEventPublisher(
            RabbitTemplate rabbitTemplate,
            ConnectionFactory connectionFactory,
            Environment environment,
            RabbitMqProperties rabbitMqProperties
    ) {
        return new RabbitMqIntegrationEventPublisher(
                rabbitTemplate,
                connectionFactory,
                environment,
                rabbitMqProperties.getPublishThreadPoolSize(),
                rabbitMqProperties.isAutoDeclareExchange(),
                rabbitMqProperties.getDefaultExchangeType());
    }

    @Bean
    @ConditionalOnProperty(name = "spring.rabbitmq.host")
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
    public RabbitMqIntegrationEventSubscriberAdapter rabbitMqIntegrationEventSubscriberAdapter(
            EventSubscriberManager eventSubscriberManager,
            List<EventMessageInterceptor> eventMessageInterceptors,
            @Autowired(required = false)
            RabbitMqIntegrationEventConfigure rabbitMqIntegrationEventConfigure,
            SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory,
            ConnectionFactory connectionFactory,
            Environment environment,
            EventProperties eventProperties,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_ROCKETMQ_MSG_CHARSET)
            String msgCharset,
            RabbitMqProperties rabbitMqProperties
    ) {
        RabbitMqIntegrationEventSubscriberAdapter adapter = new RabbitMqIntegrationEventSubscriberAdapter(
                eventSubscriberManager,
                eventMessageInterceptors,
                rabbitMqIntegrationEventConfigure,
                simpleRabbitListenerContainerFactory,
                connectionFactory,
                environment,
                eventProperties.getEventScanPackage(),
                svcName,
                msgCharset,
                rabbitMqProperties.isAutoDeclareQueue()
        );
        adapter.init();
        return adapter;
    }

}
