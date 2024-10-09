package org.netcorepal.cap4j.ddd.domain.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 领域服务注解
 *
 * @author binking338
 * @date 2024/9/3
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainService {
    /**
     * 领域服务名称
     *
     * @return
     */
    String name() default "";
}
