package org.netcorepal.cap4j.ddd.share.misc;

import org.springframework.core.env.Environment;

import java.util.Random;
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


    private static final Random random = new Random(System.currentTimeMillis());
    private static final Character[] RANDOM_DICTIONARY = new Character[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * 生成随机字符串
     *
     * @param length     长度
     * @param hasDigital 包含数字
     * @param hasLetter  包含字母
     * @return
     */
    public static String randomString(int length, boolean hasDigital, boolean hasLetter) {
        return randomString(length, hasDigital, hasLetter, false, null);
    }

    /**
     * 生成随机字符串
     *
     * @param length             长度
     * @param hasDigital         包含数字
     * @param hasLetter          包含字母
     * @param mixLetterCase      混合大小写，不混合则只使用小写字母
     * @param externalDictionary 外部字典
     * @return
     */
    public static String randomString(int length, boolean hasDigital, boolean hasLetter, boolean mixLetterCase, Character[] externalDictionary) {
        int externalCapacity = externalDictionary != null
                ? externalDictionary.length
                : 0;
        int offset = 0, capacity = 0;
        if (hasDigital) {
            capacity += 10;
        } else {
            offset += 10;
        }
        if (hasLetter && mixLetterCase) {
            capacity += 52;
        } else if (hasLetter) {
            offset += 26;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int count = length;
        while (count-- > 0) {
            int index = random.nextInt(capacity + externalCapacity);
            if (index >= capacity) {
                stringBuilder.append(externalDictionary[index - capacity]);
            } else {
                stringBuilder.append(RANDOM_DICTIONARY[index + offset]);
            }
        }
        return stringBuilder.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.matches("^\\s*$");
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
