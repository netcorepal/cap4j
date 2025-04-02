package org.netcorepal.cap4j.ddd.impl;

import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.domain.repo.AggregatePredicate;
import org.netcorepal.cap4j.ddd.domain.repo.AggregateSupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.Predicate;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
import org.netcorepal.cap4j.ddd.domain.service.DomainServiceSupervisor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.Collection;
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
    public <ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> ENTITY create(ENTITY_PAYLOAD entityPayload) {
        return AggregateFactorySupervisor.getInstance().create(entityPayload);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        return RepositorySupervisor.getInstance().find(predicate, orders, persist);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        return RepositorySupervisor.getInstance().find(predicate, pageParam, persist);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist) {
        return RepositorySupervisor.getInstance().findOne(predicate, persist);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        return RepositorySupervisor.getInstance().findFirst(predicate, orders, persist);
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        return RepositorySupervisor.getInstance().findPage(predicate, pageParam, persist);
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate) {
        return RepositorySupervisor.getInstance().remove(predicate);
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit) {
        return RepositorySupervisor.getInstance().remove(predicate, limit);
    }

    @Override
    public <ENTITY> long count(Predicate<ENTITY> predicate) {
        return RepositorySupervisor.getInstance().count(predicate);
    }

    @Override
    public <ENTITY> boolean exists(Predicate<ENTITY> predicate) {
        return RepositorySupervisor.getInstance().exists(predicate);
    }

    @Override
    public <DOMAIN_SERVICE> DOMAIN_SERVICE getService(Class<DOMAIN_SERVICE> domainServiceClass) {
        return DomainServiceSupervisor.getInstance().getService(domainServiceClass);
    }

    @Override
    public void persist(Object entity) {
        UnitOfWork.getInstance().persist(entity);
    }

    @Override
    public boolean persistIfNotExist(Object entity) {
        return UnitOfWork.getInstance().persistIfNotExist(entity);
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
    public <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request) {
        return RequestSupervisor.getInstance().send(request);
    }

    @Override
    public <INTEGRATION_EVENT> void attach(INTEGRATION_EVENT integrationEventPayload, LocalDateTime schedule) {
        IntegrationEventSupervisor.getInstance().attach(integrationEventPayload, schedule);
    }

    @Override
    public <INTEGRATION_EVENT> void detach(INTEGRATION_EVENT integrationEventPayload) {
        IntegrationEventSupervisor.getInstance().detach(integrationEventPayload);
    }

    @Override
    public <AGGREGATE extends Aggregate<ENTITY>, ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> AGGREGATE create(Class<AGGREGATE> clazz, ENTITY_PAYLOAD payload) {
        return AggregateSupervisor.getInstance().create(clazz, payload);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders, boolean persist) {
        return AggregateSupervisor.getInstance().find(predicate, orders, persist);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam, boolean persist) {
        return AggregateSupervisor.getInstance().find(predicate, pageParam, persist);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> predicate, boolean persist) {
        return AggregateSupervisor.getInstance().findOne(predicate, persist);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findFirst(AggregatePredicate<AGGREGATE> predicate, Collection<OrderInfo> orders, boolean persist) {
        return AggregateSupervisor.getInstance().findFirst(predicate, orders, persist);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam, boolean persist) {
        return AggregateSupervisor.getInstance().findPage(predicate, pageParam, persist);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> predicate) {
        return AggregateSupervisor.getInstance().remove(predicate);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> remove(AggregatePredicate<AGGREGATE> predicate, int limit) {
        return AggregateSupervisor.getInstance().remove(predicate, limit);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> long count(AggregatePredicate<AGGREGATE> predicate) {
        return AggregateSupervisor.getInstance().count(predicate);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> boolean exists(AggregatePredicate<AGGREGATE> predicate) {
        return AggregateSupervisor.getInstance().exists(predicate);
    }
}
