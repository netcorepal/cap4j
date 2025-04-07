package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 聚合工厂管理器
 *
 * @author binking338
 * @date 2024/9/3
 */
public interface AggregateFactorySupervisor {
    public static AggregateFactorySupervisor getInstance() {
        return AggregateFactorySupervisorSupport.instance;
    }

    /**
     * 创建新聚合实例
     *
     * @param entityPayload
     * @return
     * @param <ENTITY_PAYLOAD>
     * @param <ENTITY>
     */
    <ENTITY_PAYLOAD extends AggregatePayload<ENTITY>, ENTITY> ENTITY create(ENTITY_PAYLOAD entityPayload);

}
