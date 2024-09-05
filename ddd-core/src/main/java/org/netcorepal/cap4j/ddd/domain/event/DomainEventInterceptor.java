package org.netcorepal.cap4j.ddd.domain.event;

import org.netcorepal.cap4j.ddd.share.EventInterceptor;

import java.time.LocalDateTime;

/**
 * 领域事件拦截器
 *
 * @author binking338
 * @date 2024/8/27
 */
public interface DomainEventInterceptor extends EventInterceptor {
    /**
     * 附加
     * @param eventPayload
     * @param entity
     * @param schedule
     */
    void onAttach(Object eventPayload, Object entity, LocalDateTime schedule);

    /**
     * 解除附加
     * @param eventPayload
     * @param entity
     */
    void onDetach(Object eventPayload, Object entity);
}
