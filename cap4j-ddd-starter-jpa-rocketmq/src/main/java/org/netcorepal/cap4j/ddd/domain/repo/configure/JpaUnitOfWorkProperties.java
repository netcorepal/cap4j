package org.netcorepal.cap4j.ddd.domain.repo.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JpaUnitOfWork配置类
 *
 * @author binking338
 * @date 2024/8/11
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.domain.jpa-uow")
public class JpaUnitOfWorkProperties {
    /**
     * 单次获取记录数
     */
    int retrieveCountWarnThreshold = 3000;
}
