package org.netcorepal.cap4j.ddd.application.distributed.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *  JdbcLocker配置类
 *
 * @author binking338
 * @date 2024/8/11
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.distributed.locker.jdbc")
public class JdbcLockerProperties {
    /**
     * 锁表名
     */
    String table = "`__locker`";
    /**
     * 锁名称字段名
     */
    String fieldName = "`name`";
    /**
     * 锁密码字段名
     */
    String fieldPwd = "`pwd`";
    /**
     * 锁获取时间字段名
     */
    String fieldLockAt = "`lock_at`";
    /**
     * 锁释放时间字段名
     */
    String fieldUnlockAt = "`unlock_at`";
}
