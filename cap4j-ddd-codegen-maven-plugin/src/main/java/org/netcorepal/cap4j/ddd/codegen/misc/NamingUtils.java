package org.netcorepal.cap4j.ddd.codegen.misc;

/**
 * @author binking338
 * @date 2022-02-17
 */
public class NamingUtils {
    /**
     * 下划线转小驼峰
     * user_name  ---->  userName
     * userName   --->  userName
     *
     * @param underlineStr 带有下划线的字符串
     * @return 驼峰字符串
     */
    public static String toLowerCamelCase(String underlineStr) {
        if (underlineStr == null) {
            return null;
        }
        // 分成数组
        char[] charArray = underlineStr.toCharArray();
        // 判断上次循环的字符是否是"_"
        boolean underlineBefore = false;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, l = charArray.length; i < l; i++) {
            if (i == 0 && Character.isUpperCase(charArray[0])) {
                buffer.append(Character.toLowerCase(charArray[i]));
            }
            // 判断当前字符是否是"_",如果跳出本次循环
            else if (charArray[i] == 95) {
                underlineBefore = true;
            } else if (underlineBefore) {
                // 如果为true，代表上次的字符是"_",当前字符需要转成大写
                buffer.append(charArray[i] -= 32);
                underlineBefore = false;
            } else {
                // 不是"_"后的字符就直接追加
                buffer.append(charArray[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * 下划线转大驼峰
     * user_name  ---->  UserName
     * userName   --->  UserName
     *
     * @param underlineStr 带有下划线的字符串
     * @return 驼峰字符串
     */
    public static String toUpperCamelCase(String underlineStr) {
        String camel = toLowerCamelCase(underlineStr);
        return camel.substring(0, 1).toUpperCase() + camel.substring(1);
    }
}
