package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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

//    public Optional<Entity> findById(Object id) {
//        List<ID> ids = new ArrayList<>(1);
//        ids.add((ID) id);
//        Optional<Entity> entity = jpaRepository.findAllById(ids).stream().findFirst();
//        return entity;
//    }
//
//    public List<Entity> findByIds(Iterable<Object> ids) {
//        List<Entity> entities = jpaRepository.findAllById((Iterable<ID>) ids);
//        return entities;
//    }
//
//    public boolean existsById(Object id) {
//        return jpaRepository.existsById((ID) id);
//    }

    public Optional<Entity> findOne(Predicate<Entity> predicate) {
        return jpaSpecificationExecutor.findOne(JpaPredicate.resume(predicate));
    }

    public PageData<Entity> findPage(Predicate<Entity> predicate, PageParam pageParam) {
        Page<Entity> page = jpaSpecificationExecutor.findAll(JpaPredicate.resume(predicate), convertPageable(pageParam));
        return convertPageData(page);
    }

    public List<Entity> find(Predicate<Entity> predicate, List<OrderInfo> orders) {
        Sort sort = Sort.unsorted();
        if (orders != null && !orders.isEmpty()) {
            sort = convertSort(orders);
        }
        List<Entity> entities = jpaSpecificationExecutor.findAll(JpaPredicate.resume(predicate), sort);
        return entities;
    }

    public long count(Predicate<Entity> condition) {
        long result = jpaSpecificationExecutor.count((org.springframework.data.jpa.domain.Specification<Entity>) condition);
        return result;
    }

    public boolean exists(Predicate<Entity> condition) {
        boolean result = jpaSpecificationExecutor.exists((org.springframework.data.jpa.domain.Specification<Entity>) condition);
        return result;
    }

    private Sort convertSort(List<OrderInfo> orders) {
        Sort sort = Sort.unsorted();
        if (orders != null && !orders.isEmpty()) {
            Sort.by(orders.stream().map(order -> {
                if (order.getDesc()) {
                    return Sort.Order.desc(order.getField());
                } else {
                    return Sort.Order.asc(order.getField());
                }
            }).collect(Collectors.toList()));
        }
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
