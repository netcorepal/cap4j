package org.netcorepal.cap4j.ddd.application.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventManager;
import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;
import org.netcorepal.cap4j.ddd.domain.aggregate.SpecificationManager;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventManager;
import org.netcorepal.cap4j.ddd.domain.repo.PersistListenerManager;
import org.netcorepal.cap4j.ddd.domain.repo.PersistType;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final DomainEventManager domainEventManager;
    private final IntegrationEventManager integrationEventManager;
    private final SpecificationManager specificationManager;
    private final PersistListenerManager persistListenerManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final boolean supportEntityInlinePersistListener;
    private final boolean supportValueObjectExistsCheckOnSave;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;
    protected static JpaUnitOfWork instance;

    public static void fixAopWrapper(JpaUnitOfWork unitOfWork) {
        instance = unitOfWork;
    }

    private static ThreadLocal<Set<Object>> persistedEntitiesThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Set<Object>> removedEntitiesThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Set<Object>> processingThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<EntityPersisttedEvent> entityPersisttedEventThreadLocal = new ThreadLocal<>();

    private static ConcurrentHashMap<Class<?>, EntityInformation> entityInformationCache = new ConcurrentHashMap<>();

    private EntityInformation getEntityInformation(Class<?> entityClass) {
        return entityInformationCache.computeIfAbsent(entityClass, cls -> {
            return JpaEntityInformationSupport.getEntityInformation(cls, getEntityManager());
        });
    }

    @Override
    public void persist(Object entity) {
        if (entity instanceof ValueObject) {
            boolean exists = null != getEntityManager()
                    .find(entity.getClass(), ((ValueObject<?>) entity).hash());
            if (exists) {
                return;
            }
        }
        if (persistedEntitiesThreadLocal.get() == null) {
            persistedEntitiesThreadLocal.set(new LinkedHashSet<>());
        } else if (persistedEntitiesThreadLocal.get().contains(entity)) {
            return;
        }
        persistedEntitiesThreadLocal.get().add(entity);
    }

    @Override
    public boolean persistIfNotExist(Object entity) {
        if (entity instanceof ValueObject) {
            boolean exists = null != getEntityManager()
                    .find(entity.getClass(), ((ValueObject<?>) entity).hash());
            if (!exists) {
                persist(entity);
            }
            return !exists;
        }
        EntityInformation entityInformation = getEntityInformation(entity.getClass());
        if (entityInformation.isNew(entity)) {
            persist(entity);
            return true;
        }
        Object id = entityInformation.getId(entity);
        if (id == null || null == getEntityManager().find(entity.getClass(), id)) {
            persist(entity);
            return true;
        }
        return false;
    }

    @Override
    public void remove(Object entity) {
        if (removedEntitiesThreadLocal.get() == null) {
            removedEntitiesThreadLocal.set(new LinkedHashSet<>());
        } else if (removedEntitiesThreadLocal.get().contains(entity)) {
            return;
        }
        removedEntitiesThreadLocal.get().add(entity);
    }

    @Override
    public void save() {
        save(Propagation.REQUIRED);
    }

    private boolean pushProcessingEntities(Object entity, Set<Object> currentProcessedPersistenceContextEntities) {
        if (null == entity) {
            return false;
        }
        if (processingThreadLocal.get() == null) {
            processingThreadLocal.set(new LinkedHashSet<>());
            processingThreadLocal.get().add(entity);
            currentProcessedPersistenceContextEntities.add(entity);
            return true;
        } else if (!processingThreadLocal.get().contains(entity)) {
            processingThreadLocal.get().add(entity);
            currentProcessedPersistenceContextEntities.add(entity);
            return true;
        }
        return false;
    }

    private boolean popProcessingEntities(Set<Object> currentProcessedPersistenceContextEntities) {
        if (processingThreadLocal.get() != null && currentProcessedPersistenceContextEntities != null) {
            return processingThreadLocal.get().removeAll(currentProcessedPersistenceContextEntities);
        }
        return true;
    }

    @Override
    public void save(Propagation propagation) {
        Set<Object> currentProcessedEntitySet = new LinkedHashSet<>();

        Set<Object> persistEntitySet = null;
        if (persistedEntitiesThreadLocal.get() != null) {
            persistEntitySet = new LinkedHashSet<>(persistedEntitiesThreadLocal.get());
            persistedEntitiesThreadLocal.get().clear();
            persistEntitySet.forEach(e -> pushProcessingEntities(e, currentProcessedEntitySet));
        } else {
            persistEntitySet = new LinkedHashSet<>();
        }
        Set<Object> deleteEntitySet = null;
        if (removedEntitiesThreadLocal.get() != null) {
            deleteEntitySet = new LinkedHashSet<>(removedEntitiesThreadLocal.get());
            removedEntitiesThreadLocal.get().clear();
            deleteEntitySet.forEach(e -> pushProcessingEntities(e, currentProcessedEntitySet));
        } else {
            deleteEntitySet = new LinkedHashSet<>();
        }
        if (null == entityPersisttedEventThreadLocal.get()) {
            entityPersisttedEventThreadLocal.set(new EntityPersisttedEvent(this, new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>()));
        }
        specifyEntitiesBeforeTransaction(persistEntitySet);
        Set<Object>[] saveAndDeleteEntities = new Set[]{persistEntitySet, deleteEntitySet, currentProcessedEntitySet};
        save(input -> {
            Set<Object> persistEntities = input[0];
            Set<Object> deleteEntities = input[1];
            Set<Object> processedEntities = input[2];
            specifyEntitiesInTransaction(persistEntities);
            boolean flush = false;
            List<Object> refreshEntityList = null;
            if (persistEntities != null && !persistEntities.isEmpty()) {
                flush = true;
                for (Object entity : persistEntities) {
                    EntityInformation entityInformation = getEntityInformation(entity.getClass());
                    if (!entityInformation.isNew(entity)) {
                        if (!getEntityManager().contains(entity)) {
                            if (supportValueObjectExistsCheckOnSave && entity instanceof ValueObject) {
                                Object hash = ((ValueObject) entity).hash();
                                boolean exists = null != getEntityManager().find(entity.getClass(), hash);
                                if (exists) {
                                    getEntityManager().merge(entity);
                                } else {
                                    getEntityManager().persist(entity);
                                }
                            } else {
                                getEntityManager().merge(entity);
                            }
                        }
                        entityPersisttedEventThreadLocal.get().getUpdatedEntities().add(entity);
                    } else {
                        if (!getEntityManager().contains(entity)) {
                            getEntityManager().persist(entity);
                        }
                        if (refreshEntityList == null) {
                            refreshEntityList = new ArrayList<>();
                        }
                        refreshEntityList.add(entity);
                        entityPersisttedEventThreadLocal.get().getCreatedEntities().add(entity);
                    }
                }
            }
            if (deleteEntities != null && !deleteEntities.isEmpty()) {
                flush = true;
                for (Object entity : deleteEntities) {
                    if (getEntityManager().contains(entity)) {
                        getEntityManager().remove(entity);
                    } else {
                        getEntityManager().remove(getEntityManager().merge(entity));
                    }
                    entityPersisttedEventThreadLocal.get().getDeletedEntities().add(entity);
                }
            }

            if (flush) {
                getEntityManager().flush();
                if (refreshEntityList != null && !refreshEntityList.isEmpty()) {
                    for (Object entity : refreshEntityList) {
                        getEntityManager().refresh(entity);
                    }
                }
                EntityPersisttedEvent entityPersisttedEvent = entityPersisttedEventThreadLocal.get();
                entityPersisttedEventThreadLocal.remove();
                applicationEventPublisher.publishEvent(entityPersisttedEvent);
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
            domainEventManager.release(entities);
            integrationEventManager.release();
            return null;
        }, saveAndDeleteEntities, propagation);
        popProcessingEntities(currentProcessedEntitySet);
    }

    public static void reset() {
        persistedEntitiesThreadLocal.remove();
        removedEntitiesThreadLocal.remove();
        processingThreadLocal.remove();
        entityPersisttedEventThreadLocal.remove();
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

    /**
     * 校验持久化实体
     *
     * @param entities
     */
    protected void specifyEntitiesInTransaction(Collection<Object> entities) {
        if (entities != null && !entities.isEmpty()) {
            for (Object entity : entities) {
                Specification.Result result = specificationManager.specifyInTransaction(entity);
                if (!result.isPassed()) {
                    throw new DomainException(result.getMessage());
                }
            }
        }
    }

    /**
     * 校验持久化实体(事务开启前)
     *
     * @param entities
     */
    protected void specifyEntitiesBeforeTransaction(Collection<Object> entities) {
        if (entities != null && !entities.isEmpty()) {
            for (Object entity : entities) {
                Specification.Result result = specificationManager.specifyBeforeTransaction(entity);
                if (!result.isPassed()) {
                    throw new DomainException(result.getMessage());
                }
            }
        }
    }

    /**
     * UoW实体持久化事件
     */
    public static class EntityPersisttedEvent extends ApplicationEvent {
        @Getter
        Set<Object> createdEntities;
        @Getter
        Set<Object> updatedEntities;
        @Getter
        Set<Object> deletedEntities;

        public EntityPersisttedEvent(Object source, Set<Object> createdEntities, Set<Object> updatedEntities, Set<Object> deletedEntities) {
            super(source);
            this.createdEntities = createdEntities;
            this.updatedEntities = updatedEntities;
            this.deletedEntities = deletedEntities;
        }
    }

    @EventListener(classes = EntityPersisttedEvent.class)
    public void onTransactionCommiting(EntityPersisttedEvent event) {
        if (!supportEntityInlinePersistListener) {
            return;
        }
        for (Object entity : event.getCreatedEntities()) {
            persistListenerManager.onChange(entity, PersistType.CREATE);
        }
        for (Object entity : event.getUpdatedEntities()) {
            persistListenerManager.onChange(entity, PersistType.UPDATE);
        }
        for (Object entity : event.getDeletedEntities()) {
            persistListenerManager.onChange(entity, PersistType.DELETE);
        }
    }
}
