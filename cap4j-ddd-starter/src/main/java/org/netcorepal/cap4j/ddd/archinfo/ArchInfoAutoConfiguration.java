package org.netcorepal.cap4j.ddd.archinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.archinfo.configure.ArchInfoProperties;
import org.netcorepal.cap4j.ddd.archinfo.model.ArchInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.HttpRequestHandler;

import java.nio.charset.StandardCharsets;

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

    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "cap4j.ddd.archinfo.enabled", havingValue = "true")
    @Bean(name = "/cap4j/archinfo")
    public HttpRequestHandler archInfoRequestHandler(
            ArchInfoManager archInfoManager,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("ArchInfo URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/archinfo");
        return (req, res) -> {
            ArchInfo archInfo = archInfoManager.getArchInfo();
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(archInfo, SerializerFeature.SortField));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }
}
