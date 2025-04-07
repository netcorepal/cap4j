package org.netcorepal.cap4j.ddd.domain.repo.querydsl;

import com.querydsl.core.types.Predicate;
import org.netcorepal.cap4j.ddd.domain.repo.JpaPageUtils;
import org.netcorepal.cap4j.ddd.domain.repo.JpaSortUtils;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;

import java.util.Collection;

/**
 * Querydsl仓储检索断言Support
 *
 * @author binking338
 * @date 2025/3/29
 */
public class QuerydslPredicateSupport {

    public static Sort resumeSort(org.netcorepal.cap4j.ddd.domain.repo.Predicate predicate, Collection<OrderInfo> orders) {
        if (!(predicate instanceof QuerydslPredicate)) {
            return null;
        }
        if (((QuerydslPredicate<?>) predicate).orderSpecifiers.size() > 0) {
            return new QSort(((QuerydslPredicate<?>) predicate).orderSpecifiers);
        } else {
            return JpaSortUtils.toSpringData(orders);
        }
    }

    public static Pageable resumePageable(org.netcorepal.cap4j.ddd.domain.repo.Predicate predicate, PageParam pageParam) {
        if (!(predicate instanceof QuerydslPredicate)) {
            return null;
        }
        if (((QuerydslPredicate<?>) predicate).orderSpecifiers.size() > 0) {
            return QPageRequest.of(pageParam.getPageNum(), pageParam.getPageSize(), new QSort(((QuerydslPredicate<?>) predicate).orderSpecifiers));
        } else {
            return JpaPageUtils.toSpringData(pageParam);
        }
    }

    public static <ENTITY> Predicate resumePredicate(org.netcorepal.cap4j.ddd.domain.repo.Predicate predicate) {
        if (!(predicate instanceof QuerydslPredicate)) {
            return null;
        }
        return ((QuerydslPredicate) predicate).predicate;
    }

    /**
     * 获取断言实体类型
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    public static <ENTITY> Class<ENTITY> reflectEntityClass(org.netcorepal.cap4j.ddd.domain.repo.Predicate<ENTITY> predicate) {
        if (!(predicate instanceof QuerydslPredicate)) {
            return null;
        }
        return ((QuerydslPredicate<ENTITY>) predicate).entityClass;
    }
}
