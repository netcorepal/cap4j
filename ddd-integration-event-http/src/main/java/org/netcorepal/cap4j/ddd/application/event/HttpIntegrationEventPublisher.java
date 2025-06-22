package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.event.commands.IntegrationEventHttpCallbackTriggerCommand;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.core.env.Environment;
import org.springframework.objenesis.instantiator.util.ClassUtils;

import java.util.List;
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
        List<HttpIntegrationEventSubscriberRegister.SubscriberInfo> subscribers = subscriberRegister.subscribers(destination);

        if (subscribers != null && !subscribers.isEmpty()) {
            String eventType = destination;
            executorService.execute(() -> {
                try {
                    for (HttpIntegrationEventSubscriberRegister.SubscriberInfo subscriber : subscribers) {
                        Mediator.commands().async(IntegrationEventHttpCallbackTriggerCommand.Request.builder()
                                .url(subscriber.getCallbackUrl())
                                .uuid(event.getId())
                                .event(eventType)
                                .payload(event.getPayload())
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
}
