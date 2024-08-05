package org.netcorepal.cap4j.ddd.domain.web;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.repo.UnitOfWork;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    private final UnitOfWork unitOfWork;
    private final DomainEventSupervisor domainEventSupervisor;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        unitOfWork.reset();
        domainEventSupervisor.reset();
    }
}
