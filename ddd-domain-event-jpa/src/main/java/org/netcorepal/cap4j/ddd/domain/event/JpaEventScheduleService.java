package org.netcorepal.cap4j.ddd.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEvent;
import org.netcorepal.cap4j.ddd.domain.event.persistence.ArchivedEventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.event.persistence.Event;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private final EventPublisher eventPublisher;
    private final EventRecordRepository eventRecordRepository;
    private final EventJpaRepository eventJpaRepository;
    private final ArchivedEventJpaRepository archivedEventJpaRepository;
    private final Locker locker;
    private final String svcName;
    private final String compensationLockerKey;
    private final String archiveLockerKey;
    private final boolean enableAddPartition;
    private final JdbcTemplate jdbcTemplate;

    public void init() {
        addPartition();
    }

    private String getSvcName() {
        return svcName;
    }

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
                    List<EventRecord> eventRecords = eventRecordRepository.getByNextTryTime(svcName, now.plus(interval), batchSize);
                    if (eventRecords == null || eventRecords.isEmpty()) {
                        noneEvent = true;
                        continue;
                    }
                    for (EventRecord eventRecord : eventRecords) {
                        log.info("事件发送补偿: {}", eventRecord);
                        eventPublisher.retry(eventRecord, now.plus(interval));
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
                                    cb.lessThan(root.get(Event.F_EXPIRE_AT), DateUtils.addDays(now, expireDays)),
                                    cb.equal(root.get(Event.F_SVC_NAME), svcName)
                            ));
                    return null;
                }, PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, Event.F_NEXT_TRY_TIME)));
                if (!events.hasContent()) {
                    break;
                }
                List<ArchivedEvent> archivedEvents = events.stream().map(e -> {
                            ArchivedEvent ae = new ArchivedEvent();
                            ae.archiveFrom(e);
                            return ae;
                        }
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
        eventJpaRepository.deleteInBatch(events);
    }

    public void addPartition() {
        if (!enableAddPartition) {
            return;
        }
        Date now = new Date();
        addPartition("__event", DateUtils.addMonths(now, 1));
        addPartition("__archived_event", DateUtils.addMonths(now, 1));
    }

    /**
     * 创建date日期所在月下个月的分区
     *
     * @param table
     * @param date
     */
    private void addPartition(String table, Date date) {
        String sql = "alter table " + table + " add partition (partition p" + DateFormatUtils.format(date, "yyyyMM") + " values less than (to_days('" + DateFormatUtils.format(DateUtils.addMonths(date, 1), "yyyy-MM") + "-01')) ENGINE=InnoDB)";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            if (!ex.getMessage().contains("Duplicate partition")) {
                log.error("分区创建异常 table = " + table + " partition = p" + DateFormatUtils.format(date, "yyyyMM"), ex);
            }
        }
    }
}
