package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.domain.event.EventMessageInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriberManager;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 自动处理集成事件回调
 *
 * @author binking338
 * @date 2025/5/19
 */
@RequiredArgsConstructor
@Slf4j
public class HttpIntegrationEventSubscriberAdapter {
    private final EventSubscriberManager eventSubscriberManager;
    private final List<EventMessageInterceptor> eventMessageInterceptors;
    private final HttpIntegrationEventSubscriberRegister httpIntegrationEventSubscriberRegister;
    ;
    private final RestTemplate restTemplate;

    private final Environment environment;
    private final String scanPath;
    private final String applicationName;
    private final String httpBaseUrl;
    private final String httpSubscribePath;
    private final String httpConsumePath;

    public void init() {
        Set<Class<?>> classes = ScanUtils.findIntegrationEventClasses(scanPath);
        classes.stream().filter(cls -> {
            IntegrationEvent integrationEvent = cls.getAnnotation(IntegrationEvent.class);
            return !Objects.isNull(integrationEvent) && StringUtils.isNotEmpty(integrationEvent.value())
                    & !IntegrationEvent.NONE_SUBSCRIBER.equalsIgnoreCase(integrationEvent.subscriber());
        }).forEach(integrationEventClass -> {
            IntegrationEvent integrationEvent = integrationEventClass.getAnnotation(IntegrationEvent.class);
            boolean isRemote = integrationEvent.value() != null && integrationEvent.value().contains("@");
            String subscriber = StringUtils.isNotBlank(integrationEvent.subscriber()) ? integrationEvent.subscriber() : applicationName;
            subscriber = TextUtils.resolvePlaceholderWithCache(subscriber, environment);

            String target = integrationEvent.value().split("@")[0];
            target = TextUtils.resolvePlaceholderWithCache(target, environment);

            String event = target;
            String eventSourceRegisterUrl = isRemote
                    ? integrationEvent.value().split("@")[1]
                    : (httpBaseUrl + httpSubscribePath);
            String eventCallbackUrl = httpBaseUrl + httpConsumePath;

            if (!isRemote) {
                httpIntegrationEventSubscriberRegister.subscribe(event, subscriber, eventCallbackUrl);
            } else {
                try {
                    SubscribeRequest subscribeRequest = new SubscribeRequest();
                    subscribeRequest.setEvent(event);
                    subscribeRequest.setSubscriber(subscriber);
                    subscribeRequest.setCallbackUrl(eventCallbackUrl);
                    ResponseEntity<OperationResponse> response = restTemplate.postForEntity(eventSourceRegisterUrl, subscribeRequest, OperationResponse.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        if (response.getBody().isSuccess()) {
                            log.info(String.format("集成事件订阅成功, %s", integrationEvent.value()));
                        } else {
                            log.error(String.format("集成事件订阅失败, %s (Consume)", integrationEvent.value()));
                        }
                    } else {
                        log.error(String.format("集成事件订阅失败, %s (Server)", integrationEvent.value()));
                    }
                } catch (Throwable throwable) {
                    log.error(String.format("集成事件订阅失败, %s (Client)", integrationEvent.value()), throwable);
                    throw throwable;
                }
            }
            eventPayloadClassMap.put(event, integrationEventClass);
        });
    }

    public boolean consume(String event, String payloadJsonStr, Map<String, Object> headers) {
        try {
            if (!eventPayloadClassMap.containsKey(event)) {
                log.error(String.format("集成事件消费失败, %s : %s", event, payloadJsonStr));
            }
            Class<?> integrationEventClass = eventPayloadClassMap.get(event);
            Object eventPayload = JSON.parseObject(payloadJsonStr, integrationEventClass, Feature.SupportNonPublicField);
            if (getOrderedEventMessageInterceptors().isEmpty()) {
                eventSubscriberManager.dispatch(eventPayload);
            } else {
                Message<Object> message = new GenericMessage<>(eventPayload, new EventMessageInterceptor.ModifiableMessageHeaders(headers));
                getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.preSubscribe(message));
                // 拦截器可能修改消息，重新赋值
                eventPayload = message.getPayload();
                eventSubscriberManager.dispatch(eventPayload);
                getOrderedEventMessageInterceptors().forEach(interceptor -> interceptor.postSubscribe(message));
            }
            return true;
        } catch (Exception ex) {
            log.error(String.format("集成事件消费失败, %s : %s", event, payloadJsonStr), ex);
            return false;
        }
    }

    private List<EventMessageInterceptor> orderedEventMessageInterceptors = null;

    /**
     * 获取排序后的事件消息拦截器
     * 基于{@link org.springframework.core.annotation.Order}
     *
     * @return
     */
    private List<EventMessageInterceptor> getOrderedEventMessageInterceptors() {
        if (orderedEventMessageInterceptors == null) {
            orderedEventMessageInterceptors = new ArrayList<>(eventMessageInterceptors);
            orderedEventMessageInterceptors.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
        }
        return orderedEventMessageInterceptors;
    }

    private Map<String, Class<?>> eventPayloadClassMap = new HashMap<>();


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationResponse {
        private boolean success;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscribeRequest {
        private String event;
        private String subscriber;
        private String callbackUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnsubscribeRequest {
        private String event;
        private String subscriber;
    }
}
