package org.netcorepal.cap4j.ddd.application.distributed;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.netcorepal.cap4j.ddd.application.distributed.snowflake.SnowflakeIdGenerator;

import java.io.Serializable;

/**
 * Snowflake Id生成器
 *
 * @author binking338
 * @date 2024/8/11
 */
public class SnowflakeIdentifierGenerator implements IdentifierGenerator {
    static SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return snowflakeIdGenerator.nextId();
    }
}
