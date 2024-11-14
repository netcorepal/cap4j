package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 值对象
 *
 * @author binking338
 * @date 2024/9/18
 */
public interface ValueObject<ID> {
    /**
     * 值对象哈希码
     *
     * @return
     */
    default ID hash() {
        return null;
    }
}
