package org.netcorepal.cap4j.ddd.archinfo;

import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.archinfo.configure.ArchInfoProperties;
import org.netcorepal.cap4j.ddd.archinfo.web.ArchInfoRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_NAME;
import static org.netcorepal.cap4j.ddd.share.Constants.CONFIG_KEY_4_SVC_VERSION;

/**
 * 架构信息自动配置
 *
 * @author binking338
 * @date 2024/11/24
 */
@Configuration
@Slf4j
public class ArchInfoAutoConfiguration {

    @Bean
    public ArchInfoManager archInfoManager(
            @Value(CONFIG_KEY_4_SVC_NAME)
            String name,
            @Value(CONFIG_KEY_4_SVC_VERSION)
            String version,
            ArchInfoProperties archInfoProperties
    ) {
        return new ArchInfoManager(name, version, archInfoProperties.getBasePackage());
    }


    @Value("${server.port:80}")
    private String serverPort;
    @Value("${server.servlet.context-path:/}")
    private String serverServletContentPath;

    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "cap4j.ddd.archinfo.enabled", havingValue = "true")
    @Bean(name = "/cap4j/archinfo")
    public ArchInfoRequestHandler archInfoRequestHandler(ArchInfoManager archInfoManager) {
        log.info("archinfo URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/archinfo");
        return new ArchInfoRequestHandler(archInfoManager);
    }
}
