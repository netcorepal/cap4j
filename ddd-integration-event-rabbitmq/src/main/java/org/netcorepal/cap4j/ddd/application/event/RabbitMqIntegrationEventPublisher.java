package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.objenesis.instantiator.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * 基于RabbitMq的集成事件发布器
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqIntegrationEventPublisher implements IntegrationEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final Environment environment;
    private final int threadPoolSize;
    private final String threadFactoryClassName;
    private final boolean autoDeclareExchange;
    private final String defaultExchangeType;

    private ExecutorService executorService = null;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(threadFactoryClassName)) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        } else {
            Class<?> threadFactoryClass = ClassUtils.getExistingClass(this.getClass().getClassLoader(), threadFactoryClassName);
            ThreadFactory threadFactory =  (ThreadFactory) ClassUtils.newInstance(threadFactoryClass);
            if (threadFactory != null) {
                executorService = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
            } else {
                executorService = Executors.newFixedThreadPool(threadPoolSize);
            }
        }
    }

    @Override
    public void publish(EventRecord event, PublishCallback publishCallback) {
        try {
            // 事件的主題
            String destination = event.getEventTopic();
            destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
            if (destination == null || destination.isEmpty()) {
                throw new DomainException(String.format("集成事件发布失败: %s 缺失topic", event.getId()));
            }
            String exchange = destination.lastIndexOf(':') > 0 ? destination.substring(0, destination.lastIndexOf(':')) : destination;
            String tag = destination.lastIndexOf(':') > 0 ? destination.substring(destination.lastIndexOf(':') + 1) : "";
            String message = JSON.toJSONString(event.getMessage());
            if (autoDeclareExchange) {
                tryDeclareExchange(exchange, defaultExchangeType);
            }
            // MQ消息通道
            executorService.execute(() -> {
                rabbitTemplate.convertAndSend(exchange,
                        tag,
                        message,
                        new IntegrationEventSendCallback(event, publishCallback));
            });
        } catch (Exception ex) {
            log.error(String.format("集成事件发布失败: %s", event.getId()), ex);
        }
    }

    private void tryDeclareExchange(String exchange, String exchangeType) {
        try {
            Connection connection = connectionFactory.createConnection();
            Channel channel = connection.createChannel(false);
            channel.exchangeDeclare(exchange, exchangeType, true, false, null);
            channel.close();
            connection.close();
        } catch (Exception e) {
            log.error("创建消息交换机失败", e);
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class IntegrationEventSendCallback implements MessagePostProcessor {
        private final EventRecord event;
        private final PublishCallback publishCallback;

        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            log.info(String.format("集成事件发送成功, %s", event.getId()));
            message.getMessageProperties().setMessageId(event.getId());
            try {
                publishCallback.onSuccess(event);
            } catch (Throwable throwable) {
                log.error("回调失败（事件发送成功）", throwable);
                publishCallback.onException(event, throwable);
            }
            return message;
        }
    }
}
