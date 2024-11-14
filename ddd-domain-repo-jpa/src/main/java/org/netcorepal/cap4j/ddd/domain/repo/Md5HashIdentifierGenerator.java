package org.netcorepal.cap4j.ddd.domain.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.NumberUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
            if (null != hash && hash instanceof Serializable) {
                return (Serializable) hash;
            }
        }
        return hash(o, ID_FIELD_NAME);
    }

    /**
     * 返回对象MD5哈希值
     *
     * @param o
     * @param idFieldName ID字段名称
     * @return
     */
    public static Serializable hash(Object o, String idFieldName) {
        if (null == o) {
            return null;
        }
        JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
        recursionRemove(jsonObject, idFieldName);
        String json = jsonObject.toString(SerializerFeature.SortField);

        Class idFieldType = ClassUtils.resolveGenericTypeClass(o.getClass(), 0,
                ValueObject.class
        );
        if (idFieldType == String.class) {
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        }

        byte[] hashBytes = DigestUtils.md5Digest(json.getBytes(StandardCharsets.UTF_8));
        if (idFieldType == Integer.class) {
            return bytesToInteger(hashBytes);
        }
        if (idFieldType == Long.class) {
            return bytesToLong(hashBytes);
        }
        if (idFieldType == BigInteger.class) {
            return BigInteger.valueOf(bytesToLong(hashBytes));
        }
        if (idFieldType == BigDecimal.class) {
            return BigDecimal.valueOf(bytesToLong(hashBytes));
        }
        if (Number.class.isAssignableFrom(idFieldType)) {
            return NumberUtils.convertNumberToTargetClass(bytesToLong(hashBytes), idFieldType);
        }
        return bytesToLong(hashBytes);
    }

    private static void recursionRemove(Map<String, Object> obj, String fieldName) {
        obj.remove(fieldName);
        for (Map.Entry<String, Object> entry :
                obj.entrySet()) {
            if (null != entry.getValue() && entry.getValue() instanceof Map) {
                recursionRemove((Map<String, Object>) entry.getValue(), fieldName);
            }
        }
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
