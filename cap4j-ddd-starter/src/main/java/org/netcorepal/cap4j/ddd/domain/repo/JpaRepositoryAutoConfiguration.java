package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.application.UnitOfWorkSupport;
import org.netcorepal.cap4j.ddd.application.impl.JpaUnitOfWork;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactorySupervisorSupport;
import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;
import org.netcorepal.cap4j.ddd.domain.aggregate.SpecificationManager;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.DefaultAggregateFactorySupervisor;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.DefaultSpecificationManager;
import org.netcorepal.cap4j.ddd.domain.aggregate.impl.SpecificationUnitOfWorkInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.configure.EventProperties;
import org.netcorepal.cap4j.ddd.domain.repo.configure.JpaUnitOfWorkProperties;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultAggregateSupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultEntityInlinePersistListener;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultPersistListenerManager;
import org.netcorepal.cap4j.ddd.domain.repo.impl.DefaultRepositorySupervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@RequiredArgsConstructor
public class JpaRepositoryAutoConfiguration {

    @Bean
    public DefaultRepositorySupervisor defaultRepositorySupervisor(
            List<Repository<?>> repositories,
            JpaUnitOfWork unitOfWork
    ) {
        DefaultRepositorySupervisor repositorySupervisor = new DefaultRepositorySupervisor(repositories, unitOfWork);
        repositorySupervisor.init();
        RepositorySupervisorSupport.configure(repositorySupervisor);
        return repositorySupervisor;
    }

    @Bean
    public DefaultAggregateSupervisor defaultAggregateSupervisor(
            DefaultRepositorySupervisor repositorySupervisor
    ) {
        DefaultAggregateSupervisor aggregateSupervisor = new DefaultAggregateSupervisor(
                repositorySupervisor
        );
        AggregateSupervisorSupport.configure(aggregateSupervisor);
        return aggregateSupervisor;
    }

    @Bean
    public DefaultAggregateFactorySupervisor defaultAggregateFactorySupervisor(
            List<AggregateFactory<?, ?>> factories,
            JpaUnitOfWork jpaUnitOfWork
    ) {
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
            List<UnitOfWorkInterceptor> unitOfWorkInterceptors,
            PersistListenerManager persistListenerManager,
            JpaUnitOfWorkProperties jpaUnitOfWorkProperties
    ) {
        JpaUnitOfWork unitOfWork = new JpaUnitOfWork(
                unitOfWorkInterceptors,
                persistListenerManager,
                jpaUnitOfWorkProperties.isSupportEntityInlinePersistListener(),
                jpaUnitOfWorkProperties.isSupportValueObjectExistsCheckOnSave());
        UnitOfWorkSupport.configure(unitOfWork);
        JpaQueryUtils.configure(unitOfWork, jpaUnitOfWorkProperties.getRetrieveCountWarnThreshold());
        Md5HashIdentifierGenerator.configure(jpaUnitOfWorkProperties.getGeneralIdFieldName());
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

    @Bean
    public SpecificationUnitOfWorkInterceptor specificationUnitOfWorkInterceptor(SpecificationManager specificationManager) {
        SpecificationUnitOfWorkInterceptor specificationUnitOfWorkInterceptor = new SpecificationUnitOfWorkInterceptor(specificationManager);
        return specificationUnitOfWorkInterceptor;
    }

    @Bean
    @ConditionalOnProperty(
            name = "cap4j.ddd.application.jpa-uow.supportEntityInlinePersistListener",
            havingValue = "true",
            matchIfMissing = true
    )
    public DefaultEntityInlinePersistListener defaultEntityInlinePersistListener() {
        DefaultEntityInlinePersistListener entityInlinePersistListener = new DefaultEntityInlinePersistListener();
        return entityInlinePersistListener;
    }
}
