package org.netcorepal.cap4j.ddd.application.event;

import lombok.Data;

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
     * 获取事件列表
     *
     * @return {@link List}<{@link String}>
     */
    List<String> events();

    /**
     * 获取订阅者列表
     *
     * @param event 事件
     * @return {@link List}<{@link String}>
     */
    List<SubscriberInfo> subscribers(String event);

    /**
     * 订阅者信息
     */
    @Data
    public static class SubscriberInfo {
        private String event;
        private String subscriber;
        private String callbackUrl;
    }
}
