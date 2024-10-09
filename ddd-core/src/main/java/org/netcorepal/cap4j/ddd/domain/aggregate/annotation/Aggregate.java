package org.netcorepal.cap4j.ddd.domain.aggregate.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合信息
 *
 * @author binking338
 * @date 2024/8/23
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {
    public static final String TYPE_ENTITY = "entity";
    public static final String TYPE_VALUE_OBJECT = "value-object";
    public static final String TYPE_ENUM = "enum";
    public static final String TYPE_REPOSITORY = "repository";
    public static final String TYPE_DOMAIN_EVENT = "domain-event";
    public static final String TYPE_FACTORY = "factory";
    public static final String TYPE_FACTORY_PAYLOAD = "factory-payload";
    public static final String TYPE_SPECIFICATION = "specification";

    /**
     * 所属聚合
     *
     * @return
     */
    String aggregate() default "";

    /**
     * 元素名称
     *
     * @return
     */
    String name() default "";

    /**
     * 是否聚合根
     * @return
     */
    boolean root() default false;

    /**
     * 元素类型
     * entity、value-object、repository、factory、factory-payload、domain-event、specification、enum
     *
     * @return
     */
    String type() default "";

    /**
     * 实体描述
     *
     * @return
     */
    String description() default "";

    /**
     * 关联元素名称
     *
     * @return
     */
    String[] relevant() default {};
}
