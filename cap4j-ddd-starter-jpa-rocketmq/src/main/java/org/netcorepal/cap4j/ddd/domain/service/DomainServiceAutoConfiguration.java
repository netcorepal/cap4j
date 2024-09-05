package org.netcorepal.cap4j.ddd.domain.service;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.service.impl.DefaultDomainServiceSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务自动配置
 *
 * @author binking338
 * @date 2024/9/4
 */
@Configuration
@RequiredArgsConstructor
public class DomainServiceAutoConfiguration {

    /**
     * 默认领域服务管理器
     *
     * @param applicationContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DomainServiceSupervisor.class)
    public DomainServiceSupervisor defaultDomainServiceSupervisor(ApplicationContext applicationContext) {
        DefaultDomainServiceSupervisor domainServiceSupervisor = new DefaultDomainServiceSupervisor(
                applicationContext
        );
        return domainServiceSupervisor;
    }
}
