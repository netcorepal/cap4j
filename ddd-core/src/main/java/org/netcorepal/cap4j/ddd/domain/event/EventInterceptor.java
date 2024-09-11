package org.netcorepal.cap4j.ddd.domain.event;

import org.netcorepal.cap4j.ddd.domain.event.EventRecord;

/**
 * 事件拦截器
 *
 * @author binking338
 * @date 2024/8/30
 */
public interface EventInterceptor {

    /**
     * 持久化前
     *
     * @param event
     */
    void prePersist(EventRecord event);

    /**
     * 持久化后
     *
     * @param event
     */
    void postPersist(EventRecord event);

    /**
     * 发布前
     *
     * @param event
     */
    void preRelease(EventRecord event);

    /**
     * 发布后
     *
     * @param event
     */
    void postRelease(EventRecord event);

    /**
     * 发布异常
     *
     * @param throwable
     * @param event
     */
    void onException(Throwable throwable, EventRecord event);
}
