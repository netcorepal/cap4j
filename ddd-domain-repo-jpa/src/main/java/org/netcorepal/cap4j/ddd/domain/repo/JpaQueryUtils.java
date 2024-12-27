package org.netcorepal.cap4j.ddd.domain.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.impl.JpaUnitOfWork;

import java.util.List;
import java.util.Optional;

/**
 * JPA查询帮助类
 *
 * @author binking338
 * @date 2024/12/27
 */
@Slf4j
public class JpaQueryUtils {

    public interface QueryBuilder<R, F> {
        void build(CriteriaBuilder cb, CriteriaQuery<R> cq, Root<F> root);
    }

    private static JpaUnitOfWork jpaUnitOfWork;
    private static int retrieveCountWarnThreshold;

    public static void configure(JpaUnitOfWork jpaUnitOfWork, int retrieveCountWarnThreshold){
        JpaQueryUtils.jpaUnitOfWork = jpaUnitOfWork;
        JpaQueryUtils.retrieveCountWarnThreshold = retrieveCountWarnThreshold;
    }

    private static EntityManager getEntityManager() {
        return jpaUnitOfWork.getEntityManager();
    }

    /**
     * 自定义查询
     * 期待返回一条记录，数据异常返回0条或多条记录将抛出异常
     *
     * @param resultClass
     * @param fromEntityClass
     * @param queryBuilder
     * @param <R>
     * @param <F>
     * @return
     */
    public static <R, F> R queryOne(Class<R> resultClass, Class<F> fromEntityClass, QueryBuilder<R, F> queryBuilder) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultClass);
        Root<F> root = criteriaQuery.from(fromEntityClass);
        queryBuilder.build(criteriaBuilder, criteriaQuery, root);
        R result = getEntityManager().createQuery(criteriaQuery).getSingleResult();
        return result;
    }

    /**
     * 自定义查询
     * 返回0条或多条记录
     *
     * @param resultClass
     * @param fromEntityClass
     * @param queryBuilder
     * @param <R>
     * @param <F>
     * @return
     */
    public static <R, F> List<R> queryList(Class<R> resultClass, Class<F> fromEntityClass, QueryBuilder<R, F> queryBuilder) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultClass);
        Root<F> root = criteriaQuery.from(fromEntityClass);
        queryBuilder.build(criteriaBuilder, criteriaQuery, root);
        List<R> results = getEntityManager().createQuery(criteriaQuery).getResultList();
        if (results.size() > retrieveCountWarnThreshold) {
            log.warn("查询记录数过多: retrieve_count=" + results.size());
        }
        return results;
    }

    /**
     * 自定义查询
     * 如果存在符合筛选条件的记录，返回第一条记录
     *
     * @param resultClass
     * @param fromEntityClass
     * @param queryBuilder
     * @param <R>
     * @param <F>
     * @return
     */
    public static <R, F> Optional<R> queryFirst(Class<R> resultClass, Class<F> fromEntityClass, QueryBuilder<R, F> queryBuilder) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultClass);
        Root<F> root = criteriaQuery.from(fromEntityClass);
        queryBuilder.build(criteriaBuilder, criteriaQuery, root);
        List<R> results = getEntityManager().createQuery(criteriaQuery)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();
        return results.stream().findFirst();
    }

    /**
     * 自定义查询
     * 获取分页列表
     *
     * @param resultClass
     * @param fromEntityClass
     * @param queryBuilder
     * @param pageIndex
     * @param pageSize
     * @param <R>
     * @param <F>
     * @return
     */
    public static <R, F> List<R> queryPage(Class<R> resultClass, Class<F> fromEntityClass, QueryBuilder<R, F> queryBuilder, int pageIndex, int pageSize) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<R> criteriaQuery = criteriaBuilder.createQuery(resultClass);
        Root<F> root = criteriaQuery.from(fromEntityClass);
        queryBuilder.build(criteriaBuilder, criteriaQuery, root);
        List<R> results = getEntityManager().createQuery(criteriaQuery)
                .setFirstResult(pageSize * pageIndex).setMaxResults(pageSize)
                .getResultList();
        return results;
    }

    /**
     * 自定义查询
     * 返回查询计数
     *
     * @param fromEntityClass
     * @param queryBuilder
     * @param <F>
     * @return
     */
    public static <F> long count(Class<F> fromEntityClass, QueryBuilder<Long, F> queryBuilder) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<F> root = criteriaQuery.from(fromEntityClass);
        queryBuilder.build(criteriaBuilder, criteriaQuery, root);
        long total = getEntityManager().createQuery(criteriaQuery).getSingleResult().longValue();
        return total;
    }
}
