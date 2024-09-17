package org.netcorepal.cap4j.ddd.domain.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * md5
 *
 * @author binking338
 * @date 2024/9/17
 */
public class Md5HashIdentifierGenerator  implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        String json = JSON.toJSONString(o, SerializerFeature.SortField);
        String hash = DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        return hash;
    }
}
