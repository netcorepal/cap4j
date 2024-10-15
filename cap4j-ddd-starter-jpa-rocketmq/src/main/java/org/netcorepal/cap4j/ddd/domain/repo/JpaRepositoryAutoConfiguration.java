package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.netcorepal.cap4j.ddd.application.event.IntegrationEventManager;
import org.netcorepal.cap4j.ddd.application.impl.JpaUnitOfWork;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkSupport;
import org.netcorepal.cap4j.ddd.domain.aggregate.*;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.DefaultAggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.event.*;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
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
    public DefaultRepositorySupervisor defaultRepositorySupervisor(
            List<AbstractJpaRepository<?,?>> repositories,
            JpaUnitOfWork unitOfWork
    ){
        DefaultRepositorySupervisor repositorySupervisor = new DefaultRepositorySupervisor(repositories, unitOfWork);
        repositorySupervisor.init();
        RepositorySupervisorSupport.configure(repositorySupervisor);
        return repositorySupervisor;
    }

    @Bean
    public DefaultAggregateFactorySupervisor defaultAggregateFactorySupervisor(
            List<AggregateFactory<?, ?>> factories,
            JpaUnitOfWork jpaUnitOfWork
    ){
        DefaultAggregateFactorySupervisor aggregateFactorySupervisor = new DefaultAggregateFactorySupervisor(
                factories,
                jpaUnitOfWork
        );
        aggregateFactorySupervisor.init();
        AggregateFactorySupervisorSupport.configure(aggregateFactorySupervisor);
        return aggregateFactorySupervisor;
    }

    @Bean
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
    @ConditionalOnMissingBean(PersistListenerManager.class)
    public DefaultPersistListenerManager defaultPersistListenerManager(
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
    public DefaultSpecificationManager defaultSpecificationManager(List<Specification<?>> specifications) {
        DefaultSpecificationManager specificationManager = new DefaultSpecificationManager(specifications);
        specificationManager.init();
        return specificationManager;
    }
}
