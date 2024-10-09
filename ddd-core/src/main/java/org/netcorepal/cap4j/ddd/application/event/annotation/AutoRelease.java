package org.netcorepal.cap4j.ddd.application.event.annotation;

import java.lang.annotation.*;

/**
 * 自动发布
 * 领域事件 -> 集成事件
 *
 * @author binking338
 * @date 2024/8/29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AutoReleases.class)
public @interface AutoRelease {

    /**
     * 源领域事件类型
     *
     * @return
     */
    Class<?> sourceDomainEventClass() default Void.class;

    /**
     * 延迟发布（秒）
     *
     * @return
     */
    int delayInSeconds() default 0;

    /**
     * 领域事件 -> 集成事件 转换器
     * {@link org.springframework.core.convert.converter.Converter}
     *
     * @return {@link Class<? extends org.springframework.core.convert.converter.Converter>}
     */
    Class<?> converterClass() default Void.class;
}
