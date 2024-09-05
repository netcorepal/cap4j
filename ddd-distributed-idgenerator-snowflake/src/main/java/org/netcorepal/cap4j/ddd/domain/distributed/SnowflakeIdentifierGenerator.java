package org.netcorepal.cap4j.ddd.domain.distributed;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.netcorepal.cap4j.ddd.domain.distributed.snowflake.SnowflakeIdGenerator;

import java.io.Serializable;

/**
 * Snowflake Id生成器
 *
 * @author binking338
 * @date 2024/8/11
 */
public class SnowflakeIdentifierGenerator implements IdentifierGenerator {
    static SnowflakeIdGenerator snowflakeIdGeneratorImpl;

    public static void configure(SnowflakeIdGenerator snowflakeIdGenerator) {
        snowflakeIdGeneratorImpl = snowflakeIdGenerator;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return snowflakeIdGeneratorImpl.nextId();
    }
}
