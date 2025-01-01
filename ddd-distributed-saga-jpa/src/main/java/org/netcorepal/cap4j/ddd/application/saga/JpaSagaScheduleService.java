package org.netcorepal.cap4j.ddd.application.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Saga调度服务
 * 失败定时重试
 *
 * @author binking338
 * @date 2024/10/14
 */
@Slf4j
@RequiredArgsConstructor
public class JpaSagaScheduleService {
    private final SagaRecordRepository sagaRecordRepository;
    private final Locker locker;
    private final String svcName;
    private final String compensationLockerKey;
    private final String archiveLockerKey;
    private final boolean enableAddPartition;
    private final JdbcTemplate jdbcTemplate;

    public void init() {
        addPartition();
    }

    private boolean compensationRunning = false;

    public void compense(int batchSize, int maxConcurrency, Duration interval, Duration maxLockDuration) {
        if (compensationRunning) {
            log.info("Saga执行补偿:上次Saga执行补偿仍未结束，跳过");
            return;
        }
        compensationRunning = true;

        String pwd = TextUtils.randomString(8, true, true);
        String lockerKey = compensationLockerKey;
        try {
            boolean noneSaga = false;
            LocalDateTime now = LocalDateTime.now();
            while (!noneSaga) {
                try {
                    if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
                        return;
                    }
                    List<SagaRecord> sagaRecords = sagaRecordRepository.getByNextTryTime(svcName, now.plus(interval), batchSize);
                    if (sagaRecords == null || sagaRecords.isEmpty()) {
                        noneSaga = true;
                        continue;
                    }
                    for (SagaRecord sagaRecord : sagaRecords) {
                        log.info("Saga执行补偿: {}", sagaRecord);
                        SagaSupervisor.getInstance().resume(sagaRecord);
                    }
                } catch (Exception ex) {
                    log.error("Saga执行补偿:异常失败", ex);
                } finally {
                    locker.release(lockerKey, pwd);
                }
            }
        } finally {
            compensationRunning = false;
        }
    }


    /**
     * Saga归档
     */
    public void archive(int expireDays, int batchSize, Duration maxLockDuration) {
        String pwd = TextUtils.randomString(8, true, true);
        String lockerKey = archiveLockerKey;

        if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
            return;
        }
        log.info("Saga归档");

        LocalDateTime now = LocalDateTime.now();
        int failCount = 0;
        while (true) {
            try {
                int archivedCount = sagaRecordRepository.archiveByExpireAt(svcName, now.plusDays(expireDays), batchSize);
                if (archivedCount == 0) {
                    break;
                }
            } catch (Exception ex) {
                failCount++;
                log.error("Saga归档:失败", ex);
                if (failCount >= 3) {
                    log.info("Saga归档:累计3次异常退出任务");
                    break;
                }
            }
        }
        locker.release(lockerKey, pwd);
    }

    public void addPartition() {
        if (!enableAddPartition) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        addPartition("__saga", now.plusMonths(1));
        addPartition("__archived_saga", now.plusMonths(1));
    }

    /**
     * 创建date日期所在月下个月的分区
     *
     * @param table
     * @param date
     */
    private void addPartition(String table, LocalDateTime date) {
        String sql = String.format("alter table %s add partition (partition p%s values less than (to_days('%s-01')) ENGINE=InnoDB)",
                table,
                date.format(DateTimeFormatter.ofPattern("yyyyMM")),
                date.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            if (!ex.getMessage().contains("Duplicate partition")) {
                log.error(String.format("分区创建异常 table = %s partition = p%s",
                                table,
                                date.format(DateTimeFormatter.ofPattern("yyyyMM"))
                        ),
                        ex);
            }
        }
    }
}
