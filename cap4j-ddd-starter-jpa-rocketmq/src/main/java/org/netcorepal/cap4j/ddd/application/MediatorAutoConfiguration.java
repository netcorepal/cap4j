package org.netcorepal.cap4j.ddd.application;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.impl.DefaultMediator;
import org.netcorepal.cap4j.ddd.application.impl.DefaultRequestSupervisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public DefaultRequestSupervisor defaultRequestSupervisor(List<RequestHandler> requestHandlers){
        DefaultRequestSupervisor defaultRequestSupervisor = new DefaultRequestSupervisor(requestHandlers);
        defaultRequestSupervisor.init();
        RequestSupervisorConfiguration.configure(defaultRequestSupervisor);
        return defaultRequestSupervisor;
    }

    @Bean
    public DefaultMediator defaultMediator(){
        DefaultMediator defaultMediator = new DefaultMediator();
        MediatorConfiguration.configure(defaultMediator);
        return defaultMediator;
    }
}
