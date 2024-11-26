package org.netcorepal.cap4j.ddd.archinfo.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * todo: 类描述
 *
 * @author binking338
 * @date 2024/11/24
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.archinfo")
public class ArchInfoProperties {
    boolean enabled = false;
    String basePackage;
}
