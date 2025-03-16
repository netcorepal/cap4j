package org.netcorepal.cap4j.ddd.impl;

import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.RequestSupervisor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.domain.repo.*;
import org.netcorepal.cap4j.ddd.domain.service.DomainServiceSupervisor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.transaction.annotation.Propagation;

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
    public <ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> ENTITY create(ENTITY_PAYLOAD entityPayload) {
        return AggregateFactorySupervisor.getInstance().create(entityPayload);
    }

    @Override
    public <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass) {
        return RepositorySupervisor.getInstance().repo(entityClass);
    }

    @Override
    public <ENTITY> List<ENTITY> findWithoutPersist(Predicate<ENTITY> predicate, List<OrderInfo> orders) {
        return RepositorySupervisor.getInstance().findWithoutPersist(predicate, orders);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, List<OrderInfo> orders) {
        return RepositorySupervisor.getInstance().find(predicate, orders);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOneWithoutPersist(Predicate<ENTITY> predicate) {
        return RepositorySupervisor.getInstance().findOneWithoutPersist(predicate);
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate) {
        return RepositorySupervisor.getInstance().findOne(predicate);
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPageWithoutPersist(Predicate<ENTITY> predicate, PageParam pageParam) {
        return RepositorySupervisor.getInstance().findPageWithoutPersist(predicate, pageParam);
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam) {
        return RepositorySupervisor.getInstance().findPage(predicate, pageParam);
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
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit) {
        return RepositorySupervisor.getInstance().remove(predicate, limit);
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
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> findWithoutPersist(AggregatePredicate<AGGREGATE> predicate, List<OrderInfo> orders) {
        return AggregateSupervisor.getInstance().findWithoutPersist(predicate, orders);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> List<AGGREGATE> find(AggregatePredicate<AGGREGATE> predicate, List<OrderInfo> orders) {
        return AggregateSupervisor.getInstance().find(predicate, orders);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOneWithoutPersist(AggregatePredicate<AGGREGATE> predicate) {
        return AggregateSupervisor.getInstance().findOneWithoutPersist(predicate);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> Optional<AGGREGATE> findOne(AggregatePredicate<AGGREGATE> predicate) {
        return AggregateSupervisor.getInstance().findOne(predicate);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPageWithoutPersist(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam) {
        return AggregateSupervisor.getInstance().findPageWithoutPersist(predicate, pageParam);
    }

    @Override
    public <AGGREGATE extends Aggregate<?>> PageData<AGGREGATE> findPage(AggregatePredicate<AGGREGATE> predicate, PageParam pageParam) {
        return AggregateSupervisor.getInstance().findPage(predicate, pageParam);
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
