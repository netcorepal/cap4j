package org.netcorepal.cap4j.ddd.domain.aggregate.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合信息
 * @author binking338
 * @date 2024/8/23
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {
    /**
     * 实体名称
     * @return
     */
    @AliasFor("entity")
    String value() default "";

    /**
     * 实体名称
     * @return
     */
    @AliasFor("value")
    String entity() default "";

    /**
     * 是否聚合根
     * @return
     */
    boolean root() default false;

    /**
     * 实体描述
     * @return
     */
    String description() default "";

    /**
     * 所属聚合
     * @return
     */
    String aggregate() default "";

    /**
     * 归属实体名称
     * @return
     */
    String parentEntity() default "";
}
