package org.netcorepal.cap4j.ddd.domain.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * md5
 *
 * @author binking338
 * @date 2024/9/17
 */
public class Md5HashIdentifierGenerator implements IdentifierGenerator {
    static final String ID_FIELD_NAME = "id";
    static Md5HashIdentifierGenerator instance = null;

    public static Md5HashIdentifierGenerator getInstance() {
        if (instance == null) {
            instance = new Md5HashIdentifierGenerator();
        }
        return instance;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        if (o instanceof ValueObject) {
            Object hash = ((ValueObject) o).hash();
            if (null != hash) {
                if (hash instanceof Number) {
                    return (Number) hash;
                }
                return hash.toString();
            }
        }

        Class idFieldType = null;
        try {
            idFieldType = o.getClass().getField(ID_FIELD_NAME).getType();
        } catch (NoSuchFieldException e) {
            /* don't care */
        }
        return hash(o, ID_FIELD_NAME, idFieldType);
    }

    /**
     * 返回对象MD5哈希值
     *
     * @param o
     * @param idFieldName ID字段名称
     * @param idFieldType ID字段类型
     * @return
     */
    public static <ID_TYPE extends Serializable> ID_TYPE hash(Object o, String idFieldName, Class<ID_TYPE> idFieldType) {
        if (null == o) {
            return null;
        }
        // todo 解决内嵌ValueObject的id移除
        JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
        jsonObject.remove(idFieldName);
        String json = jsonObject.toString(SerializerFeature.SortField);

        if (idFieldType == String.class) {
            return (ID_TYPE) DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        }

        byte[] hashBytes = DigestUtils.md5Digest(json.getBytes(StandardCharsets.UTF_8));
        if (idFieldType == Integer.class) {
            return (ID_TYPE) bytesToInteger(hashBytes);
        }
        if (idFieldType == Long.class) {
            return (ID_TYPE) bytesToLong(hashBytes);
        }
        if(idFieldType == BigInteger.class){
            return (ID_TYPE) BigInteger.valueOf(bytesToLong(hashBytes));
        }
        if(idFieldType == BigDecimal.class){
            return (ID_TYPE) BigDecimal.valueOf(bytesToLong(hashBytes));
        }
        return (ID_TYPE) bytesToLong(hashBytes);
    }

    private static Long bytesToLong(byte[] b) {
        long res = 0;
        for (int i = 0; i < 8 && i < b.length; i++) {
            res |= (long) (b[i] & 0xff) << (8 * i);
        }
        return res;
    }

    private static Integer bytesToInteger(byte[] b) {
        int res = 0;
        for (int i = 0; i < 4 && i < b.length; i++) {
            res |= (b[i] & 0xff) << (8 * i);
        }
        return res;
    }
}
