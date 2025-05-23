package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.env.Environment;
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
    private final RestTemplate restTemplate;
    private final String eventParam;
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
        String destination = event.getEventTopic();
        if (destination == null || destination.isEmpty()) {
            throw new DomainException(String.format("集成事件发布失败: %s 缺失topic", event.getId()));
        }
        destination = TextUtils.resolvePlaceholderWithCache(destination, environment);
        destination = destination.split("@")[0];
        List<String> callbackUrls = subscriberRegister.getCallbackUrls(destination);

        if (callbackUrls != null && !callbackUrls.isEmpty()) {
            Map<String, Object> uriParams = new HashMap<>();
            try {
                uriParams.put(eventParam, URLEncoder.encode(destination, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            executorService.execute(() -> {
                for (String callbackUrl : callbackUrls) {
                    try {

                        callbackUrl = callbackUrl.contains("?")
                                ? callbackUrl + "&" + eventParam + "={" + eventParam + "}"
                                : callbackUrl + "?" + eventParam + "={" + eventParam + "}";
                        ResponseEntity<HttpIntegrationEventSubscriberAdapter.OperationResponse> result
                                = restTemplate.postForEntity(callbackUrl, event.getPayload(), HttpIntegrationEventSubscriberAdapter.OperationResponse.class, uriParams);
                        if (result.getStatusCode().is2xxSuccessful()) {
                            if (result.getBody().isSuccess()) {
                                log.info(String.format("集成事件发送成功, %s", event.getId()));
                                publishCallback.onSuccess(event);
                            } else {
                                log.error(String.format("集成事件发布失败, %s (Consume)", event.getId()));
                                publishCallback.onException(event, new RuntimeException(result.getBody().getMessage()));
                            }
                        } else {
                            log.error(String.format("集成事件发布失败, %s (Server)", event.getId()));
                            publishCallback.onException(event, new RuntimeException(String.format("集成事件HTTP消费失败:%s", result.getStatusCode().toString())));
                        }
                    } catch (Throwable throwable) {
                        log.error(String.format("集成事件发布失败, %s (Client)", event.getId()), throwable);
                        publishCallback.onException(event, throwable);
                    }
                }
            });
        }
    }
}
