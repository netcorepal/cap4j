package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基于Jpa的仓储抽象类
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
public class AbstractJpaRepository<Entity, ID> implements Repository<Entity> {
    private final JpaSpecificationExecutor<Entity> jpaSpecificationExecutor;
    private final JpaRepository<Entity, ID> jpaRepository;

    public Optional<Entity> findOne(Predicate<Entity> predicate) {
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            return jpaRepository.findById((ID) JpaPredicateSupport.resumeId(predicate));
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            return jpaSpecificationExecutor.findOne(JpaPredicateSupport.resumeSpecification(predicate));
        }
        return Optional.empty();
    }

    public PageData<Entity> findPage(Predicate<Entity> predicate, PageParam pageParam) {
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return PageData.empty(pageParam.getPageSize(), JpaPredicateSupport.reflectEntityClass(predicate));
            }
            List<Entity> entities = jpaRepository.findAllById((Iterable<ID>) JpaPredicateSupport.resumeIds(predicate))
                    .stream()
                    .skip((pageParam.getPageNum() - 1) * pageParam.getPageSize())
                    .limit(pageParam.getPageSize())
                    .collect(Collectors.toList());
            return PageData.create(pageParam, Long.valueOf(entities.size()), entities);
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            Page<Entity> page = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), convertPageable(pageParam));
            return convertPageData(page);
        }
        return PageData.empty(pageParam.getPageSize(), JpaPredicateSupport.reflectEntityClass(predicate));
    }

    public List<Entity> find(Predicate<Entity> predicate, List<OrderInfo> orders) {
        Sort sort = Sort.unsorted();
        if (orders != null && !orders.isEmpty()) {
            sort = convertSort(orders);
        }
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return Collections.emptyList();
            }
            return jpaRepository.findAllById((Iterable<ID>) JpaPredicateSupport.resumeIds(predicate));
        }
        if (null != JpaPredicateSupport.resumeSpecification(predicate)) {
            List<Entity> entities = jpaSpecificationExecutor.findAll(JpaPredicateSupport.resumeSpecification(predicate), sort);
            return entities;
        }
        return Collections.emptyList();
    }

    public long count(Predicate<Entity> predicate) {
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            return jpaRepository.findById((ID) JpaPredicateSupport.resumeId(predicate)).isPresent() ?
                    1L :
                    0L;
        }
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return 0L;
            }
            return jpaRepository.findAllById((Iterable<ID>) JpaPredicateSupport.resumeIds(predicate))
                    .size();
        }
        long result = jpaSpecificationExecutor.count(JpaPredicateSupport.resumeSpecification(predicate));
        return result;
    }

    public boolean exists(Predicate<Entity> predicate) {
        if (null != JpaPredicateSupport.resumeId(predicate)) {
            return jpaRepository.findById((ID) JpaPredicateSupport.resumeId(predicate)).isPresent();
        }
        if (null != JpaPredicateSupport.resumeIds(predicate)) {
            if (!JpaPredicateSupport.resumeIds(predicate).iterator().hasNext()) {
                return false;
            }
            return jpaRepository.findAllById((Iterable<ID>) JpaPredicateSupport.resumeIds(predicate))
                    .size() > 0;
        }
        boolean result = jpaSpecificationExecutor.exists(JpaPredicateSupport.resumeSpecification(predicate));
        return result;
    }

    private Sort convertSort(List<OrderInfo> orders) {
        if (orders == null || orders.isEmpty()) {
            return Sort.unsorted();
        }
        Sort sort = Sort.by(orders.stream().map(order -> {
            if (order.getDesc()) {
                return Sort.Order.desc(order.getField());
            } else {
                return Sort.Order.asc(order.getField());
            }
        }).collect(Collectors.toList()));
        return sort;
    }

    private Pageable convertPageable(PageParam pageParam) {
        PageRequest pageRequest = null;

        Sort orders = convertSort(pageParam.getSort());
        pageRequest = PageRequest.of(pageParam.getPageNum() - 1, pageParam.getPageSize(), orders);
        return pageRequest;
    }

    private <T> PageData<T> convertPageData(Page<T> page) {
        return PageData.create(page.getPageable().getPageSize(), page.getPageable().getPageNumber() + 1, page.getTotalElements(), page.getContent());
    }
}
