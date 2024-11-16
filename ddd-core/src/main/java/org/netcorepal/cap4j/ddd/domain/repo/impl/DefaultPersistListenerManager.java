package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.DomainEventSupervisor;
import org.netcorepal.cap4j.ddd.domain.event.annotation.AutoAttach;
import org.netcorepal.cap4j.ddd.domain.repo.PersistListener;
import org.netcorepal.cap4j.ddd.domain.repo.PersistListenerManager;
import org.netcorepal.cap4j.ddd.domain.repo.PersistType;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;
import java.util.*;

/**
 * 默认实体持久化监听管理器
 *
 * @author binking338
 * @date 2024/3/9
 */
@RequiredArgsConstructor
public class DefaultPersistListenerManager implements PersistListenerManager {
    protected final List<PersistListener<?>> persistListeners;
    protected final String scanPath;

    Map<Class<?>, List<PersistListener<?>>> persistListenersMap;

    public void init() {
        if (null != persistListenersMap) {
            return;
        }
        synchronized (this) {
            if (null != persistListenersMap) {
                return;
            }
            persistListenersMap = new HashMap<>();
            persistListeners.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE)));
            for (PersistListener persistListener : persistListeners) {
                Class<?> entityClass = ClassUtils.resolveGenericTypeClass(
                    persistListener, 0,
                    AbstractPersistListener.class, PersistListener.class
                );
                subscribe(entityClass, persistListener);
            }

            ScanUtils.findDomainEventClasses(scanPath).stream()
                    .filter(domainEventClass ->
                            null != domainEventClass.getAnnotation(AutoAttach.class))
                    .forEach(domainEventClass -> {
                        AutoAttach autoAttach = domainEventClass.getAnnotation(AutoAttach.class);
                        Class<?> converterClass = null;
                        if(Converter.class.isAssignableFrom(domainEventClass)){
                            converterClass = domainEventClass;
                        }
                        if(Converter.class.isAssignableFrom(autoAttach.converterClass())){
                            converterClass = autoAttach.converterClass();
                        }
                        Converter<Object,Object> converter = ClassUtils.newConverterInstance(autoAttach.sourceEntityClass(), domainEventClass, converterClass);
                        subscribe(autoAttach.sourceEntityClass(), (e, t) -> {
                            for (PersistType listenPersistType : autoAttach.persistType()) {
                                if(listenPersistType == t){
                                    DomainEventSupervisor.getInstance().attach(converter.convert(e), e, Duration.ofSeconds(autoAttach.delayInSeconds()));
                                    DomainEventSupervisor.getManager().release(Collections.singleton(e));
                                    break;
                                }
                            }
                        });
                    });
        }
    }

    /**
     * 订阅持久化事件监听器
     * @param entityClass
     * @param persistListener
     */
    private void subscribe(Class<?> entityClass, PersistListener<?> persistListener){
        if (persistListenersMap == null) {
            persistListenersMap = new HashMap<>();
        }
        if(!persistListenersMap.containsKey(entityClass)){
            persistListenersMap.put(entityClass, new java.util.ArrayList<>());
        }
        persistListenersMap.get(entityClass).add(persistListener);
    }

    /**
     * onCreate & onUpdate & onDelete
     * @param entity
     * @param type
     * @param <Entity>
     */
    @Override
    public <Entity> void onChange(Entity entity, PersistType type) {
        init();
        List<PersistListener<?>> listeners = persistListenersMap.get(entity.getClass());
        if (listeners != null) {
            for (PersistListener<?> listener :
                    listeners) {
                try {
                    ((PersistListener<Entity>)listener).onChange(entity, type);
                } catch (Exception ex){
                    throw ex;
                }
            }
        }
        if(!Object.class.equals(entity.getClass())){
            listeners = persistListenersMap.get(Object.class);
            if (listeners != null) {
                for (PersistListener<?> listener :
                        listeners) {
                    try {
                        ((PersistListener<Object>)listener).onChange(entity, type);
                    } catch (Exception ex){
                        throw ex;
                    }
                }
            }
        }
    }
}
