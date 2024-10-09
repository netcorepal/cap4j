package org.netcorepal.cap4j.ddd;

import org.springframework.context.ApplicationContext;

/**
 * 终结者配置
 *
 * @author binking338
 * @date 2024/8/24
 */
public class MediatorSupport {
    static Mediator instance = null;

    static ApplicationContext ioc = null;

    public static void configure(Mediator mediator) {
        instance = mediator;
    }

    public static void configure(ApplicationContext applicationContext) {
        ioc = applicationContext;
    }
}
