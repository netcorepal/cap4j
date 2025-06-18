package org.netcorepal.cap4j.ddd.application.event;

import com.alibaba.fastjson.JSON;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 基于Http的集成事件发布器
 *
 * @author binking338
 * @date 2025/5/19
 */
@RequiredArgsConstructor
@Slf4j
public class HttpIntegrationEventPublisher implements IntegrationEventPublisher {
    private final HttpIntegrationEventSubscriberRegister subscriberRegister;
    private final Environment environment;
    private final String eventParamName;
    private final String eventIdParamName;
    private final int threadPoolSize;
    private final String threadFactoryClassName;

    private ExecutorService executorService = null;

    public void init() {
        if (StringUtils.isBlank(threadFactoryClassName)) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        } else {
            Class<?> threadFactoryClass = ClassUtils.getExistingClass(this.getClass().getClassLoader(), threadFactoryClassName);
            ThreadFactory threadFactory = (ThreadFactory) ClassUtils.newInstance(threadFactoryClass);
            if (threadFactory != null) {
                executorService = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
            } else {
                executorService = Executors.newFixedThreadPool(threadPoolSize);
            }
        }
    }

    @Override
    public void publish(EventRecord event, PublishCallback publishCallback) {
        String destination = event.getType();
        if (destination == null || destination.isEmpty()) {
            throw new DomainException(String.format("集成事件发布失败: %s 缺失topic", event.getId()));
        }
        destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
        destination = destination.split("@")[0];
        List<String> callbackUrls = subscriberRegister.getCallbackUrls(destination);

        if (callbackUrls != null && !callbackUrls.isEmpty()) {
            Map<String, Object> uriParams = new HashMap<>();
            try {
                uriParams.put(eventParamName, URLEncoder.encode(destination, StandardCharsets.UTF_8.name()));
                uriParams.put(eventIdParamName, URLEncoder.encode(event.getId(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            executorService.execute(() -> {
                try {
                    for (String callbackUrl : callbackUrls) {
                        Mediator.commands().async(HttpIntegrationEventCallbackTriggerCommand.Request.builder()
                                        .eventId(event.getId())
                                        .payload(event.getPayload())
                                        .url(callbackUrl)
                                        .uriParams(uriParams)
                                        .build());
                    }
                    publishCallback.onSuccess(event);
                } catch (Throwable throwable) {
                    log.error(String.format("集成事件发布失败, %s", event.getId()), throwable);
                    publishCallback.onException(event, throwable);
                }
            });
        }
    }


    /**
     * 集成事件回调触发命令
     */
    public static class HttpIntegrationEventCallbackTriggerCommand {
        @RequiredArgsConstructor
        public static class Handler implements Command<Request, Response> {
            private final RestTemplate restTemplate;

            @Override
            public Response exec(Request param) {
                for (Map.Entry<String, Object> entry : param.uriParams.entrySet()) {
                    param.url = param.url.contains("?")
                            ? param.url + "&" + entry.getKey() + "={" + entry.getKey() + "}"
                            : param.url + "?" + entry.getKey() + "={" + entry.getKey() + "}";
                }
                ResponseEntity<HttpIntegrationEventSubscriberAdapter.OperationResponse> result = null;
                try {
                    Object payload = param.getPayload();
                    String payloadJsonStr = payload == null ? null : JSON.toJSONString(payload);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", "application/json; charset=utf-8");
                    HttpEntity<byte[]> payloadJsonStrEntity = new HttpEntity<>(payloadJsonStr.getBytes(StandardCharsets.UTF_8.name()), headers);
                    result = restTemplate.postForEntity(param.url, payloadJsonStrEntity, HttpIntegrationEventSubscriberAdapter.OperationResponse.class, param.uriParams);
                } catch (Throwable throwable) {
                    log.error(String.format("集成事件触发失败, %s (Client)", param.getEventId()), throwable);
                    throw new RuntimeException(throwable);
                }
                if (result.getStatusCode().is2xxSuccessful()) {
                    if (result.getBody().isSuccess()) {
                        log.info(String.format("集成事件触发成功, %s", param.getEventId()));
                        return Response.builder().success(true).build();
                    } else {
                        log.error(String.format("集成事件触发失败, %s (Consume) %s", param.getEventId(), result.getBody().getMessage()));
                        throw new RuntimeException(result.getBody().getMessage());
                    }
                } else {
                    log.error(String.format("集成事件触发失败, %s (Server) 集成事件HTTP消费失败:%d", param.getEventId(), result.getStatusCode().value()));
                    throw new RuntimeException(String.format("集成事件HTTP消费失败:%d", result.getStatusCode().value()));
                }
            }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Request implements RequestParam<Response> {
            private String eventId;
            private Object payload;
            private String url;
            private Map<String, Object> uriParams;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Response {
            private boolean success;
        }
    }
}
