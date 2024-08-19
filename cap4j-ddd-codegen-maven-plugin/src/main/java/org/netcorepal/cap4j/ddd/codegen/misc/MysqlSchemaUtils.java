package org.netcorepal.cap4j.ddd.codegen.misc;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author binking338
 * @date 2022-03-06
 */
public class MysqlSchemaUtils {
    public static Mojo mojo;

    static Pattern ANNOTATION_PATTERN = Pattern.compile("@([A-Za-z]+)(\\=[^;]+)?;?");

    private static Log getLog() {
        return mojo.getLog();
    }


    public static boolean hasColumn(String columnName, List<Map<String, Object>> columns) {
        return columns.stream().anyMatch(col -> col.get("COLUMN_NAME").toString().equalsIgnoreCase(columnName));
    }

    public static String getName(Map<String, Object> tableOrColumn) {
        return tableOrColumn.containsKey("COLUMN_NAME") ? getColumnName(tableOrColumn) : getTableName(tableOrColumn);
    }

    public static String getColumnName(Map<String, Object> column) {
        return column.get("COLUMN_NAME").toString();
    }

    public static String getTableName(Map<String, Object> tableOrColumn) {
        return tableOrColumn.get("TABLE_NAME").toString();
    }

    public static String getColumnDbType(Map<String, Object> column) {
        return column.get("COLUMN_TYPE").toString();
    }

    public static boolean isColumnNullable(Map<String, Object> column) {
        return "YES".equalsIgnoreCase(column.get("IS_NULLABLE").toString());
    }

    public static String getComment(Map<String, Object> tableOrColumn, boolean cleanAnnotations) {
        String comment = "";
        if (tableOrColumn.containsKey("TABLE_COMMENT")) {
            comment = tableOrColumn.get("TABLE_COMMENT") == null ? "" : tableOrColumn.get("TABLE_COMMENT").toString();
        } else if (tableOrColumn.containsKey("COLUMN_COMMENT")) {
            comment = tableOrColumn.get("COLUMN_COMMENT") == null ? "" : tableOrColumn.get("COLUMN_COMMENT").toString();
        }

        if (cleanAnnotations) {
            comment = ANNOTATION_PATTERN.matcher(comment).replaceAll("");
        }
        return comment;
    }

    public static String getComment(Map<String, Object> tableOrColumn) {
        return getComment(tableOrColumn, true);
    }

    /**
     * 注解缓存，注释为Key
     */
    static Map<String, Map<String, String>> AnnotaionsCache = new HashMap<>();

    public static Map<String, String> getAnnotations(Map<String, Object> tableOrColumn) {
        String comment = getComment(tableOrColumn, false);
        if (AnnotaionsCache.containsKey(comment)) {
            return AnnotaionsCache.get(comment);
        }
        HashMap<String, String> annotations = new HashMap<>();
        Matcher matcher = ANNOTATION_PATTERN.matcher(comment);
        while (matcher.find()) {
            if (matcher.groupCount() > 1 && StringUtils.isNotBlank(matcher.group(1))) {
                String key = matcher.group(1);
                String val = matcher.group(2);
                if (StringUtils.isNotBlank(val) && val.length() > 0) {
                    annotations.put(key, val.substring(1));
                    getLog().debug("找到注解:" + getTableName(tableOrColumn) + " @" + key + "=" + val.substring(1) + ";");
                } else {
                    annotations.put(key, "");
                    getLog().debug("找到注解:" + getTableName(tableOrColumn) + " @" + key + ";");
                }
            }
        }
        AnnotaionsCache.putIfAbsent(comment, annotations);
        return annotations;
    }

    public static boolean hasAnnotation(Map<String, Object> tableOrColumn, String annotation) {
        return getAnnotations(tableOrColumn).containsKey(annotation);
    }

    public static String getAnnotation(Map<String, Object> tableOrColumn, String annotation) {
        return getAnnotations(tableOrColumn).getOrDefault(annotation, "");
    }

