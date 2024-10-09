package org.netcorepal.cap4j.ddd.domain.service;

/**
 * 领域服务管理器
 *
 * @author binking338
 * @date 2024/9/4
 */
public interface DomainServiceSupervisor {

    public static DomainServiceSupervisor getInstance(){
        return DomainServiceSupervisorSupport.instance;
    }

    /**
     * 获取领域服务
     * @param domainServiceClass
     * @return
     * @param <DOMAIN_SERVICE>
     */
    <DOMAIN_SERVICE> DOMAIN_SERVICE getService(Class<DOMAIN_SERVICE> domainServiceClass);
}
