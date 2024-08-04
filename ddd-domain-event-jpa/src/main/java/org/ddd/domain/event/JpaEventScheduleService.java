package org.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.ddd.application.distributed.Locker;
import org.ddd.domain.event.persistence.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.ddd.share.Constants.CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_THREADPOOLSIIZE;
import static org.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;

/**
 * 事件调度服务
 * 失败定时重试
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
@Slf4j
public class JpaEventScheduleService {
    private static final String KEY_COMPENSATION_LOCKER = "event_compense[" + CONFIG_KEY_4_SVC_NAME + "]";
    private static final String KEY_ARCHIVE_LOCKER = "event_archive[" + CONFIG_KEY_4_SVC_NAME + "]";

    private final Locker locker;
    private final DomainEventPublisher domainEventPublisher;
    private final EventRepository eventRepository;
    private final ArchivedEventJpaRepository archivedEventJpaRepository;

    @Value(CONFIG_KEY_4_DISTRIBUTED_EVENT_SCHEDULE_THREADPOOLSIIZE)
    private int threadPoolsize;
    private ThreadPoolExecutor executor = null;

    @Value(CONFIG_KEY_4_SVC_NAME)
    private String svcName = null;

    private String getSvcName() {
        return svcName;
    }

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(threadPoolsize, threadPoolsize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Value(KEY_COMPENSATION_LOCKER)
    private String compensationLockerKey = null;

    private String getCompensationLockerKey() {
        return compensationLockerKey;
    }

    private boolean compensationRunning = false;

    public void compense(int batchSize, int maxConcurrency, Duration interval, Duration maxLockDuration) {
        if (compensationRunning) {
            log.info("事件发送补偿:上次事件发送补偿仍未结束，跳过");
            return;
        }
        compensationRunning = true;

        String pwd = RandomStringUtils.random(8, true, true);
        String svcName = getSvcName();
        String lockerKey = getCompensationLockerKey();
        try {
            boolean noneEvent = false;
            LocalDateTime now = LocalDateTime.now();
            while (!noneEvent) {
                try {
                    if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
                        return;
                    }
                    Page<Event> events = eventRepository.findAll((root, cq, cb) -> {
                        cq.where(cb.or(
                                cb.and(
                                        // 【初始状态】
                                        cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.INIT),
                                        cb.lessThan(root.get(Event.F_NEXT_TRY_TIME), now.plusSeconds(0)),
                                        cb.equal(root.get(Event.F_SVC_NAME), svcName)
                                ), cb.and(
                                        // 【未知状态】
                                        cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.DELIVERING),
                                        cb.lessThan(root.get(Event.F_NEXT_TRY_TIME), now.plusSeconds(0)),
                                        cb.equal(root.get(Event.F_SVC_NAME), svcName)
                                )));
                        return null;
                    }, PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, Event.F_CREATE_AT)));
                    if (!events.hasContent()) {
                        noneEvent = true;
                        continue;
                    }
                    for (Event event : events.getContent()) {
                        log.info("事件发送补偿: {}", event.toString());
                        event.holdState4Delivery(now);
                        event = eventRepository.saveAndFlush(event);
                        if(event.isDelivering(now)) {
                            EventRecordImpl eventRecordImpl = new EventRecordImpl();
                            eventRecordImpl.resume(event);
                            executor.submit(() -> domainEventPublisher.publish(eventRecordImpl));
                        }
                    }
                } catch (Exception ex) {
                    log.error("事件发送补偿:异常失败", ex);
                } finally {
                    locker.release(lockerKey, pwd);
                }
            }
        } finally {
            compensationRunning = false;
        }
    }

    @Value(KEY_ARCHIVE_LOCKER)
    private String archiveLockerKey = null;

    private String getArchiveLockerKey() {
        return archiveLockerKey;
    }

    /**
     * 本地事件库归档
     */
    public void archive(int expireDays, int batchSize, Duration maxLockDuration) {
        String pwd = RandomStringUtils.random(8, true, true);
        String svcName = getSvcName();
        String lockerKey = getArchiveLockerKey();

        if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
            return;
        }
        log.info("事件归档");

        Date now = new Date();
        int failCount = 0;
        while (true) {
            try {
                Page<Event> events = eventRepository.findAll((root, cq, cb) -> {
                    cq.where(
                            cb.and(
                                    // 【状态】
                                    cb.or(
                                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.CANCEL),
                                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.EXPIRED),
                                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.FAILED),
                                            cb.equal(root.get(Event.F_EVENT_STATE), Event.EventState.DELIVERED)
                                    ),
                                    cb.lessThan(root.get(Event.F_EXPIRE_AT), DateUtils.addDays(now, expireDays)),
                                    cb.equal(root.get(Event.F_SVC_NAME), svcName)
                            ));
                    return null;
                }, PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, Event.F_CREATE_AT)));
                if (!events.hasContent()) {
                    break;
                }
                List<ArchivedEvent> archivedEvents = events.stream().map(e -> ArchivedEvent.builder()
                        .id(e.getId())
                        .eventUuid(e.getEventUuid())
                        .svcName(e.getSvcName())
                        .eventType(e.getEventType())
                        .data(e.getData())
                        .dataType(e.getDataType())
                        .createAt(e.getCreateAt())
                        .expireAt(e.getExpireAt())
                        .eventState(e.getEventState())
                        .tryTimes(e.getTryTimes())
                        .triedTimes(e.getTriedTimes())
                        .lastTryTime(e.getLastTryTime())
                        .nextTryTime(e.getNextTryTime())
                        .version(e.getVersion())
                        .build()
                ).collect(Collectors.toList());
                migrate(events.toList(), archivedEvents);
            } catch (Exception ex) {
                failCount++;
                log.error("事件归档:失败", ex);
                if (failCount >= 3) {
                    log.info("事件归档:累计3次异常退出任务");
                    break;
                }
            }
        }
        locker.release(lockerKey, pwd);
    }

    @Transactional
    public void migrate(List<Event> events, List<ArchivedEvent> archivedEvents) {
        archivedEventJpaRepository.saveAll(archivedEvents);
        eventRepository.deleteInBatch(events);
    }

    public void addPartition() {
        Date now = new Date();
        addPartition("__event", DateUtils.addMonths(now, 1));
        addPartition("__archived_event", DateUtils.addMonths(now, 1));
    }

    private final JdbcTemplate jdbcTemplate;

    /**
     * 创建date日期所在月下个月的分区
     *
     * @param table
     * @param date
     */
    private void addPartition(String table, Date date) {
        String sql = "alter table `" + table + "` add partition (partition p" + DateFormatUtils.format(date, "yyyyMM") + " values less than (to_days('" + DateFormatUtils.format(DateUtils.addMonths(date, 1), "yyyy-MM") + "-01')) ENGINE=InnoDB)";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            if (!ex.getMessage().contains("Duplicate partition")) {
                log.error("分区创建异常 table = " + table + " partition = p" + DateFormatUtils.format(date, "yyyyMM"), ex);
            }
        }
    }
}
