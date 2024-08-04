package org.ddd.domain.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 领域事件
 *
 * @author <template/>
 * @date
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEvent {
    public static final String NONE_SUBSCRIBER = "[none]";

    /**
     * 领域事件名称
     * 集成事件需要定义领域事件名称（通常作为MQ topic名称）
     *
     * @return
     */
    String value() default "";

    /**
     * 订阅者
     * （通常作为MQ consumer group名称）
     * @return
     */
    String subscriber() default NONE_SUBSCRIBER;

    /**
     * 事件记录持久化
     * 如果持久化，则事件发送失败或消费失败将会有重试机制
     *
     * @return
     */
    boolean persist() default false;
}
