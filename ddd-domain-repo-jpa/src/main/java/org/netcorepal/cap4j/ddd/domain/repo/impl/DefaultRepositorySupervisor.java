package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.domain.repo.Predicate;
import org.netcorepal.cap4j.ddd.domain.repo.Repository;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;

import java.util.*;
import java.util.function.Function;

/**
 * 默认仓储管理器
 *
 * @author binking338
 * @date 2024/9/3
 */
@RequiredArgsConstructor
public class DefaultRepositorySupervisor implements RepositorySupervisor {
    private final List<Repository<?>> repositories;
    private final UnitOfWork unitOfWork;

    protected Map<Class<?>, Map<Class<?>, Repository<?>>> repositoryMap = null;

    protected static Map<Class<?>, Function<Predicate<?>, Class<?>>> predicateClass2EntityClassReflector = new HashMap<>();
    protected static Map<Class<?>, Function<Repository<?>, Class<?>>> repositoryClass2EntityClassReflector = new HashMap<>();

    public static void registerPredicateEntityClassReflector(Class<?> predicateClass, Function<Predicate<?>, Class<?>> entityClassReflector) {
        predicateClass2EntityClassReflector.putIfAbsent(predicateClass, entityClassReflector);
    }

    public static void registerRepositoryEntityClassReflector(Class<?> repositoryClass, Function<Repository<?>, Class<?>> entityClassReflector) {
        repositoryClass2EntityClassReflector.putIfAbsent(repositoryClass, entityClassReflector);
    }

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
                    repository, 0,
                    Repository.class
            );
            if (entityClass == null || Object.class.equals(entityClass)) {
                for (Map.Entry<Class<?>, Function<Repository<?>, Class<?>>> entry : repositoryClass2EntityClassReflector.entrySet()) {
                    if (entry.getKey().isAssignableFrom(repository.getClass())) {
                        entityClass = entry.getValue().apply(repository);
                        if (!Object.class.equals(entityClass))
                            break;
                    }
                }
            }
            repositoryMap.computeIfAbsent(entityClass, cls -> new HashMap<>())
                    .put(repository.supportPredicateClass(), repository);
        });
    }

    protected <ENTITY> Repository<ENTITY> repo(Class<ENTITY> entityClass, Predicate<?> predicate) {
        init();
        Map<Class<?>, Repository<?>> repos = repositoryMap.getOrDefault(entityClass, null);
        if (repos == null || repos.isEmpty()) {
            throw new DomainException("仓储不存在：" + entityClass.getTypeName());
        }
        if (!repos.containsKey(predicate.getClass())) {
            new DomainException("仓储不兼容断言条件：" + predicate.getClass().getName());
        }
        return (Repository<ENTITY>) repos.get(predicate.getClass());
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        return repo(entityClass, predicate).find(predicate, orders, persist);
    }

    @Override
    public <ENTITY> List<ENTITY> find(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        List<ENTITY> list = repo(entityClass, predicate).find(predicate, pageParam, persist);
        if (persist && list != null) {
            list.forEach(unitOfWork::persist);
        }
        return list;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findOne(Predicate<ENTITY> predicate, boolean persist) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        Optional<ENTITY> entity = repo(entityClass, predicate).findOne(predicate, persist);
        if (persist) {
            entity.ifPresent(unitOfWork::persist);
        }
        return entity;
    }

    @Override
    public <ENTITY> Optional<ENTITY> findFirst(Predicate<ENTITY> predicate, Collection<OrderInfo> orders, boolean persist) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        Optional<ENTITY> entity = repo(entityClass, predicate).findFirst(predicate, orders, persist);
        if (persist) {
            entity.ifPresent(unitOfWork::persist);
        }
        return entity;
    }

    @Override
    public <ENTITY> PageData<ENTITY> findPage(Predicate<ENTITY> predicate, PageParam pageParam, boolean persist) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        PageData<ENTITY> pageData = repo(entityClass, predicate).findPage(predicate, pageParam, persist);
        if (persist && pageData != null && pageData.getList() != null) {
            pageData.getList().forEach(unitOfWork::persist);
        }
        return pageData;
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);

        List<ENTITY> entities = repo(entityClass, predicate).find(predicate);
        if (entities != null) {
            entities.forEach(unitOfWork::remove);
        }
        return entities;
    }

    @Override
    public <ENTITY> List<ENTITY> remove(Predicate<ENTITY> predicate, int limit) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        PageParam page = PageParam.limit(limit);
        PageData<ENTITY> entities = repo(entityClass, predicate).findPage(predicate, page);
        if (entities.getList() != null) {
            entities.getList().forEach(unitOfWork::remove);
        }
        return entities.getList();
    }

    @Override
    public <ENTITY> long count(Predicate<ENTITY> predicate) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        return repo(entityClass, predicate).count(predicate);
    }

    @Override
    public <ENTITY> boolean exists(Predicate<ENTITY> predicate) {
        if (!predicateClass2EntityClassReflector.containsKey(predicate.getClass())) {
            throw new DomainException("实体断言类型不支持：" + predicate.getClass().getName());
        }
        Class<ENTITY> entityClass = (Class<ENTITY>) predicateClass2EntityClassReflector.get(predicate.getClass()).apply(predicate);
        return repo(entityClass, predicate).exists(predicate);
    }
}
