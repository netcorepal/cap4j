package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于Jpa的事件记录仓储实现
 *
 * @author binking338
 * @date 2023/9/9
 */
@RequiredArgsConstructor
public class JpaEventRecordRepository implements EventRecordRepository {
    private final EventRepository eventRepository;

    @Override
    public EventRecord create() {
        return new EventRecordImpl();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(EventRecord eventRecord) {
        EventRecordImpl eventRecordImpl = (EventRecordImpl) eventRecord;
        Event event = eventRepository.saveAndFlush(eventRecordImpl.getEvent());
        eventRecordImpl.resume(event);
    }
}
