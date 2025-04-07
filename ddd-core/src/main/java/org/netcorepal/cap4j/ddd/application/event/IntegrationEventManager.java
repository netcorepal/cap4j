package org.netcorepal.cap4j.ddd.application.event;

/**
 * 集成事件管理器
 *
 * @author binking338
 * @date 2024/9/11
 */
public interface IntegrationEventManager {

    /**
     * 发布附加到持久化上下文的所有集成事件
     */
    void release();
}
