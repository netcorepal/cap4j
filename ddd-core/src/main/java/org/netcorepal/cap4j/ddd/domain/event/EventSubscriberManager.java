package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 领域事件订阅管理器接口
 *
 * @author binking338
 * @date 2023/8/13
 */
public interface EventSubscriberManager {
    /**
     * 订阅事件
     *
     * @param eventPayloadClass
     * @param subscriber
     * @return
     */
    boolean subscribe(Class<?> eventPayloadClass, EventSubscriber<?> subscriber);

    /**
     * 取消订阅
     *
     * @param eventPayloadClass
     * @param subscriber
     * @return
     */
    boolean unsubscribe(Class<?> eventPayloadClass, EventSubscriber<?> subscriber);

    /**
     * 分发事件到所有订阅者
     *
     * @param eventPayload
     */
    void dispatch(Object eventPayload);
}
