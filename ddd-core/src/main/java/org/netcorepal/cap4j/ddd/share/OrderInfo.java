package org.netcorepal.cap4j.ddd.share;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 排序定义
 *
 * @author binking338
 * @date 2023/8/13
 */
@Data
public class OrderInfo {
    /**
     * 排序字段
     */
    String field;
    /**
     * 是否降序
     */
    Boolean desc;

    /**
     * 降序
     *
     * @param field
     * @return
     */
    public static OrderInfo desc(Object field) {
        return desc(field.toString());
    }

    /**
     * 降序
     *
     * @param field
     * @return
     */
    public static OrderInfo desc(String field) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.field = field;
        orderInfo.desc = true;
        return orderInfo;
    }

    /**
     * 升序
     *
     * @param field
     * @return
     */
    public static OrderInfo asc(Object field) {
        return asc(field.toString());
    }

    /**
     * 升序
     *
     * @param field
     * @return
     */
    public static OrderInfo asc(String field) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.field = field;
        orderInfo.desc = false;
        return orderInfo;
    }

    public static class OrderInfosBuilder {
        List<OrderInfo> orderInfos = new ArrayList<>();

        public OrderInfosBuilder asc(String field){
            orderInfos.add(OrderInfo.asc(field));
            return this;
        }

        public OrderInfosBuilder desc(String field){
            orderInfos.add(OrderInfo.desc(field));
            return this;
        }

        public OrderInfosBuilder asc(Object field){
            orderInfos.add(OrderInfo.desc(field));
            return this;
        }

        public OrderInfosBuilder desc(Object field){
            orderInfos.add(OrderInfo.desc(field));
            return this;
        }

        public Collection<OrderInfo> build() {
            return orderInfos;
        }
    }

    public static OrderInfosBuilder sortBuilder() {
        return new OrderInfosBuilder();
    }
}
