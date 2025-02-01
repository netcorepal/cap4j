package org.netcorepal.cap4j.ddd.share.misc;

import lombok.SneakyThrows;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.converter.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 类型工具类
 *
 * @author binking338
 */
public class ClassUtils {

    /**
     * 获取指定类或接口泛型参数类型
     *
     * @param obj
     * @param typeArgumentIndex
     * @param superClasses
     * @return
     */
    public static Class<?> resolveGenericTypeClass(Object obj, int typeArgumentIndex, Class<?>... superClasses) {
        Class<?> clazz = AopUtils.getTargetClass(obj);
        return resolveGenericTypeClass(clazz, typeArgumentIndex, superClasses);
    }

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

    /**
     * 获取 Converter对象
     *
     * @param srcClass       源类型
     * @param destClass      模板类型
     * @param converterClass 转换类
     * @return
     */
    @SneakyThrows
    public static Converter<Object, Object> newConverterInstance(Class<?> srcClass, Class<?> destClass, Class<?> converterClass) {
        Converter<?, ?> converter = null;
        try {
            converter = null == converterClass || Void.class.equals(converterClass)
                    ? null
                    : (Converter<?, ?>) converterClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("事件Converter无法实例化", e);
        }

        Method method = findMethod(
                destClass,
                "convert",
                m -> m.getParameterCount() == 1
                        && srcClass.isAssignableFrom(m.getParameterTypes()[0])
                        && destClass.isAssignableFrom(m.getReturnType())
        );
        if (null != method) {
            return (src) -> {
                Object dest = null;
                try {
                    dest = method.invoke(destClass.newInstance(), src);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return dest;
            };
        }

        if (converter == null) {
            BeanCopier copier = BeanCopier.create(
                    srcClass,
                    destClass, false);
            converter = source -> {
                Object dest = null;
                try {
                    dest = destClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("无法完成事件自动转换", e);
                }
                copier.copy(source, dest, null);
                return dest;
            };
        }
        return (Converter<Object, Object>) converter;
    }
}
