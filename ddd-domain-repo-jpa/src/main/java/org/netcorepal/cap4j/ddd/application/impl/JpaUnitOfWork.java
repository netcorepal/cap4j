package org.netcorepal.cap4j.ddd.application.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.netcorepal.cap4j.ddd.domain.repo.PersistListenerManager;
import org.netcorepal.cap4j.ddd.domain.repo.PersistType;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于Jpa的UnitOfWork实现
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
@Slf4j
public class JpaUnitOfWork implements UnitOfWork {
    private final List<UnitOfWorkInterceptor> uowInterceptors;
    private final PersistListenerManager persistListenerManager;
    private final boolean supportEntityInlinePersistListener;
    private final boolean supportValueObjectExistsCheckOnSave;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;
    protected static JpaUnitOfWork instance;

    public static void fixAopWrapper(JpaUnitOfWork unitOfWork) {
        instance = unitOfWork;
    }

    private static ThreadLocal<Set<Object>> persistEntitiesThreadLocal = ThreadLocal.withInitial(() -> new LinkedHashSet<>());
    private static ThreadLocal<Set<Object>> removeEntitiesThreadLocal = ThreadLocal.withInitial(() -> new LinkedHashSet<>());
    private static ThreadLocal<Set<Object>> processingEntitiesThreadLocal = ThreadLocal.withInitial(() -> new LinkedHashSet<>());
    private static ThreadLocal<Map<Object, Aggregate>> wrapperMapThreadLocal = ThreadLocal.withInitial(() -> new HashMap<>());

    private static ConcurrentHashMap<Class<?>, EntityInformation> entityInformationCache = new ConcurrentHashMap<>();

    private EntityInformation getEntityInformation(Class<?> entityClass) {
        return entityInformationCache.computeIfAbsent(
                entityClass,
                cls -> JpaEntityInformationSupport.getEntityInformation(cls, getEntityManager())
        );
    }

    private boolean isValueObjectAndExists(Object entity) {
        ValueObject<?> valueObject = entity instanceof ValueObject
                ? (ValueObject<?>) entity
                : null;
        if (null == valueObject) {
            return false;
        }
        return null != getEntityManager().find(entity.getClass(), ((ValueObject<?>) entity).hash());
    }

    private boolean isExists(Object entity) {
        EntityInformation entityInformation = getEntityInformation(entity.getClass());
        boolean isValueObject = entity instanceof ValueObject;
        if (!isValueObject && entityInformation.isNew(entity)) {
            return false;
        }
        Object id = isValueObject
                ? ((ValueObject<?>) entity).hash()
                : entityInformation.getId(entity);
        if (id == null || null == getEntityManager().find(entity.getClass(), id)) {
            return false;
        }
        return true;
    }

