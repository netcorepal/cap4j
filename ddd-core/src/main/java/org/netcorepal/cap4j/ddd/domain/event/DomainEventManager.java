package org.netcorepal.cap4j.ddd.domain.event;

import java.util.Set;

/**
 * 领域事件发布管理器
 *
 * @author binking338
 * @date 2024/9/11
 */
public interface DomainEventManager {

    /**
     * 发布附加到指定实体以及所有未附加到实体的领域事件
     * @param entities 指定实体集合
     */
    void release(Set<Object> entities);
}
