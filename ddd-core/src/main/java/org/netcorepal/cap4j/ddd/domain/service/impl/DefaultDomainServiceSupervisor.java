package org.netcorepal.cap4j.ddd.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.service.DomainServiceSupervisor;
import org.netcorepal.cap4j.ddd.domain.service.annotation.DomainService;
import org.springframework.context.ApplicationContext;

/**
 * 默认领域服务管理器
 *
 * @author binking338
 * @date 2024/9/4
 */
@RequiredArgsConstructor
public class DefaultDomainServiceSupervisor implements DomainServiceSupervisor {
    private final ApplicationContext applicationContext;

    @Override
    public <DOMAIN_SERVICE> DOMAIN_SERVICE getService(Class<DOMAIN_SERVICE> domainServiceClass) {
        DOMAIN_SERVICE domainService = applicationContext.getBean(domainServiceClass);
        if(null == domainService.getClass().getAnnotation(DomainService.class)) {
            return null;
        }
        return domainService;
    }
}
