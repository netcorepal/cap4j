package org.netcorepal.cap4j.ddd.application;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.MediatorSupport;
import org.netcorepal.cap4j.ddd.impl.DefaultMediator;
import org.netcorepal.cap4j.ddd.application.impl.DefaultRequestSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * CQS自动配置类
 *
 * @author binking338
 * @date 2024/8/24
 */
@Configuration
@RequiredArgsConstructor
public class MediatorAutoConfiguration {
    @Bean
    public DefaultRequestSupervisor defaultRequestSupervisor(
            List<RequestHandler<?,?>> requestHandlers,
            List<RequestInterceptor<?,?>> requestInterceptors
    ){
        DefaultRequestSupervisor defaultRequestSupervisor = new DefaultRequestSupervisor(requestHandlers, requestInterceptors);
        defaultRequestSupervisor.init();
        RequestSupervisorSupport.configure(defaultRequestSupervisor);
        return defaultRequestSupervisor;
    }

    @Bean
    @ConditionalOnMissingBean(Mediator.class)
    public DefaultMediator defaultMediator(){
        DefaultMediator defaultMediator = new DefaultMediator();
        MediatorSupport.configure(defaultMediator);
        return defaultMediator;
    }
}
