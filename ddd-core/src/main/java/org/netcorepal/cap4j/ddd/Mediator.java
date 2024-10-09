package org.netcorepal.cap4j.ddd;

import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
import org.netcorepal.cap4j.ddd.domain.service.DomainServiceSupervisor;
import org.springframework.context.ApplicationContext;

/**
 * 中介者
 *
 * @author binking338
 * @date 2024/8/24
 */
public interface Mediator extends AggregateFactorySupervisor, RepositorySupervisor, DomainServiceSupervisor, UnitOfWork, IntegrationEventSupervisor, RequestSupervisor {
    static Mediator getInstance() {
        return MediatorSupport.instance;
    }

    /**
     * 获取IOC容器
     *
     * @return
     */
    static ApplicationContext ioc() {
        return MediatorSupport.ioc;
    }

    /**
     * 获取聚合工厂管理器
     *
     * @return
     */
    static AggregateFactorySupervisor factories() {
        return AggregateFactorySupervisor.getInstance();
    }

    /**
     * 获取聚合仓储管理器
     *
     * @return
     */
    static RepositorySupervisor repositories() {
        return RepositorySupervisor.getInstance();
    }

    /**
     * 获取领域服务管理器
     *
     * @return
     */
    static DomainServiceSupervisor services() {
        return DomainServiceSupervisor.getInstance();
    }

    /**
     * 获取单元工作单元
     *
     * @return
     */
    static UnitOfWork uow() {
        return UnitOfWork.getInstance();
    }

    /**
     * 获取集成事件管理器
     *
     * @return
     */
    static IntegrationEventSupervisor events() {
        return IntegrationEventSupervisor.getInstance();
    }


    /**
     * 获取请求管理器
     * 兼容 cmd() qry()，当前三者实现一致。
     *
     * @return
     */
    static RequestSupervisor requests() {
        return RequestSupervisor.getInstance();
    }

    /**
     * 获取命令管理器
     *
     * @return
     */
    static RequestSupervisor commands() {
        return RequestSupervisor.getInstance();
    }

    /**
     * 获取查询管理器
     *
     * @return
     */
    static RequestSupervisor queries() {
        return RequestSupervisor.getInstance();
    }

    default ApplicationContext getApplicationContext() {
        return MediatorSupport.ioc;
    }

    default AggregateFactorySupervisor getAggregateFactorySupervisor() {
        return AggregateFactorySupervisor.getInstance();
    }

    default RepositorySupervisor getRepositorySupervisor() {
        return RepositorySupervisor.getInstance();
    }

    default UnitOfWork getUnitOfWork() {
        return UnitOfWork.getInstance();
    }

    default IntegrationEventSupervisor getIntegrationEventSupervisor() {
        return IntegrationEventSupervisor.getInstance();
    }

    default RequestSupervisor getRequestSupervisor() {
        return RequestSupervisor.getInstance();
    }
}
