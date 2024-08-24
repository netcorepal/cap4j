package org.netcorepal.cap4j.ddd.application;

/**
 * todo: 类描述
 *
 * @author binking338
 * @date 2024/8/24
 */
public class MediatorConfiguration {
    static Mediator instance = null;

    public static void configure(Mediator mediator)
    {
        instance = mediator;
    }
}
