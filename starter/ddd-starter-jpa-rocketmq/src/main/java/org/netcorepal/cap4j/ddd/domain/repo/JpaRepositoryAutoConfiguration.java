package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 基于Jpa的仓储实现自动配置类
 *
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@ConditionalOnBean(RocketMQTemplate.class)
@RequiredArgsConstructor
public class JpaRepositoryAutoConfiguration {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DomainEventSupervisor domainEventSupervisor;
    private final DomainEventPublisher domainEventPublisher;
    private final DomainEventSubscriberManager domainEventSubscriberManager;
    private final EventRecordRepository eventRecordRepository;

    @Bean
    public JpaPersistListenerManager jpaPersistListenerManager(List<AbstractJpaPersistListener> persistListeners) {
        JpaPersistListenerManager persistListenerManager = new JpaPersistListenerManager(persistListeners);
        return persistListenerManager;
    }

    @Bean
    public JpaSpecificationManager jpaSpecificationManager(List<AbstractJpaSpecification> specifications) {
        JpaSpecificationManager specificationManager = new JpaSpecificationManager(specifications);
        return specificationManager;
    }

    @Bean
    public JpaUnitOfWork jpaUnitOfWork(JpaSpecificationManager jpaSpecificationManager, JpaPersistListenerManager jpaPersistListenerManager, @Autowired(required = false) DomainEventMessageInterceptor domainEventMessageInterceptor) {
        JpaUnitOfWork unitOfWork = new JpaUnitOfWork(applicationEventPublisher, domainEventSupervisor, domainEventPublisher, domainEventSubscriberManager, eventRecordRepository, jpaSpecificationManager, jpaPersistListenerManager, domainEventMessageInterceptor);
        return unitOfWork;
    }

    @Configuration
    private static class JpaLoader {
        public JpaLoader(@Autowired(required = false) JpaUnitOfWork jpaUnitOfWork) {
            JpaUnitOfWork.instance = jpaUnitOfWork;
        }
    }
}
