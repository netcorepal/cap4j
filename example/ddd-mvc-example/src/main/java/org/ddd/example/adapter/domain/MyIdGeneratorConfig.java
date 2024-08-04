package org.ddd.example.adapter.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author binking338
 * @date 2024/4/14
 */
public class MyIdGeneratorConfig implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }
}
