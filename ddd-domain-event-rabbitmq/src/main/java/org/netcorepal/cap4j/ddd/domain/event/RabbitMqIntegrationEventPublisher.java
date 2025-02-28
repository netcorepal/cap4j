package org.netcorepal.cap4j.ddd.domain.event;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.netcorepal.cap4j.ddd.application.event.IntegrationEventPublisher;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;


/**
 * 基于RabbitMq的领域事件发布器
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqIntegrationEventPublisher implements IntegrationEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final Environment environment;

    @Override
    public void publish(EventRecord event, PublishCallback publishCallback) {
        try {
            // 事件的主題，
            String destination = event.getEventTopic();
            destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
            if (destination == null || destination.isEmpty()) {
                throw new DomainException(String.format("集成事件发布失败: %s 缺失topic", event.getId()));
            }
            String message = JSON.toJSONString(event.getMessage());
            // MQ消息通道
            rabbitTemplate.convertAndSend("crungoo.ddd.event",
                    "crungoo.ddd.routing",
                    message,
                    new DomainEventSendCallback(event, publishCallback));
        } catch (Exception ex) {
            log.error(String.format("集成事件发布失败: %s", event.getId()), ex);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class DomainEventSendCallback implements MessagePostProcessor {
        private final EventRecord event;
        private final PublishCallback publishCallback;


        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                log.info(String.format("集成事件发送成功, %s msgId=%s",
                        event.getId(),
                        message.getMessageProperties().getMessageId()));
                publishCallback.onSuccess(event);
                return message;
            } catch (Throwable throwable) {
                log.error("回调失败（事件发送成功）", throwable);
                publishCallback.onException(event, throwable);
            }
            return null;
        }
    }
}
