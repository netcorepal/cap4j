package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于Jpa的实体持久化监听管理器
 *
 * @author binking338
 * @date 2024/3/9
 */
@RequiredArgsConstructor
@Slf4j
public class JpaPersistListenerManager implements PersistListenerManager {
    protected final List<AbstractJpaPersistListener> persistListeners;

    Map<Class, List<AbstractJpaPersistListener>> persistListenersMap;

    public void init() {
        if (persistListenersMap != null) {
            return;
        }
        synchronized (this) {
            if (persistListenersMap != null) {
                return;
            }
            persistListenersMap = new HashMap<>();
            persistListeners.sort((a, b) ->
                    OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE) - OrderUtils.getOrder(b.getClass(), Ordered.LOWEST_PRECEDENCE)
            );
            for (AbstractJpaPersistListener persistListener : persistListeners) {
                if (!persistListenersMap.containsKey(persistListener.forEntityClass())) {
                    persistListenersMap.put(persistListener.forEntityClass(), new java.util.ArrayList<AbstractJpaPersistListener>());
                }
                List<AbstractJpaPersistListener> persistListenerList = persistListenersMap.get(persistListener.forEntityClass());
                persistListenerList.add(persistListener);
            }
        }
    }




    /**
     * onCreate & onUpdate & onDelete
     * @param entity
     * @param <Entity>
     */
    @Override
    public <Entity> void onChange(Entity entity) {
        init();
        List<AbstractJpaPersistListener> listeners = persistListenersMap.get(entity.getClass());
        if (listeners != null) {
            for (AbstractJpaPersistListener listener :
                    listeners) {
                try {
                    listener.onChange(entity);
                } catch (Exception ex){
                    log.error("onPersist 异常", ex);
                    if(listener.throwOnException()){
                        throw ex;
                    }
                }
            }
        }
    }

    @Override
    public <Entity> void onCreate(Entity entity) {
        init();
        List<AbstractJpaPersistListener> listeners = persistListenersMap.get(entity.getClass());
        if (listeners != null) {
            for (AbstractJpaPersistListener listener :
                    listeners) {
                try {
                    listener.onCreate(entity);
                } catch (Exception ex){
                    log.error("onCreate 异常", ex);
                    if(listener.throwOnException()){
                        throw ex;
                    }
                }
            }
        }
    }

    @Override
    public <Entity> void onUpdate(Entity entity) {
        init();
        List<AbstractJpaPersistListener> listeners = persistListenersMap.get(entity.getClass());
        if (listeners != null) {
            for (AbstractJpaPersistListener listener :
                    listeners) {
                try {
                    listener.onUpdate(entity);
                } catch (Exception ex){
                    log.error("onUpdate 异常", ex);
                    if(listener.throwOnException()){
                        throw ex;
                    }
                }
            }
        }

    }

    @Override
    public <Entity> void onDelete(Entity entity) {
        init();
        List<AbstractJpaPersistListener> listeners = persistListenersMap.get(entity.getClass());
        if (listeners != null) {
            for (AbstractJpaPersistListener listener :
                    listeners) {
                try {
                    listener.onDelete(entity);
                } catch (Exception ex){
                    log.error("onDelete 异常", ex);
                    if(listener.throwOnException()){
                        throw ex;
                    }
                }
            }
        }

    }
}
