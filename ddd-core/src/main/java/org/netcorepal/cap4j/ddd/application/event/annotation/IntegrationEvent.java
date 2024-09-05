package org.netcorepal.cap4j.ddd.application.event.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author binking338
 * @date 2024/8/27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationEvent {
    public static final String NONE_SUBSCRIBER = "[none]";

    /**
     * 集成事件名称
     * 通常作为MQ topic名称
     *
     * @return
     */
    String value() default "";

    /**
     * 订阅者
     * 通常作为MQ consumer group名称
     *
     * @return
     */
    String subscriber() default NONE_SUBSCRIBER;
}
