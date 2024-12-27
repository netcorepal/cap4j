package org.netcorepal.cap4j.ddd.domain.web;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.impl.DefaultIntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.application.impl.JpaUnitOfWork;
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 领域事件上下文清理拦截器
 *
 * @author binking338
 * @date 2023-03-10
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class ClearDomainContextInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        JpaUnitOfWork.reset();
        DefaultDomainEventSupervisor.reset();
        DefaultIntegrationEventSupervisor.reset();
    }
}
