package org.ddd.example.adapter._share.configure.mvc;

import lombok.RequiredArgsConstructor;
import org.ddd.domain.web.ClearDomainContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author <template/>
 * @date
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
