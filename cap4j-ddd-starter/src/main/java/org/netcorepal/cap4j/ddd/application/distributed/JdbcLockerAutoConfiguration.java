package org.netcorepal.cap4j.ddd.application.distributed;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.distributed.configure.JdbcLockerProperties;
import org.netcorepal.cap4j.ddd.application.distributed.impl.ReentrantAspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Jdbc锁自动配置类
 *
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@RequiredArgsConstructor
public class JdbcLockerAutoConfiguration {
    private final JdbcTemplate jdbcTemplate;

    private static final String CONFIG_KEY_4_JPA_SHOW_SQL = "${spring.jpa.show-sql:${spring.jpa.showSql:false}}";

    @Bean
    @ConditionalOnMissingBean(value = Locker.class)
    public JdbcLocker jdbcLocker(
            JdbcLockerProperties properties,
            @Value(CONFIG_KEY_4_JPA_SHOW_SQL)
            boolean showSql
    ) {
        JdbcLocker jdbcLocker = new JdbcLocker(
                jdbcTemplate,
                properties.getTable(),
                properties.getFieldName(),
                properties.getFieldPwd(),
                properties.getFieldLockAt(),
                properties.getFieldUnlockAt(),
                showSql
        );
        return jdbcLocker;
    }

    @Bean
    public ReentrantAspect reentrantAspect(Locker locker) {
        ReentrantAspect reentrantAspect = new ReentrantAspect(locker);
        return reentrantAspect;
    }
}
