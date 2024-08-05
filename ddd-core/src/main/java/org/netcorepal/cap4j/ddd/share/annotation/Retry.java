package org.netcorepal.cap4j.ddd.share.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试注解
 *
 * @author binking338
 * @date 2023/8/28
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    /**
     * 重试次数
     * 只有集成事件重试次数才有意义
     *
     * @return
     */
    int retryTimes() default 15;

    /**
     * 重试时间间隔，单位分钟
     * @return
     */
    int[] retryIntervals() default {};

    /**
     * 过期时长，单位分钟，默认一天
     * @return
     */
    int expireAfter() default 1440;
}
