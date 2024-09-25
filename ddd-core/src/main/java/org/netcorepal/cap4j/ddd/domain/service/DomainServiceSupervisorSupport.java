package org.netcorepal.cap4j.ddd.domain.service;

/**
 * 领域服务管理
 *
 * @author binking338
 * @date 2024/9/4
 */
public class DomainServiceSupervisorSupport {
    static DomainServiceSupervisor instance;

    public static void configure(DomainServiceSupervisor domainServiceSupervisor){
        instance = domainServiceSupervisor;
    }
}
