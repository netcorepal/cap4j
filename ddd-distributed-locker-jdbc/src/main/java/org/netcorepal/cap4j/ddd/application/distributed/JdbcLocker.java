package org.netcorepal.cap4j.ddd.application.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.netcorepal.cap4j.ddd.share.Constants.*;

/**
 * @author binking338
 * @date 2023/8/17
 */
@RequiredArgsConstructor
@Slf4j
public class JdbcLocker implements Locker {
    private final JdbcTemplate jdbcTemplate;
    private ConcurrentHashMap<String, LocalDateTime> lockerExpireMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> lockerPwdMap = new ConcurrentHashMap<>();

    @Value(CONFIG_KEY_4_DISTRIBUTED_LOCKER_JDBC_TABLE)
    private String table = "__locker";
    @Value(CONFIG_KEY_4_DISTRIBUTED_LOCKER_JDBC_FIELD_NAME)
    private String fieldName = "name";
    @Value(CONFIG_KEY_4_DISTRIBUTED_LOCKER_JDBC_FIELD_PWD)
    private String fieldPwd = "pwd";
    @Value(CONFIG_KEY_4_DISTRIBUTED_LOCKER_JDBC_FIELD_LOCKAT)
    private String fieldLockAt = "lock_at";
    @Value(CONFIG_KEY_4_DISTRIBUTED_LOCKER_JDBC_FIELD_UNLOCKAT)
    private String fieldUnlockAt = "unlock_at";

    @Value("${spring.jpa.show-sql:${spring.jpa.showSql:false}}")
    private Boolean showSql = false;

    @Override
    public boolean acquire(String key, String pwd, Duration expireDuration) {
        LocalDateTime now = LocalDateTime.now();
        synchronized (this) {
            if (lockerExpireMap.containsKey(key)) {
                // 过期
                if (lockerExpireMap.get(key).isBefore(now)) {
                    lockerExpireMap.remove(key);
                    lockerPwdMap.remove(key);
                } else {
                    if (!Objects.equals(lockerPwdMap.get(key), pwd)) {
                        return false;
                    }
                }
            }
            String sql = String.format("select count(*) from `%s` where `%s` = ? ", table, fieldName);
            if (showSql) {
                log.info(sql);
                log.info(String.format("binding parameters: [%s]", key));
            }
            Integer exists = jdbcTemplate.queryForObject(sql, Integer.class, key);
            if (exists == null || exists == 0) {
                try {
                    sql = String.format("insert into `%s`(`%s`, `%s`, `%s`, `%s`) values(?, ?, ?, ?)", table, fieldName, fieldPwd, fieldLockAt, fieldUnlockAt);
                    if (showSql) {
                        log.info(sql);
                        log.info(String.format("binding parameters: [%s, %s, %s, %s]", key, pwd, now, now.plusSeconds(expireDuration.getSeconds())));
                    }
                    jdbcTemplate.update(sql, key, pwd, now, now.plusSeconds(expireDuration.getSeconds()));
                    lockerExpireMap.put(key, now.plusSeconds(expireDuration.getSeconds()));
                    lockerPwdMap.put(key, pwd);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            } else {
                try {
                    sql = String.format("update `%s` set `%s` = ?, `%s` = ?, `%s` = ? where `%s` = ? and (`%s` < ? or `%s` = ?)", table, fieldPwd, fieldLockAt, fieldUnlockAt, fieldName, fieldUnlockAt, fieldPwd);
                    if (showSql) {
                        log.info(sql);
                        log.info(String.format("binding parameters: [%s, %s, %s, %s, %s, %s]", pwd, now, now.plusSeconds(expireDuration.getSeconds()), key, now, pwd));
                    }
                    int success = jdbcTemplate.update(sql, pwd, now, now.plusSeconds(expireDuration.getSeconds()), key, now, pwd);
                    return success > 0;
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }

    @Override
    public boolean release(String key, String pwd) {
        LocalDateTime now = LocalDateTime.now();
        if (lockerExpireMap.containsKey(key)) {
            if (Objects.equals(lockerPwdMap.get(key), pwd)) {
                lockerExpireMap.remove(key);
                lockerPwdMap.remove(key);
            } else {
                return false;
            }
        }
        String sql = String.format("select count(*) from `%s` where `%s` = ? and `%s` = ?", table, fieldName, fieldPwd);
        if (showSql) {
            log.info(sql);
            log.info(String.format("binding parameters: [%s, %s]", key, pwd));
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, key, pwd);
        if (count == null || count == 0) {
            return false;
        }
        sql = String.format("update `%s` set `%s` = ? where `%s` = ? and `%s` = ? and `%s` > ?", table, fieldUnlockAt, fieldName, fieldPwd, fieldUnlockAt);
        if (showSql) {
            log.info(sql);
            log.info(String.format("binding parameters: [%s, %s, %s, %s]", now, key, pwd, now));
        }
        jdbcTemplate.update(sql, now, key, pwd, now);
        return true;
    }
}
