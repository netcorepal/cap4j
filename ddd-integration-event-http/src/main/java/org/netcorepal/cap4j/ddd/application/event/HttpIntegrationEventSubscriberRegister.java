package org.netcorepal.cap4j.ddd.application.event;

import java.util.List;

/**
 * 集成事件订阅注册器
 *
 * @author binking338
 * @date 2025/5/19
 */
public interface HttpIntegrationEventSubscriberRegister {
    /**
     * 订阅
     *
     * @param event       事件
     * @param subscriber  订阅者
     * @param callbackUrl 回调地址
     * @return {@link Boolean}
     */
    boolean subscribe(String event, String subscriber, String callbackUrl);

    /**
     * 取消订阅
     *
     * @param event       事件
     * @param subscriber  订阅者
     * @return {@link Boolean}
     */
    boolean unsubscribe(String event, String subscriber);

    /**
     * 获取回调地址
     *
     * @param event 事件
     * @return {@link List}<{@link String}>
     */
    List<String> getCallbackUrls(String event);
}
