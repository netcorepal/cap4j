package org.netcorepal.cap4j.ddd.application.event.impl;

import org.netcorepal.cap4j.ddd.application.event.HttpIntegrationEventSubscriberRegister;

import java.util.*;

/**
 * 集成事件订阅注册器默认实现
 *
 * @author binking338
 * @date 2025/5/21
 */
public class DefaultHttpIntegrationEventSubscriberRegister implements HttpIntegrationEventSubscriberRegister {
    Map<String, Map<String, String>> subscriberMap = new HashMap<>();

    @Override
    public boolean subscribe(String event, String subscriber, String callbackUrl) {
        Map<String, String> eventSubscriberMap = subscriberMap.computeIfAbsent(event, k -> new HashMap<>());
        if (eventSubscriberMap.containsKey(subscriber)) {
            return false;
        }
        eventSubscriberMap.put(subscriber, callbackUrl);
        return true;
    }

    @Override
    public boolean unsubscribe(String event, String subscriber) {
        if(subscriberMap.containsKey(event) && subscriberMap.get(event).containsKey(subscriber)){
            subscriberMap.get(event).remove(subscriber);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getCallbackUrls(String event) {
        return subscriberMap.containsKey(event)
                ? new ArrayList<>(subscriberMap.get(event).values())
                : Collections.emptyList();
    }
}
