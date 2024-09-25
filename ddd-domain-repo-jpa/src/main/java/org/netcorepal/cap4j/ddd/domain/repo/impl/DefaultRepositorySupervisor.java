package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.domain.repo.*;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 默认仓储管理器
 *
 * @author binking338
 * @date 2024/9/3
 */
@RequiredArgsConstructor
public class DefaultRepositorySupervisor implements RepositorySupervisor {
    private final List<AbstractJpaRepository<?, ?>> repositories;
    private final UnitOfWork unitOfWork;

    protected Map<Class<?>, Repository<?>> repositoryMap = null;

    public void init() {
        if (repositoryMap != null) {
            return;
        }
        synchronized (this) {
            if (repositoryMap != null) {
                return;
            }
        }
        repositoryMap = new HashMap<>();
        repositories.forEach(repository -> {
            Class<?> entityClass = ClassUtils.resolveGenericTypeClass(
                    repository.getClass(), 0,
                    AbstractJpaRepository.class, Repository.class
            );
            repositoryMap.put(entityClass, repository);
        });
    }

    @Override
    public <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass) {
        init();
        if (!repositoryMap.containsKey(entityClass)) {
            throw new DomainException("仓储不存在：" + entityClass.getTypeName());
        }
        return (Repository<ENTITY>) repositoryMap.get(entityClass);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, List<OrderInfo> orders) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        List<ENTITY> entities = repo(entityClass).find(predicate, orders);
        return entities;
    }

    @Override
    public <ENTITY> List<ENTITY> find4Update(Predicate<ENTITY> predicate, List<OrderInfo> orders) {
        List<ENTITY> entities = find(predicate, orders);
        if (entities != null) {
            entities.forEach(unitOfWork::persist);
        }
        return entities;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        Optional<ENTITY> entity = repo(entityClass).findOne(predicate);
        return entity;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne4Update(Predicate<ENTITY> predicate) {
        Optional<ENTITY> entity = findOne(predicate);
        entity.ifPresent(unitOfWork::persist);
        return entity;
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        PageData<ENTITY> page = repo(entityClass).findPage(predicate, pageParam);
        return page;
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage4Update(Predicate<ENTITY> predicate, PageParam pageParam) {
        PageData<ENTITY> page = findPage(predicate, pageParam);
        if (page.getList() != null) {
            page.getList().forEach(unitOfWork::persist);
        }
        return page;
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        PageParam page = new PageParam();
        page.setPageNum(1);
        page.setPageSize(limit);
        PageData<ENTITY> entities = repo(entityClass).findPage(predicate, page);
        if (entities.getList() != null) {
            entities.getList().forEach(unitOfWork::remove);
        }
        return entities.getList();
    }

    @Override
    public <ENTITY> long count(Predicate<ENTITY> predicate) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        return repo(entityClass).count(predicate);
    }

    @Override
    public <ENTITY> boolean exists(Predicate<ENTITY> predicate) {
        Class<ENTITY> entityClass = JpaPredicateSupport.reflectEntityClass(predicate);
        return repo(entityClass).exists(predicate);
    }
}
