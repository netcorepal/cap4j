package ${basePackage}.adapter.infra.jpa;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * QueryDslConfig
 *
 * @author cap4j-ddd-codegen
 */
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {
    private final EntityManagerFactory entityManagerFactory;


    @PersistenceContext
    private EntityManager entityManager;


    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @Lazy(false)
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    @Bean
    public BlazeJPAQueryFactory blazeJPAQueryFactory(CriteriaBuilderFactory criteriaBuilderFactory) {
        return new BlazeJPAQueryFactory(entityManager, criteriaBuilderFactory);
    }
}
