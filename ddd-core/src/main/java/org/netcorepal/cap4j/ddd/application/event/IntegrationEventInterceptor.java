package org.netcorepal.cap4j.ddd.application.event;

import org.netcorepal.cap4j.ddd.share.EventInterceptor;

import java.time.LocalDateTime;

/**
 * 集成事件拦截器
 *
 * @author binking338
 * @date 2024/8/29
 */
public interface IntegrationEventInterceptor extends EventInterceptor {

    /**
     * 调用通知时
     *
     * @param eventPayload
     * @param schedule
     */
    void onNotify(Object eventPayload, LocalDateTime schedule);
}
