package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEvent;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于Jpa的事件记录仓储实现
 *
 * @author binking338
 * @date 2023/9/9
 */
@RequiredArgsConstructor
public class JpaEventRecordRepository implements EventRecordRepository {
    private final EventJpaRepository eventJpaRepository;
    private final ArchivedEventJpaRepository archivedEventJpaRepository;

    @Override
    public EventRecord create() {
        return new EventRecordImpl();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(EventRecord eventRecord) {
        EventRecordImpl eventRecordImpl = (EventRecordImpl) eventRecord;
        Event event = eventRecordImpl.getEvent();
        event = eventJpaRepository.save(event);
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

    @Override
    public List<EventRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit) {
        Page<Event> events = eventJpaRepository.findAll((root, cq, cb) -> {
            cq.where(cb.or(
                    cb.and(
                            // 【初始状态】
                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.INIT),
                            cb.lessThan(root.get(Event.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Event.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【发送中状态】
                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.DELIVERING),
                            cb.lessThan(root.get(Event.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Event.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【异常状态】
                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.EXCEPTION),
                            cb.lessThan(root.get(Event.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Event.F_SVC_NAME), svcName)
                    )));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Event.F_NEXT_TRY_TIME)));
        return events.stream().map(event -> {
            EventRecordImpl eventRecordImpl = new EventRecordImpl();
            eventRecordImpl.resume(event);
            return eventRecordImpl;
        }).collect(Collectors.toList());
    }

    @Override
    public int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit) {
        Page<Event> events = eventJpaRepository.findAll((root, cq, cb) -> {
            cq.where(
                    cb.and(
                            // 【状态】
                            cb.or(
                                    cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.CANCEL),
                                    cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.EXPIRED),
                                    cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.EXHAUSTED),
                                    cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.DELIVERED)
                            ),
                            cb.lessThan(root.get(Event.F_EXPIRE_AT), maxExpireAt),
                            cb.equal(root.get(Event.F_SVC_NAME), svcName)
                    ));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Event.F_NEXT_TRY_TIME)));
        if(!events.hasContent()){
            return 0;
        }
        List<ArchivedEvent> archivedEvents = events.stream().map(event -> {
            ArchivedEvent archivedEvent = new ArchivedEvent();
            archivedEvent.archiveFrom(event);
            return archivedEvent;
        }).collect(Collectors.toList());
        migrate(events.getContent(), archivedEvents);
        return events.getNumberOfElements();
    }


    @Transactional
    public void migrate(List<Event> events, List<ArchivedEvent> archivedEvents) {
        archivedEventJpaRepository.saveAll(archivedEvents);
        eventJpaRepository.deleteInBatch(events);
    }
}
