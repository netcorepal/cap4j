package org.netcorepal.cap4j.ddd.domain.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * 基于Jpq的实体持久化监听抽象类
 *
 * @author binking338
 * @date 2024/3/9
 */
public abstract class AbstractJpaPersistListener<Entity> implements PersistListener<Entity> {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ApplicationContext applicationContext;

    public <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
    public <T> T getBean(String name,  Class<T> clazz){
        return applicationContext.getBean(name, clazz);
    }


    public abstract Class<Entity> forEntityClass();

    public boolean throwOnException() {
        return true;
    }
}
