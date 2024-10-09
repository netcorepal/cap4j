package org.netcorepal.cap4j.ddd.domain.event;

/**
 * 事件记录仓储
 *
 * @author binking338
 * @date 2023/9/9
 */
public interface EventRecordRepository {
    public EventRecord create();
    public void save(EventRecord event);
    public EventRecord getById(String id);
}
