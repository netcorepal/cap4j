package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.domain.repo.AbstractJpaRepository;
import org.netcorepal.cap4j.ddd.domain.repo.Repository;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
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
    public <ENTITY> List<ENTITY> find(Class<ENTITY> entityClass, Object condition, List<OrderInfo> orders) {
        List<ENTITY> entities = repo(entityClass).find(condition, orders);
        if (entities != null) {
            entities.forEach(unitOfWork::persist);
        }
        return entities;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Class<ENTITY> entityClass, Object condition) {
        Optional<ENTITY> entity = repo(entityClass).findOne(condition);
        entity.ifPresent(unitOfWork::persist);
        return entity;
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Class<ENTITY> entityClass, Object condition, PageParam pageParam) {
        PageData<ENTITY> page = repo(entityClass).findPage(condition, pageParam);
        if (page.getList() != null) {
            page.getList().forEach(unitOfWork::persist);
        }
        return page;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findById(Class<ENTITY> entityClass, Object id) {
        Optional<ENTITY> entity = repo(entityClass).findById(id);
        entity.ifPresent(unitOfWork::persist);
        return entity;
    }

    @Override
    public <ENTITY> List<ENTITY> findByIds(Class<ENTITY> entityClass, Iterable<Object> ids) {
        List<ENTITY> entities = repo(entityClass).findByIds(ids);
        if (entities != null) {
            entities.forEach(unitOfWork::persist);
        }
        return entities;
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Class<ENTITY> entityClass, Object condition, int limit) {
        PageParam page = new PageParam();
        page.setPageNum(1);
        page.setPageSize(limit);
        PageData<ENTITY> entities = repo(entityClass).findPage(condition, page);
        if (entities.getList() != null) {
            entities.getList().forEach(unitOfWork::remove);
        }
        return entities.getList();
    }

    @Override
    public <ENTITY> Optional<ENTITY> removeById(Class<ENTITY> entityClass, Object id) {
        Optional<ENTITY> entity = repo(entityClass).findById(id);
        entity.ifPresent(unitOfWork::remove);
        return entity;
    }

    @Override
    public <ENTITY> List<ENTITY> removeByIds(Class<ENTITY> entityClass, Iterable<Object> ids) {
        List<ENTITY> entities = repo(entityClass).findByIds(ids);
        if (entities != null) {
            entities.forEach(unitOfWork::remove);
        }
        return entities;
    }

    @Override
    public <ENTITY> long count(Class<ENTITY> entityClass, Object condition) {
        return repo(entityClass).count(condition);
    }

    @Override
    public <ENTITY> boolean exists(Class<ENTITY> entityClass, Object condition) {
        return repo(entityClass).exists(condition);
    }

    @Override
    public <ENTITY> boolean existsById(Class<ENTITY> entityClass, Object id) {
        return repo(entityClass).existsById(id);
    }
}
