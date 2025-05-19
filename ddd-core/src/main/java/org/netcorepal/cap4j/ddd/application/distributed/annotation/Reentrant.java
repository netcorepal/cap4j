package org.netcorepal.cap4j.ddd.application.distributed.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可重入锁
 *
 * @author binking338
 * @date 2025/5/14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reentrant {
    /**
     * 是否可重入
     * @return
     */
    boolean value() default false;

    /**
     * 唯一识别码，默认使用方法签名
     * @return
     */
    String key() default "";

    /**
     * 是否分布式
     * @return
     */
    boolean distributed() default false;

    /**
     * 锁过期时间
     * 支持 ms, s, m, h, d 为单位（不区分大小写）
     * 支持 {@link java.time.Duration}.parse
     *
     * @return
     */
    String expire() default "6h";
}
