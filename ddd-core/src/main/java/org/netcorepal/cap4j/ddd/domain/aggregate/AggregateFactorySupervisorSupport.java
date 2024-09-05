package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 聚合工厂管理器配置
 *
 * @author binking338
 * @date 2024/9/3
 */
public class AggregateFactorySupervisorSupport {
    static AggregateFactorySupervisor instance;

    public static void configure(AggregateFactorySupervisor aggregateFactorySupervisor) {
        instance = aggregateFactorySupervisor;
    }
}
