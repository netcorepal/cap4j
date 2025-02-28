package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.impl.IntergrationEventUnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.netcorepal.cap4j.ddd.share.Constants.*;

/**
 * 基于RabbitMq的领域事件（集成事件）实现自动配置类
 *
 * <p>
 * RabbitMq：依赖spring-boot-starter-amqp后会自动装载，如果不配做参数会报错，所以不适用RabbitMQ，需要排查spring-boot-starter-amqp包
 * </p>
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@AutoConfigureAfter(RabbitAutoConfiguration.class)
public class RabbitMqEventAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(IntegrationEventPublisher.class)
    public RabbitMqIntegrationEventPublisher rabbitMqIntegrationEventPublisher(
            RabbitTemplate rabbitTemplate,
            Environment environment
    ) {
        RabbitMqIntegrationEventPublisher rocketMqDomainEventPublisher = new RabbitMqIntegrationEventPublisher(
                rabbitTemplate,
                environment);
        return rocketMqDomainEventPublisher;
    }

    @Bean
    public RabbitMqDomainEventSubscriberAdapter rabbitMqDomainEventSubscriberAdapter(
            EventSubscriberManager eventSubscriberManager,
            List<EventMessageInterceptor> eventMessageInterceptors,
            @Autowired(required = false)
            MQConsumerConfigure mqConsumerConfigure,
            ConnectionFactory connectionFactory,
            Environment environment,
            EventProperties eventProperties,
            @Value(CONFIG_KEY_4_SVC_NAME)
            String svcName,
            @Value(CONFIG_KEY_4_ROCKETMQ_MSG_CHARSET)
            String msgCharset
    ) {
        RabbitMqDomainEventSubscriberAdapter rocketMqDomainEventSubscriberAdapter = new RabbitMqDomainEventSubscriberAdapter(
                eventSubscriberManager,
                eventMessageInterceptors,
                mqConsumerConfigure,
                connectionFactory,
                environment,
                eventProperties.getEventScanPackage(),
                svcName,
                msgCharset
        );
        rocketMqDomainEventSubscriberAdapter.init();
        return rocketMqDomainEventSubscriberAdapter;
    }
}
