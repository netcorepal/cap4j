package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 聚合管理器帮助类
 *
 * @author binking338
 * @date 2025/1/12
 */
public class AggregateSupervisorSupport {
    static AggregateSupervisor instance;
    public static void configure(AggregateSupervisor aggregateSupervisor){
        instance = aggregateSupervisor;
    }
}
