package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.UnitOfWork;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventManager;
import org.netcorepal.cap4j.ddd.application.impl.JpaUnitOfWork;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkSupport;
import org.netcorepal.cap4j.ddd.domain.aggregate.*;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.DefaultAggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.netcorepal.cap4j.ddd.domain.event.persistence.EventJpaRepository;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultPersistListenerManager;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.DefaultSpecificationManager;
import org.netcorepal.cap4j.ddd.domain.repo.configure.JpaUnitOfWorkProperties;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultRepositorySupervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 基于Jpa的仓储实现自动配置类
 * <p/>
 * DefaultPersistListenerManager
 * DefaultSpecificationManager
 * JpaUnitOfWork
 *
 * @author binking338
 * @date 2023/9/10
 */
@Configuration
@ConditionalOnBean(RocketMQTemplate.class)
@RequiredArgsConstructor
public class JpaRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PersistListenerManager.class)
    public PersistListenerManager defaultPersistListenerManager(
            List<PersistListener<?>> persistListeners,
            EventProperties eventProperties
    ) {
        DefaultPersistListenerManager persistListenerManager = new DefaultPersistListenerManager(
                persistListeners,
                eventProperties.getEventScanPackage());
        persistListenerManager.init();
        return persistListenerManager;
    }

    @Bean
    @ConditionalOnMissingBean(SpecificationManager.class)
    public SpecificationManager defaultSpecificationManager(List<Specification<?>> specifications) {
        DefaultSpecificationManager specificationManager = new DefaultSpecificationManager(specifications);
        specificationManager.init();
        return specificationManager;
    }

    @Bean
    @Primary
    public JpaUnitOfWork jpaUnitOfWork(
            DomainEventManager domainEventManager,
            IntegrationEventManager integrationEventManager,
            ApplicationEventPublisher applicationEventPublisher,
            SpecificationManager specificationManager,
            PersistListenerManager persistListenerManager,
            JpaUnitOfWorkProperties jpaUnitOfWorkProperties
    ) {
        JpaUnitOfWork unitOfWork = new JpaUnitOfWork(
                domainEventManager,
                integrationEventManager,
                specificationManager,
                persistListenerManager,
                applicationEventPublisher,
                jpaUnitOfWorkProperties.getRetrieveCountWarnThreshold());
        UnitOfWorkSupport.configure(unitOfWork);
        return unitOfWork;
    }

    @Bean
    @Primary
    public RepositorySupervisor defaultRepositorySupervisor(
            List<AbstractJpaRepository<?,?>> repositories,
            UnitOfWork unitOfWork
    ){
        DefaultRepositorySupervisor repositorySupervisor = new DefaultRepositorySupervisor(repositories, unitOfWork);
        repositorySupervisor.init();
        RepositorySupervisorSupport.configure(repositorySupervisor);
        return repositorySupervisor;
    }

    @Bean
    @Primary
    public AggregateFactorySupervisor defaultAggregateFactorySupervisor(
            List<AggregateFactory<?, ?>> factories
    ){
        DefaultAggregateFactorySupervisor aggregateFactorySupervisor = new DefaultAggregateFactorySupervisor(
            factories
        );
        aggregateFactorySupervisor.init();
        AggregateFactorySupervisorSupport.configure(aggregateFactorySupervisor);
        return aggregateFactorySupervisor;
    }


    @Configuration
    private static class __JpaUnitOfWorkLoader {
        public __JpaUnitOfWorkLoader(
                @Autowired(required = false)
                JpaUnitOfWork jpaUnitOfWork
        ) {
            JpaUnitOfWork.fixAopWrapper(jpaUnitOfWork);
        }
    }


    @Bean
    @ConditionalOnMissingBean(EventRecordRepository.class)
    public JpaEventRecordRepository jpaEventRecordRepository(
            EventJpaRepository eventJpaRepository
    ) {
        JpaEventRecordRepository eventRecordRepository =
                new JpaEventRecordRepository(
                        eventJpaRepository
                );
        return eventRecordRepository;
    }
}
