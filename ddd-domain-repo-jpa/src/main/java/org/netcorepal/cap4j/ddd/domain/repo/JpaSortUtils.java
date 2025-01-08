package org.netcorepal.cap4j.ddd.domain.repo;

import org.netcorepal.cap4j.ddd.share.OrderInfo;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排序工具类
 *
 * @author binking338
 * @date 2025/1/7
 */
public class JpaSortUtils {
    /**
     * 将OrderInfo列表转换为Spring Data的Sort对象
     *
     * @param orders OrderInfo列表
     * @return Sort对象
     */
    public static Sort toSpringData(List<OrderInfo> orders) {
        return Sort.by(orders.stream()
                .map(orderInfo -> new Sort.Order(
                        orderInfo.getDesc()
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC,
                        orderInfo.getField()))
                .collect(Collectors.toList())
        );
    }

    /**
     * 将OrderInfo数组转换为Spring Data的Sort对象
     *
     * @param orders OrderInfo数组
     * @return Sort对象
     */
    public static Sort toSpringData(OrderInfo... orders) {
        return toSpringData(Arrays.asList(orders));
    }
}