    public static String getAnyAnnotation(Map<String, Object> tableOrColumn, List<String> annotations) {
        for (String annotaion :
                annotations) {
            if (hasAnnotation(tableOrColumn, annotaion)) {
                return getAnnotation(tableOrColumn, annotaion);
            }
        }
        return "";
    }

    public static boolean hasAnyAnnotation(Map<String, Object> tableOrColumn, List<String> annotations) {
        for (String annotaion :
                annotations) {
            if (hasAnnotation(tableOrColumn, annotaion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasLazy(Map<String, Object> table) {
        return hasAnyAnnotation(table, Arrays.asList("Lazy", "L"));
    }

    public static boolean isLazy(Map<String, Object> table){
        return isLazy(table, false);
    }

    public static boolean isLazy(Map<String, Object> table, boolean defaultLazy) {
        String val = getAnyAnnotation(table, Arrays.asList("Lazy", "L"));
        if(defaultLazy) {
            return StringUtils.compareIgnoreCase(val, "false") == 0 || StringUtils.compareIgnoreCase(val, "0") == 0 ? false : true;
        } else {
            return StringUtils.compareIgnoreCase(val, "true") == 0 || StringUtils.compareIgnoreCase(val, "1") == 0 ? true : false;
        }
    }

    public static boolean countIsOne(Map<String, Object> table) {
        String val = getAnyAnnotation(table, Arrays.asList("Count", "C"));
        return StringUtils.compareIgnoreCase(val, "One") == 0 || StringUtils.compareIgnoreCase(val, "1") == 0 ? true : false;
    }

    /**
     * 是否需要忽略
     *
     * @param tableOrColumn
     * @return
     */
    public static boolean isIgnore(Map<String, Object> tableOrColumn) {
        return hasAnyAnnotation(tableOrColumn, Arrays.asList("Ignore", "I"));
    }

    /**
     * 是否聚合根
     *
     * @param table
     * @return
     */
    public static boolean isAggregateRoot(Map<String, Object> table) {
        return hasAnyAnnotation(table, Arrays.asList("AggregateRoot", "Root", "R"))
                || !hasAnyAnnotation(table, Arrays.asList("Parent", "P"));
    }

    /**
     * 获取聚合关系中的父表
     *
     * @param table
     * @return
     */
    public static String getParent(Map<String, Object> table) {
        return getAnyAnnotation(table, Arrays.asList("Parent", "P"));
    }

    /**
     * 获取模块
     *
     * @param table
     * @return
     */
    public static String getModule(Map<String, Object> table) {
        String module = getAnyAnnotation(table, Arrays.asList("Module", "M"));
        return module;
    }

    /**
     * 获取聚合
     *
     * @param table
     * @return
     */
    public static String getAggregate(Map<String, Object> table) {
        String aggregate = getAnyAnnotation(table, Arrays.asList("Aggregate", "A"));
        return aggregate;
    }

    /**
     * 字段是否忽略插入
     *
     * @param column
     * @return
     */
    public static boolean hasIgnoreInsert(Map<String, Object> column) {
        return hasAnyAnnotation(column, Arrays.asList("IgnoreInsert", "II"));
    }

    /**
     * 字段是否忽略更新
     *
     * @param column
     * @return
     */
    public static boolean hasIgnoreUpdate(Map<String, Object> column) {
        return hasAnyAnnotation(column, Arrays.asList("IgnoreUpdate", "IU"));
    }

    /**
     * 字段是否忽略更新
     *
     * @param column
     * @return
     */
    public static boolean hasReadOnly(Map<String, Object> column) {
        return hasAnyAnnotation(column, Arrays.asList("ReadOnly", "RO"));
    }

    /**
     * 是否关系
     *
     * @param columnOrTable
     * @return
     */
    public static boolean hasRelation(Map<String, Object> columnOrTable) {
        return hasAnyAnnotation(columnOrTable, Arrays.asList("Relation", "Rel"));
    }

    /**
     * 获取关系
     * OneToManny
     * ManyToOne
     * ManyToMany
     *
     * @param columnOrTable
     * @return
     */
    public static String getRelation(Map<String, Object> columnOrTable) {
        return getAnyAnnotation(columnOrTable, Arrays.asList("Relation", "Rel"));
    }

    /**
     * 是否引用
     *
     * @param column
     * @return
     */
    public static boolean hasReference(Map<String, Object> column) {
        return hasAnyAnnotation(column, Arrays.asList("Reference", "Ref"));
    }

    /**
     * 获取引用，会以 {table}_id 尝试推断
     *
     * @param column
     * @return
     */
    public static String getReference(Map<String, Object> column) {
        String ref = getAnyAnnotation(column, Arrays.asList("Reference", "Ref"));
        String columnName = getColumnName(column).toLowerCase();
        if (StringUtils.isBlank(ref) && columnName.endsWith("_id")) {
            ref = columnName.replaceAll("_id$", "");
        }
        if (StringUtils.isBlank(ref)) {
            return columnName;
        }
        return ref;
    }

    public static boolean hasIdGenerator(Map<String, Object> table) {
        return hasAnyAnnotation(table, Arrays.asList("IdGenerator", "IG"));
    }

    public static String getIdGenerator(Map<String, Object> table) {
        String idGenerator = getAnyAnnotation(table, Arrays.asList("IdGenerator", "IG"));
        return idGenerator;
    }

    /**
     * 是否有类型注解
     *
     * @param columnOrTable
     * @return
     */
    public static boolean hasType(Map<String, Object> columnOrTable) {
        return hasAnyAnnotation(columnOrTable, Arrays.asList("Type", "T"));
    }

    /**
     * 获取类型注解
     *
     * @param columnOrTable
     * @return
     */
    public static String getType(Map<String, Object> columnOrTable) {
        return getAnyAnnotation(columnOrTable, Arrays.asList("Type", "T"));
    }

    /**
     * 是否枚举字段
     *
     * @param columnOrTable
     * @return
     */
    public static boolean hasEnum(Map<String, Object> columnOrTable) {
        return hasType(columnOrTable) && hasAnyAnnotation(columnOrTable, Arrays.asList("Enum", "E"));
    }

    /**
     * 获取枚举设置
     *
     * @param column
     * @return
     * @example \@Enum=0:NONE:无|1:MALE:男|2:FEMALE:女
     */
    public static Map<Integer, String[]> getEnum(Map<String, Object> column) {
        String enumsConfig = getAnyAnnotation(column, Arrays.asList("Enum", "E"));
        Map<Integer, String[]> result = new HashMap<>();
        if (StringUtils.isNotBlank(enumsConfig)) {
            String[] enumConfigs = enumsConfig.split("\\|");
            for (int i = 0; i < enumConfigs.length; i++) {
                String enumConfig = enumConfigs[i];
                getLog().debug(enumConfig);
                List<String> pair = Arrays.stream(enumConfig.split("\\:"))
                        .map(c -> c.trim()
                                .replace("\n","")
                                .replace("\r","")
                                .replace("\t",""))
                        .collect(Collectors.toList());
                if (pair.size() == 0) {
                    continue;
                } else if (pair.size() == 1) {
                    if (pair.get(0).matches("^[-+]?[0-9]+$")) {
                        continue;
                    } else {
                        result.put(i, new String[]{pair.get(0), pair.get(0)});
                    }
                } else if (pair.size() == 2) {
                    if (pair.get(0).matches("^[-+]?[0-9]+$")) {
                        result.put(Integer.parseInt(pair.get(0)), new String[]{pair.get(1), pair.get(1)});
                    } else {
                        result.put(i, new String[]{pair.get(0), pair.get(1)});
                    }
                } else {
                    if (pair.get(0).matches("^[-+]?[0-9]+$")) {
                        result.put(Integer.parseInt(pair.get(0)), new String[]{pair.get(1), pair.get(2)});
                    } else {
                        result.put(i, new String[]{pair.get(0), pair.get(1)});
                    }
                }
            }
        }
        return result;
    }
}
