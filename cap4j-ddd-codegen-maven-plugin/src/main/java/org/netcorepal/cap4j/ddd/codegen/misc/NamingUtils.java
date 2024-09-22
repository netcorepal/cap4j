package org.netcorepal.cap4j.ddd.codegen.misc;

import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

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
     * @param someCase 带有下划线的字符串
     * @return 驼峰字符串
     */
    public static String toLowerCamelCase(String someCase) {
        if (someCase == null) {
            return null;
        }
        String camel = toUpperCamelCase(someCase);
        return camel.substring(0, 1).toLowerCase() + camel.substring(1);
//        if (someCase == null) {
//            return null;
//        }
//        // 分成数组
//        char[] charArray = someCase.toCharArray();
//        // 判断上次循环的字符是否是"_"
//        boolean underlineBefore = false;
//        StringBuffer buffer = new StringBuffer();
//        for (int i = 0, l = charArray.length; i < l; i++) {
//            if (i == 0 && Character.isUpperCase(charArray[0])) {
//                buffer.append(Character.toLowerCase(charArray[i]));
//            }
//            // 判断当前字符是否是"_",如果跳出本次循环
//            else if (charArray[i] == 95) {
//                underlineBefore = true;
//            } else if (underlineBefore) {
//                // 如果为true，代表上次的字符是"_",当前字符需要转成大写
//                buffer.append(charArray[i] -= 32);
//                underlineBefore = false;
//            } else {
//                // 不是"_"后的字符就直接追加
//                buffer.append(charArray[i]);
//            }
//        }
//        return buffer.toString();
    }

    /**
     * 下划线转大驼峰
     * user_name  ---->  UserName
     * userName   --->  UserName
     *
     * @param someCase 带有下划线的字符串
     * @return 驼峰字符串
     */
    public static String toUpperCamelCase(String someCase) {
        return String.join(
                "",
                Arrays.stream(someCase.split("(?=[A-Z])|[^a-zA-Z0-9]"))
                        .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))
                        .filter(s -> StringUtils.isNotBlank(s))
                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                        .collect(Collectors.toList())
        );
//        String camel = toLowerCamelCase(someCase);
//        return camel.substring(0, 1).toUpperCase() + camel.substring(1);
    }

    /**
     * 转蛇形风格命名(snake_case)
     *
     * @param someCase
     * @return
     */
    public static String toSnakeCase(String someCase) {
        if (someCase == null) {
            return null;
        }
        return String.join(
                "_",
                Arrays.stream(someCase.split("(?=[A-Z])|[^a-zA-Z0-9_]"))
                        .filter(s -> StringUtils.isNotBlank(s))
                        .map(s -> s.toLowerCase())
                        .collect(Collectors.toList())
        );
    }

    /**
     * 转土耳其烤肉风格命名(kebab-case)
     *
     * @param someCase
     * @return
     */
    public static String toKebabCase(String someCase) {
        if (someCase == null) {
            return someCase;
        }
        return String.join(
                "-",
                Arrays.stream(someCase.split("(?=[A-Z])|[^a-zA-Z0-9\\-]"))
                        .filter(s -> StringUtils.isNotBlank(s))
                        .map(s -> s.toLowerCase())
                        .collect(Collectors.toList())
        );
    }

    /**
     * 获取末位包名
     *
     * @param packageName
     * @return
     */
    public static String getLastPackageName(String packageName) {
        if (null == packageName || packageName.isEmpty()) {
            return "";
        }
        if (!packageName.contains(".")) {
            return packageName;
        }
        return packageName.substring(packageName.lastIndexOf(".") + 1);
    }

    /**
     * 获取父包名
     *
     * @param packageName
     * @return
     */
    public static String parentPackageName(String packageName) {
        String lastPackageName = getLastPackageName(packageName);
        if (packageName.length() == lastPackageName.length()) {
            return "";
        }
        return packageName.substring(0, packageName.length() - lastPackageName.length() - 1);
    }
}
