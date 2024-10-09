package org.netcorepal.cap4j.ddd.domain.event;

import java.util.Set;

/**
 * 事件消息拦截器管理器
 *
 * @author binking338
 * @date 2024/9/12
 */
public interface EventMessageInterceptorManager {
    /**
     * 拦截器基于 {@link org.springframework.core.annotation.Order} 排序
     * @return
     */
    Set<EventMessageInterceptor> getOrderedEventMessageInterceptors();
}
