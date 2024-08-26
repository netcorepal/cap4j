package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.ClassUtils;
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


    Class<Entity> forEntityClass(){
        return ((Class<Entity>) ClassUtils.findMethod(
                this.getClass(),
                "onChange",
                m -> m.getParameterCount() == 1
        ).getParameters()[0].getType());
    }

    public boolean throwOnException() {
        return true;
    }


    /**
     * 持久化变更
     * @param entity
     */
    @Override
    public void onChange(Entity entity){

    }

    /**
     * 新增实体时
     * @param entity
     */
    @Override
    public void onCreate(Entity entity){

    }

    /**
     * 更新实体时
     * @param entity
     */
    @Override
    public void onUpdate(Entity entity){

    }

    /**
     * 删除实体时
     * @param entity
     */
    @Override
    public void onDelete(Entity entity){

    }
}
