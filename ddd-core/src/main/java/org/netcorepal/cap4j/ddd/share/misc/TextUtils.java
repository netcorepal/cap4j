package org.netcorepal.cap4j.ddd.share.misc;

import org.springframework.core.env.Environment;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本辅助
 *
 * @author binking338
 * @date 2024/8/10
 */
public class TextUtils {

    private static ConcurrentHashMap<String, String> resolvePlaceholderCache = new ConcurrentHashMap();

    /**
     * @param origin
     * @param environment
     * @return
     */
    public static String resolvePlaceholderWithCache(String origin, Environment environment) {
        String result = resolvePlaceholderCache.computeIfAbsent(origin, (t) -> environment.resolvePlaceholders(t));
        return result;
    }
}
