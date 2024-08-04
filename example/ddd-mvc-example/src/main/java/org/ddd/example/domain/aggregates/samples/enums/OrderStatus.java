package org.ddd.example.domain.aggregates.samples.enums;

import lombok.Getter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
public enum OrderStatus {

    /**
     * 待支付
     */
    INIT(0, "待支付"),
    /**
     * 已关闭
     */
    CLOSE(-1, "已关闭"),
    /**
     * 已完成
     */
    FINISH(1, "已完成"),
;
    @Getter
    private int code;
    @Getter
    private String name;

    OrderStatus(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    private static Map<Integer, OrderStatus> enums = null;
    public static OrderStatus valueOf(Integer code) {
        if(enums == null) {
            enums = new HashMap<>();
            for (OrderStatus val : OrderStatus.values()) {
                enums.put(val.code, val);
            }
        }
        if(enums.containsKey(code)){
            return enums.get(code);
        }
        throw new RuntimeException("枚举类型OrderStatus枚举值转换异常，不存在的值" + code);
    }

    /**
     * JPA转换器
     */
    public static class Converter implements AttributeConverter<OrderStatus, Integer>{
        @Override
        public Integer convertToDatabaseColumn(OrderStatus  val) {
            return val.code;
        }

        @Override
        public OrderStatus convertToEntityAttribute(Integer code) {
            return OrderStatus.valueOf(code);
        }
    }
}

