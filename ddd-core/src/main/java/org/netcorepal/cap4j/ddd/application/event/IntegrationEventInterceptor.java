package org.netcorepal.cap4j.ddd.application.event;

import org.netcorepal.cap4j.ddd.domain.event.EventInterceptor;

import java.time.LocalDateTime;

/**
 * 集成事件拦截器
 *
 * @author binking338
 * @date 2024/8/29
 */
public interface IntegrationEventInterceptor extends EventInterceptor {
    /**
     * 附加
     * @param eventPayload
     * @param schedule
     */
    void onAttach(Object eventPayload, LocalDateTime schedule);

    /**
     * 解除附加
     * @param eventPayload
     */
    void onDetach(Object eventPayload);
}
