package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/**
 * 分页工具类
 *
 * @author binking338
 * @date 2024/5/13
 */
public class JpaPageUtils {

    /**
     * 从JPA转换
     *
     * @return
     */
    public static <T> PageData<T> fromSpringData(Page<T> page) {
        if (page == null) {
            return PageData.empty(10, null);
        }
        return PageData.create(page.getPageable().getPageSize(), page.getPageable().getPageNumber() + 1, page.getTotalElements(), page.getContent());
    }

    /**
     * 从JPA转换
     *
     * @return
     */
    public static <S, D> PageData<D> fromSpringData(Page<S> page, Class<D> desClass) {
        return fromSpringData(page).transform(s -> {
            D d = null;
            try {
                d = (D) desClass.newInstance();
                BeanUtils.copyProperties(s, d);
            } catch (Throwable throwable) {
                throw new DomainException("分页类型转换异常", throwable);
            }
            return d;
        });
    }

    /**
     * 从JPA转换
     *
     * @return
     */
    public static <S, D> PageData<D> fromSpringData(Page<S> page, Function<S, D> transformer) {
        return fromSpringData(page).transform(transformer);
    }


    /**
     * @param param
     * @return
     */
    public static Pageable toSpringData(PageParam param) {
        if (param == null) {
            return PageRequest.of(0, 10);
        }
        PageRequest pageRequest = null;
        if (param.getSort() == null || param.getSort().size() == 0) {
            pageRequest = PageRequest.of(param.getPageNum() - 1, param.getPageSize());
        } else {
            Sort orders = JpaSortUtils.toSpringData(param.getSort());
            pageRequest = PageRequest.of(param.getPageNum() - 1, param.getPageSize(), orders);
        }
        return pageRequest;
    }
}
