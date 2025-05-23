package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.event.configure.HttpIntegrationEventAdapterProperties;
import org.netcorepal.cap4j.ddd.application.event.configure.RabbitMqIntegrationEventAdapterProperties;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultHttpIntegrationEventSubscriberRegister;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.event.impl.IntergrationEventUnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.application.event.persistence.EventHttpSubscriberJpaRepository;
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
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    @Configuration
    @ConditionalOnProperty(name = "cap4j.ddd.integration.event.http.enabled", havingValue = "true")
    @ConditionalOnClass(name = "org.netcorepal.cap4j.ddd.application.event.HttpIntegrationEventSubscriberAdapter")
    @Slf4j
    public static class HttpAdapterLauncher {
        public static final String CONSUME_EVENT_PARAM = "event";
        public static final String CONSUME_PATH = "/cap4j/integration-event/http/consume";
        public static final String SUBSCRIBE_PATH = "/cap4j/integration-event/http/subscribe";
        public static final String UNSUBSCRIBE_PATH = "/cap4j/integration-event/http/unsubscribe";

        @Configuration
        @ConditionalOnClass(name = "org.netcorepal.cap4j.ddd.application.event.JpaHttpIntegrationEventSubscriberRegister")
        @EnableJpaRepositories(basePackages = {
                "org.netcorepal.cap4j.ddd.application.event.persistence"
        })
        @EntityScan(basePackages = {
                "org.netcorepal.cap4j.ddd.application.event.persistence"
        })
        @Slf4j
        public static class JpaHttpIntegrationEventSubscriberRegisterLauncher {
            @Bean
            public JpaHttpIntegrationEventSubscriberRegister jpaHttpIntegrationEventSubscriberRegister(
                    EventHttpSubscriberJpaRepository eventHttpSubscriberJpaRepository
            ) {
                return new JpaHttpIntegrationEventSubscriberRegister(eventHttpSubscriberJpaRepository);
            }
        }

        @Bean
        @ConditionalOnMissingBean(HttpIntegrationEventSubscriberRegister.class)
        public DefaultHttpIntegrationEventSubscriberRegister httpIntegrationEventSubscriberRegister(
        ) {
            return new DefaultHttpIntegrationEventSubscriberRegister();
        }

        @Bean
        @ConditionalOnMissingBean(IntegrationEventPublisher.class)
        public HttpIntegrationEventPublisher httpIntegrationEventPublisher(
                HttpIntegrationEventSubscriberRegister subscriberRegister,
                Environment environment,
                HttpIntegrationEventAdapterProperties httpIntegrationEventAdapterProperties
        ) {
            HttpIntegrationEventPublisher httpIntegrationEventPublisher = new HttpIntegrationEventPublisher(
                    subscriberRegister,
                    environment,
                    new RestTemplate(),
                    CONSUME_EVENT_PARAM,
                    httpIntegrationEventAdapterProperties.getPublishThreadPoolSize(),
                    httpIntegrationEventAdapterProperties.getPublishThreadFactoryClassName());
            httpIntegrationEventPublisher.init();
            return httpIntegrationEventPublisher;
        }

        @Bean
        public HttpIntegrationEventSubscriberAdapter httpIntegrationEventSubscriberAdapter(
                EventSubscriberManager eventSubscriberManager,
                List<EventMessageInterceptor> eventMessageInterceptors,
                HttpIntegrationEventSubscriberRegister httpIntegrationEventSubscriberRegister,
                EventProperties eventProperties,
                HttpIntegrationEventAdapterProperties httpIntegrationEventAdapterProperties,
                Environment environment,
                @Value(CONFIG_KEY_4_SVC_NAME)
                String svcName,
                @Value("${server.port:80}")
                String serverPort,
                @Value("${server.servlet.context-path:}")
                String serverServletContentPath
        ) {
            String baseUrl = String.format("http://localhost:%s%s", serverPort, serverServletContentPath);
            HttpIntegrationEventSubscriberAdapter httpIntegrationEventSubscriberAdapter = new HttpIntegrationEventSubscriberAdapter(
                    eventSubscriberManager,
                    eventMessageInterceptors,
                    httpIntegrationEventSubscriberRegister,
                    new RestTemplate(),
                    environment,
                    eventProperties.getEventScanPackage(),
                    svcName,
                    baseUrl,
                    SUBSCRIBE_PATH,
                    CONSUME_PATH);
            httpIntegrationEventSubscriberAdapter.init();
            return httpIntegrationEventSubscriberAdapter;
        }

        @ConditionalOnWebApplication
        @Bean(name = SUBSCRIBE_PATH)
        public HttpRequestHandler httpIntegrationEventSubscribeHandler(
                HttpIntegrationEventSubscriberRegister httpIntegrationEventSubscriberRegister,
                @Value("${server.port:80}")
                String serverPort,
                @Value("${server.servlet.context-path:}")
                String serverServletContentPath
        ) {
            log.info("IntegrationEvent subscribe URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/integration-event/http/subscribe");
            return (req, res) -> {
                Scanner scanner = new Scanner(req.getInputStream(), StandardCharsets.UTF_8.name());
                StringBuilder stringBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                }
                HttpIntegrationEventSubscriberAdapter.SubscribeRequest subscribeRequest = JSON.parseObject(stringBuilder.toString(), HttpIntegrationEventSubscriberAdapter.SubscribeRequest.class);
                boolean success = httpIntegrationEventSubscriberRegister.subscribe(
                        subscribeRequest.getEvent(),
                        subscribeRequest.getSubscriber(),
                        subscribeRequest.getCallbackUrl()
                );
                HttpIntegrationEventSubscriberAdapter.OperationResponse operationResponse = HttpIntegrationEventSubscriberAdapter.OperationResponse
                        .builder()
                        .success(success)
                        .message(success ? "ok" : "fail")
                        .build();
                res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                res.setContentType("application/json; charset=utf-8");
                res.getWriter().write(JSON.toJSONString(operationResponse));
                res.getWriter().flush();
                res.getWriter().close();
            };
        }

        @ConditionalOnWebApplication
        @Bean(name = UNSUBSCRIBE_PATH)
        public HttpRequestHandler httpIntegrationEventUnsubscribeHandler(
                HttpIntegrationEventSubscriberRegister httpIntegrationEventSubscriberRegister,
                @Value("${server.port:80}")
                String serverPort,
                @Value("${server.servlet.context-path:}")
                String serverServletContentPath
        ) {
            log.info("IntegrationEvent unsubscribe URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/integration-event/http/unsubscribe");
            return (req, res) -> {
                Scanner scanner = new Scanner(req.getInputStream(), StandardCharsets.UTF_8.name());
                StringBuilder stringBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                }
                HttpIntegrationEventSubscriberAdapter.UnsubscribeRequest unsubscribeRequest = JSON.parseObject(stringBuilder.toString(), HttpIntegrationEventSubscriberAdapter.UnsubscribeRequest.class);
                boolean success = httpIntegrationEventSubscriberRegister.unsubscribe(
                        unsubscribeRequest.getEvent(),
                        unsubscribeRequest.getSubscriber()
                );
                HttpIntegrationEventSubscriberAdapter.OperationResponse operationResponse = HttpIntegrationEventSubscriberAdapter.OperationResponse
                        .builder()
                        .success(success)
                        .message(success ? "ok" : "fail")
                        .build();
                res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                res.setContentType("application/json; charset=utf-8");
                res.getWriter().write(JSON.toJSONString(operationResponse));
                res.getWriter().flush();
                res.getWriter().close();
            };
        }

        @ConditionalOnWebApplication
        @Bean(name = CONSUME_PATH)
        public HttpRequestHandler httpIntegrationEventConsumeHandler(
                HttpIntegrationEventSubscriberAdapter httpIntegrationEventSubscriberAdapter,
                @Value("${server.port:80}")
                String serverPort,
                @Value("${server.servlet.context-path:}")
                String serverServletContentPath
        ) {
            log.info("IntegrationEvent consume URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/integration-event/http/consume");
            return (req, res) -> {
                Scanner scanner = new Scanner(req.getInputStream(), StandardCharsets.UTF_8.name());
                StringBuilder stringBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                }
                Map<String, Object> headers = new HashMap<>();
                String event = req.getParameter(CONSUME_EVENT_PARAM);
                boolean success = httpIntegrationEventSubscriberAdapter.consume(
                        event, stringBuilder.toString(), headers
                );
                HttpIntegrationEventSubscriberAdapter.OperationResponse operationResponse = HttpIntegrationEventSubscriberAdapter.OperationResponse
                        .builder()
                        .success(success)
                        .message(success ? "ok" : "fail")
                        .build();

                res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                res.setContentType("application/json; charset=utf-8");
                res.getWriter().write(JSON.toJSONString(operationResponse));
                res.getWriter().flush();
                res.getWriter().close();
            };
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.netcorepal.cap4j.ddd.application.event.RocketMqIntegrationEventSubscriberAdapter")
    @ImportAutoConfiguration(org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class)
    @Slf4j
    public static class RocketMqAdapterLauncher {

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
            log.info("集成事件适配类型：RocketMQ");
            return rocketMqIntegrationEventSubscriberAdapter;
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.netcorepal.cap4j.ddd.application.event.RabbitMqIntegrationEventSubscriberAdapter")
    @Slf4j
    public static class RabbitMqAdapterLauncher {
        @Bean
        @ConditionalOnProperty(name = "spring.rabbitmq.host")
        @ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
        @ConditionalOnMissingBean(IntegrationEventPublisher.class)
        public RabbitMqIntegrationEventPublisher rabbitMqIntegrationEventPublisher(
                RabbitTemplate rabbitTemplate,
                ConnectionFactory connectionFactory,
                Environment environment,
                RabbitMqIntegrationEventAdapterProperties rabbitMqIntegrationEventAdapterProperties
        ) {
            RabbitMqIntegrationEventPublisher publisher = new RabbitMqIntegrationEventPublisher(
                    rabbitTemplate,
                    connectionFactory,
                    environment,
                    rabbitMqIntegrationEventAdapterProperties.getPublishThreadPoolSize(),
                    rabbitMqIntegrationEventAdapterProperties.getPublishThreadFactoryClassName(),
                    rabbitMqIntegrationEventAdapterProperties.isAutoDeclareExchange(),
                    rabbitMqIntegrationEventAdapterProperties.getDefaultExchangeType());
            publisher.init();
            return publisher;
        }

        @Bean
        @ConditionalOnProperty(name = "spring.rabbitmq.host")
        @ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
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
                RabbitMqIntegrationEventAdapterProperties rabbitMqIntegrationEventAdapterProperties
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
                    rabbitMqIntegrationEventAdapterProperties.isAutoDeclareQueue()
            );
            adapter.init();
            log.info("集成事件适配类型：RabbitMQ");
            return adapter;
        }
    }
}
