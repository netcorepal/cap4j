package org.netcorepal.cap4j.ddd.domain.repo;

import org.springframework.transaction.annotation.Propagation;

/**
 * UnitOfWork模式
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface UnitOfWork extends RepositorySupervisor {
    static UnitOfWork getInstance(){
        return UnitOfWorkConfiguration.instance;
    }

    /**
     * 新增或更新持久化记录
     * @param entity 实体对象
     */
    void persist(Object entity);

    /**
     * 移除持久化记录
     * @param entity 实体对象
     */
    void remove(Object entity);

    /**
     * 提交事务
     */
    void save();

    /**
     * 提交事务
     * @param propagation 事务传播特性
     */
    void save(Propagation propagation);

    /**
     * 重置上下文
     */
    void reset();
}
