package org.netcorepal.cap4j.ddd.domain.service;

/**
 * 领域服务管理
 *
 * @author binking338
 * @date 2024/9/4
 */
public class DomainServiceSupervisorConfiguration {
    static DomainServiceSupervisor instance;

    public static void configure(DomainServiceSupervisor domainServiceSupervisor){
        domainServiceSupervisor = instance;
    }
}
