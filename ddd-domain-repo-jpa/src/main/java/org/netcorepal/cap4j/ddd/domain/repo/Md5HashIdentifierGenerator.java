package org.netcorepal.cap4j.ddd.domain.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    static Md5HashIdentifierGenerator instance = null;
    public static Md5HashIdentifierGenerator getInstance(){
        if(instance == null){
            instance = new Md5HashIdentifierGenerator();
        }
        return instance;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
        jsonObject.remove("id");
        String json = jsonObject.toString(SerializerFeature.SortField);
        String hash = DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        return hash;
    }
}
