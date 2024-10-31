package org.netcorepal.cap4j.ddd.domain.repo.impl;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 默认实体内联持久化监听器
 *
 * @author binking338
 * @date 2024/10/31
 */
public class DefaultEntityInlinePersistListener extends AbstractPersistListener<Object> {

    @SneakyThrows
    @Override
    public void onCreate(Object entity) {
        if (null == entity) {
            return;
        }
        Method createHandler = getHandlerMethod(entity.getClass(), "onCreate");
        if (null == createHandler) {
            return;
        }
        createHandler.invoke(entity);
    }

    @SneakyThrows
    @Override
    public void onUpdate(Object entity) {
        if (null == entity) {
            return;
        }
        Method updateHandler = getHandlerMethod(entity.getClass(), "onUpdate");
        if (updateHandler == null) {
            return;
        }
        updateHandler.invoke(entity);
    }

    @SneakyThrows
    @Override
    public void onDelete(Object entity) {
        if (null == entity) {
            return;
        }
        Method deleteHandler = getHandlerMethod(entity.getClass(), "onDelete");
        if (deleteHandler == null) {
            deleteHandler = getHandlerMethod(entity.getClass(), "onRemove");
        }
        if (deleteHandler == null) {
            return;
        }
        deleteHandler.invoke(entity);
    }

    static Map<String, Method> HANDLER_METHOD_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private Method getHandlerMethod(Class<?> clazz, String methodName) {
        return HANDLER_METHOD_CACHE.computeIfAbsent(clazz.getName() + "." + methodName, key -> {
            try {
                Method handler = clazz.getMethod(methodName);
                return handler;
            } catch (Exception ex) {
                /* don't care */
            }
            return null;
        });
    }
}
