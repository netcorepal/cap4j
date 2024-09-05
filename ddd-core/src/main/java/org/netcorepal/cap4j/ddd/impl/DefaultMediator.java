package org.netcorepal.cap4j.ddd.impl;

import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.Repository;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
import org.netcorepal.cap4j.ddd.domain.service.DomainServiceSupervisor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.transaction.annotation.Propagation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 默认中介者
 *
 * @author binking338
 * @date 2024/8/24
 */
public class DefaultMediator implements Mediator {

    @Override
    public <ENTITY> ENTITY create(Class<ENTITY> entityClass) {
        return AggregateFactorySupervisor.getInstance().create(entityClass);
    }

    @Override
    public <ENTITY> ENTITY create(Class<ENTITY> entityClass, AggregateFactory.InitHandler<ENTITY> initHandler) {
        return AggregateFactorySupervisor.getInstance().create(entityClass, initHandler);
    }

    @Override
    public <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass) {
        return RepositorySupervisor.getInstance().repo(entityClass);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Class<ENTITY> entityClass, Object condition, List<OrderInfo> orders) {
        return RepositorySupervisor.getInstance().find(entityClass, condition, orders);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Class<ENTITY> entityClass, Object condition) {
        return RepositorySupervisor.getInstance().findOne(entityClass, condition);
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Class<ENTITY> entityClass, Object condition, PageParam pageParam) {
        return RepositorySupervisor.getInstance().findPage(entityClass, condition, pageParam);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findById(Class<ENTITY> entityClass, Object id) {
        return RepositorySupervisor.getInstance().findById(entityClass, id);
    }

    @Override
    public <ENTITY> List<ENTITY> findByIds(Class<ENTITY> entityClass, Iterable<Object> ids) {
        return RepositorySupervisor.getInstance().findByIds(entityClass, ids);
    }

    @Override
    public <ENTITY> long count(Class<ENTITY> entityClass, Object condition) {
        return RepositorySupervisor.getInstance().count(entityClass, condition);
    }

    @Override
    public <ENTITY> boolean exists(Class<ENTITY> entityClass, Object condition) {
        return RepositorySupervisor.getInstance().exists(entityClass, condition);
    }

    @Override
    public <ENTITY> boolean existsById(Class<ENTITY> entityClass, Object id) {
        return RepositorySupervisor.getInstance().existsById(entityClass, id);
    }

    @Override
    public <DOMAIN_SERVICE> DOMAIN_SERVICE getService(Class<DOMAIN_SERVICE> domainServiceClass) {
        return DomainServiceSupervisor.getInstance().getService(domainServiceClass);
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Class<ENTITY> entityClass, Object condition, int limit) {
        return RepositorySupervisor.getInstance().remove(entityClass, condition, limit);
    }

    @Override
    public <ENTITY> Optional<ENTITY> removeById(Class<ENTITY> entityClass, Object id) {
        return RepositorySupervisor.getInstance().removeById(entityClass, id);
    }

    @Override
    public <ENTITY> List<ENTITY> removeByIds(Class<ENTITY> entityClass, Iterable<Object> ids) {
        return RepositorySupervisor.getInstance().removeByIds(entityClass, ids);
    }

    @Override
    public void persist(Object entity) {
        UnitOfWork.getInstance().persist(entity);
    }

    @Override
    public void remove(Object entity) {
        UnitOfWork.getInstance().remove(entity);
    }

    @Override
    public void save() {
        UnitOfWork.getInstance().save();
    }

    @Override
    public void save(Propagation propagation) {
        UnitOfWork.getInstance().save(propagation);
    }

    @Override
    public <REQUEST> Object send(REQUEST request) {
        return RequestSupervisor.getInstance().send(request);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<RESPONSE> resultClass) {
        return RequestSupervisor.getInstance().send(request, resultClass);
    }

    @Override
    public <REQUEST, RESPONSE> RESPONSE send(REQUEST request, Class<REQUEST> paramClass, Class<RESPONSE> resultClass) {
        return RequestSupervisor.getInstance().send(request, paramClass, resultClass);
    }

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload) {
        IntegrationEventSupervisor.getInstance().notify(integrationEventPayload);
    }

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload, Duration delay) {
        IntegrationEventSupervisor.getInstance().notify(integrationEventPayload, delay);
    }

    @Override
    public <INTEGRATION_EVENT> void notify(INTEGRATION_EVENT integrationEventPayload, LocalDateTime schedule) {
        IntegrationEventSupervisor.getInstance().notify(integrationEventPayload, schedule);
    }
}
