package org.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.ddd.domain.event.DomainEventPublisher;
import org.ddd.domain.event.DomainEventSubscriberManager;
import org.ddd.domain.event.DomainEventSupervisor;
import org.ddd.domain.event.EventRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
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
    public JpaPersistListenerManager jpaPersistListenerManager(List<AbstractJpaPersistListener> persistListeners){
        JpaPersistListenerManager persistListenerManager = new JpaPersistListenerManager(persistListeners);
        return persistListenerManager;
    }

    @Bean
    public JpaSpecificationManager jpaSpecificationManager(List<AbstractJpaSpecification> specifications){
        JpaSpecificationManager specificationManager = new JpaSpecificationManager(specifications);
        return specificationManager;
    }

    @Bean
    public JpaUnitOfWork jpaUnitOfWork(JpaSpecificationManager jpaSpecificationManager, JpaPersistListenerManager jpaPersistListenerManager){
        JpaUnitOfWork unitOfWork = new JpaUnitOfWork(applicationEventPublisher, domainEventSupervisor, domainEventPublisher, domainEventSubscriberManager, eventRecordRepository, jpaSpecificationManager, jpaPersistListenerManager);
        return unitOfWork;
    }

    @Configuration
    private static class JpaLoader {
        public JpaLoader(@Autowired(required = false) JpaUnitOfWork jpaUnitOfWork){
            JpaUnitOfWork.instance = jpaUnitOfWork;
        }
    }
}
