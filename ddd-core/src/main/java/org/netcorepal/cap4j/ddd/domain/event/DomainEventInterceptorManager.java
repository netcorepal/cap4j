package org.netcorepal.cap4j.ddd.domain.event;

import java.util.Set;

/**
 * 领域事件拦截器管理器
 *
 * @author binking338
 * @date 2024/9/12
 */
public interface DomainEventInterceptorManager {
    /**
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     * @return
     */
    Set<DomainEventInterceptor> getOrderedDomainEventInterceptors();

    /**
     *
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     * @return
     */
    Set<EventInterceptor> getOrderedEventInterceptors4DomainEvent();
}
