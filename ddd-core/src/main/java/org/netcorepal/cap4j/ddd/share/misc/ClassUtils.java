package org.netcorepal.cap4j.ddd.share.misc;

import lombok.SneakyThrows;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.converter.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 类型工具类
 *
 * @author binking338
 */
public class ClassUtils {

    /**
     * 获取指定类或接口泛型参数类型
     *
     * @param clazz
     * @param typeArgumentIndex
     * @param superClasses
     * @return
     */
    public static Class<?> resolveGenericTypeClass(Class<?> clazz, int typeArgumentIndex, Class<?>... superClasses) {
        ParameterizedType parameterizedType = null;
        if (Arrays.stream(superClasses).anyMatch(
                superClass -> superClass.equals(ResolvableType.forType(clazz.getGenericSuperclass()).toClass()))
        ) {
            parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        } else {
            for (Type type : clazz.getGenericInterfaces()) {
                if (Arrays.stream(superClasses).anyMatch(
                        superClass -> superClass.equals(ResolvableType.forType(type).toClass()))
                ) {
                    parameterizedType = (ParameterizedType) type;
                }
            }
        }
        if (null == parameterizedType) {
            return Object.class;
        }
        return ResolvableType.forType(
                parameterizedType.getActualTypeArguments()[typeArgumentIndex]
        ).toClass();
    }

    /**
     * 查找方法
     *
     * @param clazz           查找基于类型
     * @param name            方法名称
     * @param methodPredicate
     * @return
     */
    public static Method findMethod(Class clazz, String name, Predicate<Method> methodPredicate) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (Objects.equals(method.getName(), name)) {
                if (methodPredicate != null) {
                    if (methodPredicate.test(method)) {
                        return method;
                    }
                } else {
                    return method;
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public static Converter<Object, Object> newConverterInstance(Class<?> srcClass, Class<?> descClass, Class<?> converterClass) {
        Converter<Object, Object> converter = null;
        try {
            converter = null == converterClass || Void.class.equals(converterClass)
                    ? null
                    : (Converter<Object, Object>) converterClass.newInstance();
        } catch (Exception e) {
            throw new DomainException("事件Converter无法实例化", e);
        }
        if (converter == null) {
            BeanCopier copier = BeanCopier.create(
                    srcClass,
                    descClass, false);
            converter = source -> {
                Object dest = null;
                try {
                    dest = descClass.newInstance();
                } catch (Exception e) {
                    throw new DomainException("无法完成事件自动转换", e);
                }
                copier.copy(source, dest, null);
                return dest;
            };
        }
        return converter;
    }
}
