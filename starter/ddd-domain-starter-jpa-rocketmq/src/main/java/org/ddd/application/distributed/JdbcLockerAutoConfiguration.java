package org.ddd.application.distributed;

import lombok.RequiredArgsConstructor;
import org.ddd.application.distributed.JdbcLocker;
import org.ddd.application.distributed.Locker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@RequiredArgsConstructor
public class JdbcLockerAutoConfiguration {
    private final JdbcTemplate jdbcTemplate;

    @Bean
    @ConditionalOnMissingBean(value = Locker.class)
    public JdbcLocker jdbcLocker() {
        JdbcLocker jdbcLocker = new JdbcLocker(jdbcTemplate);
        return jdbcLocker;
    }
}