    protected List<Object> persistenceContextEntities() {
        try {
            if (!((SessionImplementor) getEntityManager().getDelegate()).isClosed()) {
                org.hibernate.engine.spi.PersistenceContext persistenceContext = ((SessionImplementor) getEntityManager().getDelegate()).getPersistenceContext();
                Stream<Object> entitiesInPersistenceContext = Arrays.stream(persistenceContext.reentrantSafeEntityEntries()).map(e -> e.getKey());
                return entitiesInPersistenceContext.collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.debug("跟踪实体获取失败", ex);
        }
        return Collections.emptyList();
    }

    protected void onEntitiesFlushed(Set<Object> createdEntities, Set<Object> updatedEntities, Set<Object> deletedEntities) {
        if (!supportEntityInlinePersistListener) {
            return;
        }
        for (Object entity : createdEntities) {
            persistListenerManager.onChange(entity, PersistType.CREATE);
        }
        for (Object entity : updatedEntities) {
            persistListenerManager.onChange(entity, PersistType.UPDATE);
        }
        for (Object entity : deletedEntities) {
            persistListenerManager.onChange(entity, PersistType.DELETE);
        }
    }

    private Object unwrapEntity(Object entity) {
        if (!(entity instanceof Aggregate)) {
            return entity;
        }
        Aggregate<?> aggregate = (Aggregate<?>) entity;
        Object unwrappedEntity = aggregate._unwrap();
        wrapperMapThreadLocal.get().put(unwrappedEntity, aggregate);
        return unwrappedEntity;
    }

    private void updateWrappedEntity(Object entity, Object updatedEntity) {
        if (!wrapperMapThreadLocal.get().containsKey(entity)) {
            return;
        }
        Aggregate aggregate = wrapperMapThreadLocal.get().remove(entity);
        aggregate._wrap(updatedEntity);
        wrapperMapThreadLocal.get().put(updatedEntity, aggregate);
    }

    @Override
    public void persist(Object entity) {
        entity = unwrapEntity(entity);
        if (isValueObjectAndExists(entity)) {
            return;
        }
        persistEntitiesThreadLocal.get().add(entity);
    }

    @Override
    public boolean persistIfNotExist(Object entity) {
        entity = unwrapEntity(entity);
        if (isExists(entity)) {
            return false;
        }
        persistEntitiesThreadLocal.get().add(entity);
        return true;
    }

    @Override
    public void remove(Object entity) {
        entity = unwrapEntity(entity);
        removeEntitiesThreadLocal.get().add(entity);
    }

    @Override
    public void save() {
        save(Propagation.REQUIRED);
    }

    private boolean pushProcessingEntities(Object entity, Set<Object> currentProcessedPersistenceContextEntities) {
        if (null == entity) {
            return false;
        }
        if (!processingEntitiesThreadLocal.get().contains(entity)) {
            processingEntitiesThreadLocal.get().add(entity);
            currentProcessedPersistenceContextEntities.add(entity);
            return true;
        }
        return false;
    }

    private boolean popProcessingEntities(Set<Object> currentProcessedPersistenceContextEntities) {
        if (currentProcessedPersistenceContextEntities != null && currentProcessedPersistenceContextEntities.size() > 0) {
            return processingEntitiesThreadLocal.get().removeAll(currentProcessedPersistenceContextEntities);
        }
        return true;
    }

    @Override
    public void save(Propagation propagation) {
        Set<Object> currentProcessedEntitySet = new LinkedHashSet<>();
        Set<Object> persistEntitySet = persistEntitiesThreadLocal.get();
        if (persistEntitySet.size() > 0) {
            persistEntitiesThreadLocal.remove();
            persistEntitySet.forEach(e -> pushProcessingEntities(e, currentProcessedEntitySet));
        }
        Set<Object> deleteEntitySet = removeEntitiesThreadLocal.get();
        if (deleteEntitySet.size() > 0) {
            removeEntitiesThreadLocal.remove();
            deleteEntitySet.forEach(e -> pushProcessingEntities(e, currentProcessedEntitySet));
        }

        for (UnitOfWorkInterceptor interceptor :
                uowInterceptors) {
            interceptor.beforeTransaction(persistEntitySet, deleteEntitySet);
        }
        Set<Object>[] saveAndDeleteEntities = new Set[]{
                persistEntitySet,
                deleteEntitySet,
                currentProcessedEntitySet
        };
        save(input -> {
            Set<Object> persistEntities = input[0];
            Set<Object> deleteEntities = input[1];
            Set<Object> processedEntities = input[2];
            Set<Object> createdEntities = new LinkedHashSet<>();
            Set<Object> updatedEntities = new LinkedHashSet<>();
            Set<Object> deletedEntities = new LinkedHashSet<>();
            for (UnitOfWorkInterceptor interceptor :
                    uowInterceptors) {
                interceptor.preInTransaction(persistEntities, deleteEntities);
            }
            boolean flush = false;
            List<Object> refreshEntityList = null;
            if (persistEntities != null && !persistEntities.isEmpty()) {
                flush = true;
                for (Object entity : persistEntities) {
                    if (supportValueObjectExistsCheckOnSave && entity instanceof ValueObject) {
                        if (!isExists(entity)) {
                            getEntityManager().persist(entity);
                            createdEntities.add(entity);
                        }
                        continue;
                    }
                    EntityInformation entityInformation = getEntityInformation(entity.getClass());
                    if (entityInformation.isNew(entity)) {
                        if (!getEntityManager().contains(entity)) {
                            getEntityManager().persist(entity);
                        }
                        if (refreshEntityList == null) {
                            refreshEntityList = new ArrayList<>();
                        }
                        refreshEntityList.add(entity);
                        createdEntities.add(entity);
                    } else {
                        if (!getEntityManager().contains(entity)) {
                            Object mergedEntity = getEntityManager().merge(entity);
                            updateWrappedEntity(entity, mergedEntity);
                        }
                        updatedEntities.add(entity);
                    }
                }
            }
            if (deleteEntities != null && !deleteEntities.isEmpty()) {
                flush = true;
                for (Object entity : deleteEntities) {
                    if (getEntityManager().contains(entity)) {
                        getEntityManager().remove(entity);
                    } else {
                        Object mergedEntity = getEntityManager().merge(entity);
                        updateWrappedEntity(entity, mergedEntity);
                        getEntityManager().remove(mergedEntity);
                    }
                    deletedEntities.add(entity);
                }
            }

            if (flush) {
                getEntityManager().flush();
                if (refreshEntityList != null && !refreshEntityList.isEmpty()) {
                    for (Object entity : refreshEntityList) {
                        getEntityManager().refresh(entity);
                    }
                }
                onEntitiesFlushed(createdEntities, updatedEntities, deletedEntities);
            }

            Set<Object> entities = new LinkedHashSet<>();
            if (persistEntities != null && !persistEntities.isEmpty()) {
                entities.addAll(persistEntities);
            }
            if (deleteEntities != null && !deleteEntities.isEmpty()) {
                entities.addAll(deleteEntities);
            }
            for (Object entity : persistenceContextEntities()) {
                pushProcessingEntities(entity, processedEntities);
            }
            entities.addAll(processedEntities);
            for (UnitOfWorkInterceptor interceptor :
                    uowInterceptors) {
                interceptor.postEntitiesPersisted(entities);
            }

            for (UnitOfWorkInterceptor interceptor :
                    uowInterceptors) {
                interceptor.postInTransaction(persistEntities, deleteEntities);
            }
            return null;
        }, saveAndDeleteEntities, propagation);
        for (UnitOfWorkInterceptor interceptor :
                uowInterceptors) {
            interceptor.afterTransaction(persistEntitySet, deleteEntitySet);
        }
        popProcessingEntities(currentProcessedEntitySet);
    }

    public static void reset() {
        persistEntitiesThreadLocal.remove();
        removeEntitiesThreadLocal.remove();
        processingEntitiesThreadLocal.remove();
        wrapperMapThreadLocal.remove();
    }

    /**
     * 事务执行句柄
     */
    public interface TransactionHandler<I, O> {
        O exec(I input);
    }

    /**
     * 事务保存，自动发送领域事件
     *
     * @param transactionHandler
     * @param propagation
     * @param <I>
     * @param <O>
     * @return
     */
    public <I, O> O save(TransactionHandler<I, O> transactionHandler, I i, Propagation propagation) {
        O result = null;
        switch (propagation) {
            case SUPPORTS:
                result = instance.supports(transactionHandler, i);
                break;
            case NOT_SUPPORTED:
                result = instance.notSupported(transactionHandler, i);
                break;
            case REQUIRES_NEW:
                result = instance.requiresNew(transactionHandler, i);
                break;
            case MANDATORY:
                result = instance.mandatory(transactionHandler, i);
                break;
            case NEVER:
                result = instance.never(transactionHandler, i);
                break;
            case NESTED:
                result = instance.nested(transactionHandler, i);
                break;
            case REQUIRED:
            default:
                result = instance.required(transactionHandler, i);
                break;
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public <I, O> O required(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public <I, O> O requiresNew(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.SUPPORTS)
    public <I, O> O supports(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public <I, O> O notSupported(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.MANDATORY)
    public <I, O> O mandatory(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NEVER)
    public <I, O> O never(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public <I, O> O nested(TransactionHandler<I, O> transactionHandler, I in) {
        return transactionWrapper(transactionHandler, in);
    }

    protected <I, O> O transactionWrapper(TransactionHandler<I, O> transactionHandler, I in) {
        O result = null;
        if (transactionHandler != null) {
            result = transactionHandler.exec(in);
        }
        return result;
    }
}
