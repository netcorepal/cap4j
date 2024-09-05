package org.netcorepal.cap4j.ddd.domain.event.annotation;

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

    /**
     * 领域事件名称
     * @return
     */
    String value() default "";

    /**
     * 事件记录是否持久化
     * @return
     */
    boolean persist() default false;
}
