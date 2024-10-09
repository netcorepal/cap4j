package org.netcorepal.cap4j.ddd.application.event.annotation;

import java.lang.annotation.*;

/**
 * 自动触发请求
 * 事件（领域\集成）-> 命令
 *
 * @author binking338
 * @date 2024/9/1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AutoRequests.class)
public @interface AutoRequest {
    /**
     * 目标请求
     *
     * @return
     */
    Class<?> targetRequestClass() default Void.class;

    /**
     * 事件 -> 请求 转换器
     * {@link org.springframework.core.convert.converter.Converter}
     *
     * @return {@link Class<? extends org.springframework.core.convert.converter.Converter>}
     */
    Class<?> converterClass() default Void.class;
}
