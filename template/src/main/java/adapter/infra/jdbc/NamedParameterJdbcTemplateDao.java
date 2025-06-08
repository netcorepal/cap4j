package ${basePackage}.adapter.infra.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author cap4j-ddd-codegen
 */
@Slf4j
@Service
public class NamedParameterJdbcTemplateDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public NamedParameterJdbcTemplateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * 查询一个实体，数据异常返回0或多条记录将会抛出异常
     *
     * @param entityClass
     * @param sql
     * @param paramBeans
     * @param <E>
     * @return
     */
    public <E> E queryOne(Class<E> entityClass, String sql, Object... paramBeans) {
        Map<String, Object> params = resolveParamMap(paramBeans);
        E result = null;
        if (entityClass.isEnum()) {
            Integer val = this.namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
            result = (E) EnumConvertUtil.getEnumFromCode(entityClass, val);
        } else if (isPrimitiveType(entityClass)) {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, entityClass);
        } else {
            result = this.namedParameterJdbcTemplate.queryForObject(sql, params, generateRowMapper(entityClass));
        }
        return result;
    }

    /**
     * 查询第一条实体记录
     *
     * @param entityClass
     * @param sql
     * @param paramBeans
     * @param <E>
     * @return
     */
    public <E> Optional<E> queryFirst(Class<E> entityClass, String sql, Object... paramBeans) {
        Map<String, Object> params = resolveParamMap(paramBeans);
        Pattern limitPattern = Pattern.compile("\\s+LIMIT\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = limitPattern.matcher(sql);
        if (!matcher.find()) {
            if (sql.trim().endsWith(";")) {
                sql = sql.replaceFirst(";\\s*$", " LIMIT 1;");
            } else {
                sql += " LIMIT 1;";
            }
        }
        List<E> result = null;
        if (entityClass.isEnum()) {
            result = this.namedParameterJdbcTemplate.queryForList(sql, params, Integer.class)
                    .stream().map(i -> (E) EnumConvertUtil.getEnumFromCode(entityClass, i)).collect(Collectors.toList());
        } else if (isPrimitiveType(entityClass)) {
            result = this.namedParameterJdbcTemplate.queryForList(sql, params, entityClass);
        } else {
            result = this.namedParameterJdbcTemplate.query(sql, params, generateRowMapper(entityClass));
        }
        return result.stream().findFirst();
    }

    /**
     * 查询实体列表
     *
     * @param entityClass
     * @param sql
     * @param paramBeans
     * @param <E>
     * @return
     */
    public <E> List<E> queryList(Class<E> entityClass, String sql, Object... paramBeans) {
        Map<String, Object> params = resolveParamMap(paramBeans);
        List<E> result = null;
        if (entityClass.isEnum()) {
            result = this.namedParameterJdbcTemplate.queryForList(sql, params, Integer.class)
                    .stream().map(i -> (E) EnumConvertUtil.getEnumFromCode(entityClass, i)).collect(Collectors.toList());
        } else if (isPrimitiveType(entityClass)) {
            result = this.namedParameterJdbcTemplate.queryForList(sql, params, entityClass);
        } else {
            result = this.namedParameterJdbcTemplate.query(sql, params, generateRowMapper(entityClass));
        }
        return result;
    }

    /**
     * 解析参数
     *
     * @param paramBeans
     * @return
     */
    private Map<String, Object> resolveParamMap(Object... paramBeans) {
        HashMap<String, Object> params = new HashMap<>();
        for (Object paramBean : paramBeans) {
            convertToPropertiesMap(params, paramBean);
        }
        return params;
    }

    /**
     * 参数对象转Map
     *
     * @param resultMap
     * @param object
     * @param <T>
     * @return
     */
    private static <T> Map<String, Object> convertToPropertiesMap(Map<String, Object> resultMap, T object) {
        resultMap = resultMap == null
                ? new HashMap<>()
                : resultMap;

        if (object == null) {
            return resultMap;
        }
        if ((Map.class).isAssignableFrom(object.getClass())) {
            resultMap.putAll((Map<String, ?>) object);
        } else {
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);

            for (Field propertyDescriptor : wrapper.getWrappedClass().getDeclaredFields()) {
                String fieldName = propertyDescriptor.getName();

                try {
                    Object value = wrapper.getPropertyValue(fieldName);
                    value = EnumConvertUtil.processWithJpaConverter(propertyDescriptor, value);
                    value = EnumConvertUtil.processWithGetEnumCode(propertyDescriptor, value);
                    resultMap.put(fieldName, value);
                } catch (Exception e) {
                    // 处理获取属性值时发生的异常
                    log.error("命名参数转换异常 fieldName=" + fieldName, e);
                }
            }
        }

        return resultMap;
    }

    /**
     * 判断是否基础类型
     *
     * @param clazz
     * @return
     */
    private static boolean isPrimitiveType(Class clazz) {
        if (Long.class.equals(clazz)
                || Integer.class.equals(clazz)
                || Short.class.equals(clazz)
                || Byte.class.equals(clazz)
                || Float.class.equals(clazz)
                || Double.class.equals(clazz)
                || Boolean.class.equals(clazz)
                || String.class.equals(clazz)
                || BigDecimal.class.equals(clazz)
                || Date.class.equals(clazz)
                || LocalDateTime.class.equals(clazz)
                || LocalDate.class.equals(clazz)
                || LocalTime.class.equals(clazz)
        ) {
            return true;
        }
        return false;
    }


    private static Map<Class, BeanPropertyRowMapper> beanPropertyRowMapperMap = new HashMap<>();

    /**
     * 生成支持JpaConvert注解转化的RowMapper
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> BeanPropertyRowMapper<T> generateRowMapper(Class<T> clazz) {
        if (!beanPropertyRowMapperMap.containsKey(clazz)) {
            DefaultConversionService conversionService = new DefaultConversionService();
            BeanPropertyRowMapper beanPropertyRowMapper = BeanPropertyRowMapper.newInstance(clazz, conversionService);
            for (Field propertyDescriptor : clazz.getDeclaredFields()) {
                Convert convert = propertyDescriptor.getAnnotation(Convert.class);
                if (convert != null && !convert.disableConversion()) {
                    try {
                        AttributeConverter converter = (AttributeConverter) convert.converter().newInstance();
                        conversionService.addConverter(new JpaNumber2EnumConverter(Integer.class, propertyDescriptor.getType(), converter));
                        conversionService.addConverter(new JpaNumber2EnumConverter(Long.class, propertyDescriptor.getType(), converter));
                        conversionService.addConverter(new JpaNumber2EnumConverter(Short.class, propertyDescriptor.getType(), converter));
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else if (propertyDescriptor.getType().isEnum()) {
                    conversionService.addConverter(new GenericNumber2EnumConverter(Integer.class, propertyDescriptor.getType()));
                    conversionService.addConverter(new GenericNumber2EnumConverter(Long.class, propertyDescriptor.getType()));
                    conversionService.addConverter(new GenericNumber2EnumConverter(Short.class, propertyDescriptor.getType()));
                }
            }
            beanPropertyRowMapperMap.put(clazz, beanPropertyRowMapper);
        }
        return beanPropertyRowMapperMap.get(clazz);
    }

    /**
     * 枚举转化工具
     */
    private static class EnumConvertUtil {
        private static final String ENUM_PERSIST_FIELD_METHOD = "getCode";

        /**
         * 反射读取枚举数字编码
         *
         * @param e
         * @return
         */
        private static Object getEnumCode(Object e) {
            Object code = null;
            for (String fm : ENUM_PERSIST_FIELD_METHOD.split(",")) {
                try {
                    Method m = e.getClass().getMethod(fm);
                    if (m != null) {
                        code = m.invoke(e);
                        break;
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return code;
        }

        private static final Map<Class, Map<Object, Object>> CODE_ENUM_MAP_CACHE = new HashMap<>();

        /**
         * 数字编码转换成指定枚举类型
         *
         * @param enumClass
         * @param val
         * @return
         */
        private static Object getEnumFromCode(Class enumClass, Object val) {
            if (!CODE_ENUM_MAP_CACHE.containsKey(enumClass)) {
                Map<Object, Object> codeEnumMap = Arrays.stream(enumClass.getEnumConstants())
                        .collect(Collectors.toMap(e -> getEnumCode(e), e -> e));
                CODE_ENUM_MAP_CACHE.put(enumClass, codeEnumMap);
            }
            return CODE_ENUM_MAP_CACHE.get(enumClass).get(val);
        }

        /**
         * 尝试JpaConvert注解转换枚举数字编码
         *
         * @param propertyDescriptor
         * @param value
         * @return
         */
        private static Object processWithJpaConverter(Field propertyDescriptor, Object value) {
            Convert convert = propertyDescriptor.getAnnotation(Convert.class);
            if (convert != null && !convert.disableConversion()) {
                try {
                    AttributeConverter converter = null;
                    converter = (AttributeConverter) convert.converter().newInstance();
                    return converter.convertToDatabaseColumn(value);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return value;
        }

        /**
         * 尝试反射获取枚举数字编码
         *
         * @param propertyDescriptor
         * @param value
         * @return
         */
        private static Object processWithGetEnumCode(Field propertyDescriptor, Object value) {
            if (value != null && value.getClass().isEnum()) {
                return getEnumCode(value);
            }
            return value;
        }
    }

    /**
     * 基于JPA Convert注解的GenericConverter
     */
    private static class JpaNumber2EnumConverter implements GenericConverter {
        Set<ConvertiblePair> set = new HashSet<>();
        AttributeConverter attributeConverter;

        public JpaNumber2EnumConverter(Class sourceType, Class targetType, AttributeConverter converter) {
            set.add(new ConvertiblePair(sourceType, targetType));
            this.attributeConverter = converter;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return set;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return attributeConverter.convertToEntityAttribute(source);
        }
    }

    /**
     * 基于反射的GenericConverter
     */
    private static class GenericNumber2EnumConverter implements GenericConverter {

        Set<ConvertiblePair> set = new HashSet<>();

        public GenericNumber2EnumConverter(Class sourceType, Class targetType) {
            set.add(new ConvertiblePair(sourceType, targetType));
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return set;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return EnumConvertUtil.getEnumFromCode(targetType.getType(), source);
        }
    }

}
