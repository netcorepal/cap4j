package org.netcorepal.cap4j.ddd.codegen.misc;

/**
 * 文本工具
 *
 * @author binking338
 * @date 2024/9/8
 */
public class TextUtils {
    public static String[] splitWithTrim(String val, String regexSplitter){
        return splitWithTrim(val, regexSplitter, 0);
    }

    /**
     * 字符串分割
     * @param val
     * @param regexSplitter
     * @param limit
     * @return
     */
    public static String[] splitWithTrim(String val, String regexSplitter, int limit){
        if(null == val || val.isEmpty()){
            return new String[0];
        }
        String[] segments = val.split(regexSplitter, limit);
        for (int i = 0; i < segments.length; i++) {
            segments[i] = segments[i].trim();
        }
        return segments;
    }
}
