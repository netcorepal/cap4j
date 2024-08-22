package org.netcorepal.cap4j.ddd.domain.event.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 领域事件
 *
 * @author binking338
 * @date
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEvent {
    public static final String NONE_SUBSCRIBER = "[none]";

    /**
     * 集成事件名称
     * @return
     */
    @AliasFor("intergration")
    String value() default "";

    /**
     * 集成事件名称
     * 该字段非空即为集成事件
     * （通常作为MQ topic名称）
     * @return
     */
    @AliasFor("value")
    String intergration() default "";

    /**
     * 订阅者
     * （通常作为MQ consumer group名称）
     * @return
     */
    String subscriber() default NONE_SUBSCRIBER;

    /**
     * 事件记录是否持久化
     * 普通领域事件选择记录持久化，则事件发送失败将会有重试机制。
     * 集成事件必然持久化，该字段值无效。
     * @return
     */
    boolean persist() default false;
}
