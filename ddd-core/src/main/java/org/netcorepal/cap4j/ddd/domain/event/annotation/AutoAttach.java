package org.netcorepal.cap4j.ddd.domain.event.annotation;

import org.netcorepal.cap4j.ddd.domain.repo.PersistType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动附加领域事件
 * 聚合根持久化变更 -> 领域事件
 *
 * @author binking338
 * @date 2024/8/29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoAttach {

    /**
     * 持久化变更源实体类型
     *
     * @return
     */
    Class<?> sourceEntityClass() default Void.class;

    /**
     * 持久化变更类型
     *
     * @return
     */
    PersistType[] persistType() default {
            PersistType.CREATE,
            PersistType.UPDATE,
            PersistType.DELETE
    };

    /**
     * 延迟发布（秒）
     * @return
     */
    int delayInSeconds() default 0;

    /**
     * 实体 -> 领域事件 转换器
     * {@link org.springframework.core.convert.converter.Converter}
     * @return {@link Class<? extends org.springframework.core.convert.converter.Converter>}
     */
    Class<?> converterClass() default Void.class;

}
