package ${basePackage}.adapter.portal.api._share.configure;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.web.ClearDomainContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mvc配置
 * @author cap4j-ddd-codegen
 */
@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    private final ClearDomainContextInterceptor clearDomainContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clearDomainContextInterceptor).addPathPatterns("/**");
    }
}
