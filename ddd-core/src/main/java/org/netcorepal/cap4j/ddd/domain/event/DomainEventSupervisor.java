package org.netcorepal.cap4j.ddd.domain.event;

import java.util.List;

/**
 * @author binking338
 * @date 2023/8/12
 */
public interface DomainEventSupervisor {
    /**
     * 附加事件
     * @param eventPayload
     */
    void attach(Object eventPayload);

    /**
     * 剥离事件
     * @param eventPayload
     */
    void detach(Object eventPayload);
    /**
     * 重置事件
     */
    void reset();

    /**
     * 获取事件列表
     * @return
     */
    List<Object> getEvents();
}
