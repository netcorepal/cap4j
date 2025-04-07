package org.netcorepal.cap4j.ddd.domain.repo.querydsl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.repo.JpaPageUtils;
import org.netcorepal.cap4j.ddd.domain.repo.Predicate;
import org.netcorepal.cap4j.ddd.domain.repo.Repository;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultRepositorySupervisor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 基于querydsl的仓储抽象类
 *
 * @author binking338
 * @date 2025/3/29
 */
@RequiredArgsConstructor
public class AbstractQuerydslRepository<ENTITY> implements Repository<ENTITY> {
    private final QuerydslPredicateExecutor<ENTITY> querydslPredicateExecutor;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    @PostConstruct
    public void init() {
        DefaultRepositorySupervisor.registerPredicateEntityClassReflector(QuerydslPredicate.class, predicate -> {
            if (predicate == null) {
                return null;
            }
            return QuerydslPredicateSupport.reflectEntityClass(predicate);
        });
        DefaultRepositorySupervisor.registerRepositoryEntityClassReflector(AbstractQuerydslRepository.class, repository -> {
            return ClassUtils.resolveGenericTypeClass(
                    repository, 0,
                    AbstractQuerydslRepository.class
            );
        });
    }

    @Override
    public Class<?> supportPredicateClass() {
        return QuerydslPredicate.class;
    }

    @Override
    public List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        Iterable<ENTITY> entities = querydslPredicateExecutor.findAll(QuerydslPredicateSupport.resumePredicate(predicate), QuerydslPredicateSupport.resumeSort(predicate, orders));
        List<ENTITY> list = new ArrayList<>();
        entities.forEach(e -> {
            list.add(e);
            if (!persist) {
                getEntityManager().detach(e);
            }
        });
        return list;
    }

    @Override
    public List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        Iterable<ENTITY> entities = querydslPredicateExecutor.findAll(QuerydslPredicateSupport.resumePredicate(predicate), QuerydslPredicateSupport.resumePageable(predicate, pageParam));
        List<ENTITY> list = new ArrayList<>();
        entities.forEach(e -> {
            list.add(e);
            if (!persist) {
                getEntityManager().detach(e);
            }
        });
        return list;
    }

    @Override
    public Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist) {
        Optional<ENTITY> entity = querydslPredicateExecutor.findOne(QuerydslPredicateSupport.resumePredicate(predicate));
        if (!persist) {
            entity.ifPresent(getEntityManager()::detach);
        }
        return entity;
    }

    @Override
    public Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        PageParam pageParam = PageParam.limit(1);
        if (orders != null) {
            orders.forEach(order -> pageParam.orderBy(order.getField(), order.getDesc()));
        }
        Page<ENTITY> entities = querydslPredicateExecutor.findAll(QuerydslPredicateSupport.resumePredicate(predicate), QuerydslPredicateSupport.resumePageable(predicate, pageParam));
        Optional<ENTITY> entity = entities.stream().findFirst();
        if (!persist) {
            entity.ifPresent(getEntityManager()::detach);
        }
        return entity;
    }

    @Override
    public PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        Page<ENTITY> entities = querydslPredicateExecutor.findAll(QuerydslPredicateSupport.resumePredicate(predicate), QuerydslPredicateSupport.resumePageable(predicate, pageParam));
        if (!persist) {
            entities.forEach(getEntityManager()::detach);
        }
        return JpaPageUtils.fromSpringData(entities);
    }

    @Override
    public long count(Predicate<ENTITY> predicate) {
        return querydslPredicateExecutor.count(QuerydslPredicateSupport.resumePredicate(predicate));
    }

    @Override
    public boolean exists(Predicate<ENTITY> predicate) {
        return querydslPredicateExecutor.exists(QuerydslPredicateSupport.resumePredicate(predicate));
    }
}
