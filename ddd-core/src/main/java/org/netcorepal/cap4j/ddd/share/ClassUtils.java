package org.netcorepal.cap4j.ddd.share;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 类型工具类
 *
 * @author binking338
 */
public class ClassUtils {

    /**
     * 查找方法
     * @param clazz 查找基于类型
     * @param name 方法名称
     * @param methodPredicate
     * @return
     */
    public static Method findMethod(Class clazz, String name, Predicate<Method> methodPredicate){
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (Objects.equals(method.getName(), name)) {
                if(methodPredicate!=null){
                    if (methodPredicate.test(method)){
                        return method;
                    }
                } else {
                    return method;
                }
            }
        }
        return null;
    }
}
