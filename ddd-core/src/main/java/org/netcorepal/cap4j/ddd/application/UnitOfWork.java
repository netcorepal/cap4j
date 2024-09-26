package org.netcorepal.cap4j.ddd.application;

import org.springframework.transaction.annotation.Propagation;

/**
 * UnitOfWork模式
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface UnitOfWork {
    static UnitOfWork getInstance() {
        return UnitOfWorkSupport.instance;
    }

    /**
     * 提交新增或更新实体持久化记录意图到UnitOfWork上下文
     *
     * @param entity 实体对象
     */
    void persist(Object entity);

    /**
     * 提交移除实体持久化记录意图到UnitOfWork上下文
     *
     * @param entity 实体对象
     */
    void remove(Object entity);

    /**
     * 将持久化意图转换成持久化指令，并提交事务
     */
    void save();

    /**
     * 将持久化意图转换成持久化指令，并提交事务
     *
     * @param propagation 事务传播特性
     */
    void save(Propagation propagation);
}
