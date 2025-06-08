package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.env.Environment;

import static com.alibaba.fastjson.serializer.SerializerFeature.IgnoreNonFieldGetter;
import static com.alibaba.fastjson.serializer.SerializerFeature.SkipTransientField;

/**
 * 基于RocketMq的集成事件发布器
 * 如下配置需配置好，保障RocketMqTemplate被初始化
 * ## rocketmq
 * #rocketmq.name-server = myrocket.nameserver:9876
 * #rocketmq.producer.group=${spring.application.name}
 *
 * @author binking338
 * @date 2023/8/13
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqIntegrationEventPublisher implements IntegrationEventPublisher {
    private final RocketMQTemplate rocketMQTemplate;
    private final Environment environment;

    @Override
    public void publish(EventRecord event, PublishCallback publishCallback) {
        try {
            String destination = event.getType();
            destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
            if (destination == null || destination.isEmpty()) {
                throw new DomainException(String.format("集成事件发布失败: %s 缺失topic", event.getId()));
            }
            // MQ消息通道
            rocketMQTemplate.asyncSend(
                    destination,
                    event.getMessage(),
                    new IntegrationEventSendCallback(
                            event,
                            publishCallback
                    )
            );
        } catch (Exception ex) {
            log.error(String.format("集成事件发布失败: %s", event.getId()), ex);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class IntegrationEventSendCallback implements SendCallback {
        private final EventRecord event;
        private final PublishCallback publishCallback;

        @Override
        public void onSuccess(SendResult sendResult) {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                log.info(String.format("集成事件发送成功, %s msgId=%s",
                        event.getId(),
                        sendResult.getMsgId()));
                publishCallback.onSuccess(event);
            } catch (Throwable throwable) {
                log.error("回调失败（事件发送成功）", throwable);
                publishCallback.onException(event, throwable);
            }
        }

        @Override
        public void onException(Throwable throwable) {
            if (event == null) {
                throw new DomainException("集成事件为NULL");
            }
            try {
                String msg = String.format("集成事件发送失败, %s body=%s",
                        event.getId(),
                        JSON.toJSONString(event.getPayload()), IgnoreNonFieldGetter, SkipTransientField);
                log.error(msg, throwable);
                publishCallback.onException(event, throwable);
            } catch (Throwable throwable1) {
                log.error("回调失败（事件发送异常）", throwable1);
            }
        }
    }
}
