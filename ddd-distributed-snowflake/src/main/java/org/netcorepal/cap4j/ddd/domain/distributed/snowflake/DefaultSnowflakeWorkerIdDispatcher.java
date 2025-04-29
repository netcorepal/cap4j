package org.netcorepal.cap4j.ddd.domain.distributed.snowflake;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 默认WorkerId分配器
 *
 * @author binking338
 * @date 2024/8/10
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultSnowflakeWorkerIdDispatcher implements SnowflakeWorkerIdDispatcher {
    private final JdbcTemplate jdbcTemplate;
    private final String table;
    private final String fieldDatacenterId;
    private final String fieldWorkerId;
    private final String fieldDispatchTo;
    private final String fieldDispatchAt;
    private final String fieldExpireAt;
    private final int expireMinutes;
    private final String localHostIdentify;

    private final Boolean showSql;

    private Long cachedWorkerId;

    public void init() {
        Long total = jdbcTemplate.queryForObject(String.format("SELECT count(*) FROM %s", table), Long.class);
        if (total != null && total >= 1024) {
            return;
        }
        for (int datacenterId = 0; datacenterId < 32; datacenterId++) {
            for (int workerId = 0; workerId < 32; workerId++) {
                Long count = jdbcTemplate.queryForObject(String.format("SELECT count(*) FROM %s WHERE %s=%d and %s=%d",
                        table,
                        fieldDatacenterId, datacenterId,
                        fieldWorkerId, workerId), Long.class);
                if (count != null && count == 0) {
                    jdbcTemplate.execute(String.format("INSERT INTO %s (%s, %s) VALUES (%d, %d)", table,
                            fieldDatacenterId, fieldWorkerId,
                            datacenterId, workerId));
                }
            }
        }
    }

    @SneakyThrows
    private String getHostIdentify() {
        if (localHostIdentify != null && !localHostIdentify.matches("^\\s*$")) {
            return localHostIdentify;
        }
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public long acquire(Long workerId, Long datacenterId) {
        if (cachedWorkerId != null) {
            return cachedWorkerId;
        }
        LocalDateTime now = LocalDateTime.now();
        String where = String.format("where (%s=? or %s<?)", fieldDispatchTo, fieldExpireAt);
        if (workerId != null) {
            where += String.format(" and %s=%d", fieldWorkerId, workerId);
        }
        if (datacenterId != null) {
            where += String.format(" and %s=%d", fieldDatacenterId, datacenterId);
        }
        String sql = String.format("SELECT ((%s<<5) + %s)  as r from %s %s order by %s asc limit 1",
                fieldDatacenterId, fieldWorkerId, table, where, fieldExpireAt);
        if (showSql) {
            log.debug(sql);
            log.debug(String.format("binding parameters: [%s, %s]", getHostIdentify(), now));
        }
        Optional<Long> r = jdbcTemplate.queryForList(sql, Long.class, getHostIdentify(), now).stream().findFirst();
        if (!r.isPresent()) {
            throw new RuntimeException("WorkerId分发失败");
        }
        sql = String.format("UPDATE %s set %s = ?, %s = ?, %s = ? WHERE (%s=? or %s<?) and ((%s<<5) + %s) = ?", table, fieldDispatchTo, fieldDispatchAt, fieldExpireAt, fieldDispatchTo, fieldExpireAt, fieldDatacenterId, fieldWorkerId);
        if (showSql) {
            log.debug(sql);
            log.debug(String.format("binding parameters: [%s, %s, %s, %s, %s, %s]", getHostIdentify(), now, now.plusMinutes(expireMinutes), getHostIdentify(), now, r.get()));
        }
        int success = jdbcTemplate.update(sql, getHostIdentify(), now, now.plusMinutes(expireMinutes), getHostIdentify(), now, r.get());
        if (success <= 0) {
            throw new RuntimeException("WorkerId分发失败");
        }
        cachedWorkerId = r.get();
        return cachedWorkerId;
    }

    @Override
    public void release() {
        LocalDateTime now = LocalDateTime.now();
        String sql = String.format("UPDATE %s set %s = ? WHERE %s = ? and ((%s<<5) + %s)  = ?", table, fieldExpireAt, fieldDispatchTo, fieldDatacenterId, fieldWorkerId);
        if (showSql) {
            log.debug(sql);
            log.debug(String.format("binding parameters: [%s, %s, %s]", now, getHostIdentify(), cachedWorkerId));
        }
        int success = jdbcTemplate.update(sql, now, getHostIdentify(), cachedWorkerId);
        if (success <= 0) {
            throw new RuntimeException("WorkerId释放失败");
        }
    }

    @Override
    public boolean pong() {
        LocalDateTime now = LocalDateTime.now();
        String sql = String.format("UPDATE %s set %s = ? WHERE %s = ? and ((%s<<5) + %s) = ?", table, fieldExpireAt, fieldDispatchTo, fieldDatacenterId, fieldWorkerId);
        if (showSql) {
            log.debug(sql);
            log.debug(String.format("binding parameters: [%s, %s, %s]", now.plusMinutes(expireMinutes), getHostIdentify(), cachedWorkerId));
        }
        int success = jdbcTemplate.update(sql, now.plusMinutes(expireMinutes), getHostIdentify(), cachedWorkerId);
        return success > 0;
    }

    @Override
    public void remind() {

    }
}
