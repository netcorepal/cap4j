package org.netcorepal.cap4j.ddd.domain.event;

/**
 * @author binking338
 * @date 2023/9/9
 */
public interface EventRecordRepository {
    public EventRecord create();
    public void save(EventRecord event);
}
