package org.netcorepal.cap4j.ddd.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 基于Jpa的事件记录仓储实现
 *
 * @author binking338
 * @date 2023/9/9
 */
@RequiredArgsConstructor
public class JpaEventRecordRepository implements EventRecordRepository {
    private final EventJpaRepository eventJpaRepository;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public EventRecord create() {
        return new EventRecordImpl();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(EventRecord eventRecord) {
        EventRecordImpl eventRecordImpl = (EventRecordImpl) eventRecord;
        Event event = eventRecordImpl.getEvent();
        event = eventJpaRepository.saveAndFlush(event);
        eventRecordImpl.resume(event);
    }

    @Override
    public EventRecord getById(String id) {
        Event event = eventJpaRepository.findOne((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Event.F_EVENT_UUID), id))
                .orElseThrow(() -> new DomainException("EventRecord not found"));
        EventRecordImpl eventRecordImpl = new EventRecordImpl();
        eventRecordImpl.resume(event);
        return eventRecordImpl;
    }
}
