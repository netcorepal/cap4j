package org.netcorepal.cap4j.ddd.application.event.annotation;

import java.lang.annotation.*;

/**
 * @author binking338
 * @date 2024/9/11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRequests {
    AutoRequest[] value();
}
