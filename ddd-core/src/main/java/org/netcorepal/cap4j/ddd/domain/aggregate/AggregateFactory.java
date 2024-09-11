package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 聚合工厂
 *
 * @author binking338
 * @date 2024/9/3
 */
public interface AggregateFactory<ENTITY_PAYLOAD extends AggregatePayload, ENTITY> {

    /**
     * 创建新聚合实例
     *
     * @param entityPayload
     * @return
     */
    ENTITY create(ENTITY_PAYLOAD entityPayload);
}
