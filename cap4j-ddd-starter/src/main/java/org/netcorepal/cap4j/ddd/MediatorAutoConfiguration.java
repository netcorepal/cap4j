package org.netcorepal.cap4j.ddd;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestInterceptor;
import org.netcorepal.cap4j.ddd.application.RequestSupervisorSupport;
import org.netcorepal.cap4j.ddd.impl.DefaultMediator;
import org.netcorepal.cap4j.ddd.application.impl.DefaultRequestSupervisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validator;
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
            List<RequestInterceptor<?,?>> requestInterceptors,
            Validator validator
    ){
        DefaultRequestSupervisor defaultRequestSupervisor = new DefaultRequestSupervisor(requestHandlers, requestInterceptors, validator);
        defaultRequestSupervisor.init();
        RequestSupervisorSupport.configure(defaultRequestSupervisor);
        return defaultRequestSupervisor;
    }

    @Bean
    @ConditionalOnMissingBean(Mediator.class)
    public DefaultMediator defaultMediator(ApplicationContext applicationContext){
        DefaultMediator defaultMediator = new DefaultMediator();
        MediatorSupport.configure(defaultMediator);
        MediatorSupport.configure(applicationContext);
        return defaultMediator;
    }

}
