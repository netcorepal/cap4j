package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultRepositorySupervisor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基于Jpa的仓储抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
public class AbstractJpaRepository<ENTITY, ID> implements Repository<ENTITY> {
    private final JpaSpecificationExecutor<ENTITY> jpaSpecificationExecutor;
    private final JpaRepository<ENTITY, ID> jpaRepository;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    @PostConstruct
    public void init() {
        DefaultRepositorySupervisor.registerPredicateEntityClassReflector(JpaPredicate.class, predicate -> {
            if (predicate == null) {
                return null;
            }
            return JpaPredicateSupport.reflectEntityClass(predicate);
        });
        DefaultRepositorySupervisor.registerRepositoryEntityClassReflector(AbstractJpaRepository.class, repository -> {
            return ClassUtils.resolveGenericTypeClass(
                    repository, 0,
                    AbstractJpaRepository.class
            );
        });
    }

    @Override
    public Class<?> supportPredicateClass() {
        return JpaPredicate.class;
    }

    @Override
    public Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist) {
        Optional<ENTITY> entity = Optional.empty();
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            entity = jpaRepository.findById(JpaPredicateSupport.resumeId(predicate));
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            entity = jpaSpecificationExecutor.findOne(JpaPredicateSupport.resumeSpecification(predicate));
        }
        if (!persist) {
            entity.ifPresent(e -> getEntityManager().detach(e));
        }
        return entity;
    }

    @Override
    public Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        Optional<ENTITY> entity = Optional.empty();
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            entity = jpaRepository.findById(JpaPredicateSupport.resumeId(predicate));
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            PageParam page = PageParam.limit(1);
            if (orders != null && !orders.isEmpty()) {
                orders.forEach(o -> page.orderBy(o.getField(), o.getDesc()));
            }
            entity = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), JpaPageUtils.toSpringData(page)).stream().findFirst();
        }
        if (!persist) {
            entity.ifPresent(e -> getEntityManager().detach(e));
        }
        return entity;
    }

    @Override
    public PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        PageData<ENTITY> pageData = PageData.empty(pageParam.getPageSize(), null);
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                List<ENTITY> entities = jpaRepository.findAllById(JpaPredicateSupport.resumeIds(predicate))
                        .stream()
                        .skip((pageParam.getPageNum() - 1L) * pageParam.getPageSize())
                        .limit(pageParam.getPageSize())
                        .collect(Collectors.toList());
                pageData = PageData.create(pageParam, (long) entities.size(), entities);
            }
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            Page<ENTITY> page = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), JpaPageUtils.toSpringData(pageParam));
            pageData = JpaPageUtils.fromSpringData(page);
        }
        if (!persist && pageData.getList() != null && !pageData.getList().isEmpty()) {
            pageData.getList().forEach(e -> getEntityManager().detach(e));
        }
        return pageData;
    }

    @Override
    public List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        List<ENTITY> entities = Collections.emptyList();
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                entities = jpaRepository.findAllById(JpaPredicateSupport.resumeIds(predicate));
            }
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            entities = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), JpaSortUtils.toSpringData(orders));
        }
        if (!persist && !entities.isEmpty()) {
            entities.forEach(e -> getEntityManager().detach(e));
        }
        return entities;
    }

    @Override
    public List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        List<ENTITY> entities = Collections.emptyList();
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                entities = jpaRepository.findAllById(JpaPredicateSupport.resumeIds(predicate));
            }
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            Page<ENTITY> page = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), JpaPageUtils.toSpringData(pageParam));
            entities = page.getContent();
        }
        if (!persist && !entities.isEmpty()) {
            entities.forEach(e -> getEntityManager().detach(e));
        }
        return entities;
    }

    public long count(Predicate<ENTITY> predicate) {
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            return jpaRepository.findById(JpaPredicateSupport.resumeId(predicate)).isPresent() ?
                    1L :
                    0L;
        }
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return 0L;
            }
            return jpaRepository.findAllById(JpaPredicateSupport.resumeIds(predicate))
                    .size();
        }
        return jpaSpecificationExecutor.count(JpaPredicateSupport.resumeSpecification(predicate));
    }

    public boolean exists(Predicate<ENTITY> predicate) {
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            return jpaRepository.findById(JpaPredicateSupport.resumeId(predicate)).isPresent();
        }
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return false;
            }
            return jpaRepository.findAllById(JpaPredicateSupport.resumeIds(predicate))
                    .size() > 0;
        }
        return jpaSpecificationExecutor.exists(JpaPredicateSupport.resumeSpecification(predicate));
    }
}
