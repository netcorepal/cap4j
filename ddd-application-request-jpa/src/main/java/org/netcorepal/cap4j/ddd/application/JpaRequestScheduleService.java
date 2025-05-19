package org.netcorepal.cap4j.ddd.application;

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
 * 请求调度服务
 * 失败定时重试
 *
 * @author binking338
 * @date 2025/5/17
 */
@Slf4j
@RequiredArgsConstructor
public class JpaRequestScheduleService {
    private final RequestManager requestManager;
    private final Locker locker;
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
            log.info("Request执行补偿:上次Request执行补偿仍未结束，跳过");
            return;
        }
        compensationRunning = true;

        String pwd = TextUtils.randomString(8, true, true);
        String lockerKey = compensationLockerKey;
        try {
            boolean noneRequest = false;
            LocalDateTime now = LocalDateTime.now();
            while (!noneRequest) {
                try {
                    if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
                        return;
                    }
                    List<RequestRecord> requestRecords = requestManager.getByNextTryTime(now.plus(interval), batchSize);
                    if (requestRecords == null || requestRecords.isEmpty()) {
                        noneRequest = true;
                        continue;
                    }
                    for (RequestRecord requestRecord : requestRecords) {
                        log.info("Request执行补偿: {}", requestRecord);
                        requestManager.resume(requestRecord);
                    }
                } catch (Exception ex) {
                    log.error("Request执行补偿:异常失败", ex);
                } finally {
                    locker.release(lockerKey, pwd);
                }
            }
        } finally {
            compensationRunning = false;
        }
    }


    /**
     * Request归档
     */
    public void archive(int expireDays, int batchSize, Duration maxLockDuration) {
        String pwd = TextUtils.randomString(8, true, true);
        String lockerKey = archiveLockerKey;

        if (!locker.acquire(lockerKey, pwd, maxLockDuration)) {
            return;
        }
        log.info("Request归档");

        LocalDateTime now = LocalDateTime.now();
        int failCount = 0;
        while (true) {
            try {
                int archivedCount = requestManager.archiveByExpireAt(now.plusDays(expireDays), batchSize);
                if (archivedCount == 0) {
                    break;
                }
            } catch (Exception ex) {
                failCount++;
                log.error("Request归档:失败", ex);
                if (failCount >= 3) {
                    log.info("Request归档:累计3次异常退出任务");
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
        addPartition("__request", now.plusMonths(1));
        addPartition("__archived_request", now.plusMonths(1));
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
