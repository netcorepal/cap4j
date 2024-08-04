package org.ddd.domain.web;

import lombok.RequiredArgsConstructor;
import org.ddd.domain.repo.UnitOfWork;
import org.ddd.domain.event.DomainEventSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <template/>
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
