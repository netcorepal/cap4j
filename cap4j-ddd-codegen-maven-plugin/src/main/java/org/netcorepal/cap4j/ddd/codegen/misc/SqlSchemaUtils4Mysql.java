package org.netcorepal.cap4j.ddd.codegen.misc;

import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.SqlSchemaUtils.*;

/**
 * mysql 数据库schema工具
 *
 * @author binking338
 * @date 2024/9/5
 */
public class SqlSchemaUtils4Mysql {

    public static List<Map<String, Object>> resolveTables(String connectionString, String user, String pwd){
        String tableSql = "select * from " + LEFT_QUOTES_4_ID_ALIAS + "information_schema" + RIGHT_QUOTES_4_ID_ALIAS + "." + LEFT_QUOTES_4_ID_ALIAS + "tables" + RIGHT_QUOTES_4_ID_ALIAS + " where table_schema= " + LEFT_QUOTES_4_LITERAL_STRING + mojo.schema + RIGHT_QUOTES_4_LITERAL_STRING;
        if (StringUtils.isNotBlank(mojo.table)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.table.split(mojo.PATTERN_SPLITTER)).map(t -> "table_name like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            tableSql += " and (" + whereClause + ")";
        }
        if (StringUtils.isNotBlank(mojo.ignoreTable)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.ignoreTable.split(mojo.PATTERN_SPLITTER)).map(t -> "table_name like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            tableSql += " and not (" + whereClause + ")";
        }
        return executeQuery(tableSql, connectionString, user, pwd);
    }
    public static List<Map<String, Object>> resolveColumns(String connectionString, String user, String pwd){
        String columnSql = "select * from " + LEFT_QUOTES_4_ID_ALIAS + "information_schema" + RIGHT_QUOTES_4_ID_ALIAS + "." + LEFT_QUOTES_4_ID_ALIAS + "columns" + RIGHT_QUOTES_4_ID_ALIAS + " where table_schema= " + LEFT_QUOTES_4_LITERAL_STRING + mojo.schema + RIGHT_QUOTES_4_LITERAL_STRING;
        if (StringUtils.isNotBlank(mojo.table)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.table.split(mojo.PATTERN_SPLITTER)).map(t -> "table_name like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            columnSql += " and (" + whereClause + ")";
        }
        if (StringUtils.isNotBlank(mojo.ignoreTable)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.ignoreTable.split(mojo.PATTERN_SPLITTER)).map(t -> "table_name like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            columnSql += " and not (" + whereClause + ")";
        }
        return executeQuery(columnSql, connectionString, user, pwd);
    }

    /**
     * 获取列的Java映射类型(Mysql)
     *
     * @param column
     * @return
     */
    public static String getColumnJavaType(Map<String, Object> column) {
        String dataType = column.get("DATA_TYPE").toString().toLowerCase();
        String columnType = column.get("COLUMN_TYPE").toString().toLowerCase();
        String comment = SqlSchemaUtils.getComment(column);
        String columnName = SqlSchemaUtils.getColumnName(column).toLowerCase();
        if (mojo.typeRemapping != null && mojo.typeRemapping.containsKey(dataType)) {
            // 类型重映射
            return mojo.typeRemapping.get(dataType);
        }
        switch (dataType) {
            case "varchar":
            case "text":
            case "mediumtext":
            case "longtext":
            case "char":
                return "String";
            case "timestamp":
            case "datetime":
                if ("java.time".equalsIgnoreCase(mojo.datePackage4Java)) {
                    return "java.time.LocalDateTime";
                } else {
                    return "java.util.Date";
                }
            case "date":
                if ("java.time".equalsIgnoreCase(mojo.datePackage4Java)) {
                    return "java.time.LocalDate";
                } else {
                    return "java.util.Date";
                }
            case "time":
                if ("java.time".equalsIgnoreCase(mojo.datePackage4Java)) {
                    return "java.time.LocalTime";
                } else {
                    return "java.util.Date";
                }
            case "int":
                return "Integer";
            case "bigint":
                return "Long";
            case "smallint":
                return "Short";
            case "bit":
                return "Boolean";
            case "tinyint":
                if (".deleted.".contains("." + columnName + ".")) {
                    return "Boolean";
                }
                if (mojo.deletedField.equalsIgnoreCase(columnName)) {
                    return "Boolean";
                }
                if (columnType.equalsIgnoreCase("tinyint(1)")) {
                    return "Boolean";
                }
                if (comment.contains("是否")) {
                    return "Boolean";
                }
                return "Byte";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "decimal":
                return "java.math.BigDecimal";
            default:
                break;
        }
        throw new RuntimeException("包含未支持字段类型！" + dataType);
    }

    public static String getColumnDefaultJavaLiteral(Map<String, Object> column) {
        String columnDefault = column.get("COLUMN_DEFAULT") == null ? null : column.get("COLUMN_DEFAULT").toString();
        switch (SqlSchemaUtils.getColumnJavaType(column)) {
            case "String":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    return "\"" + columnDefault.replace("\"", "\\\"") + "\"";
                } else {
                    return "\"\"";
                }
            case "Integer":
            case "Short":
            case "Byte":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    return "" + columnDefault;
                } else {
                    return "0";
                }
            case "Long":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    return "" + columnDefault + "L";
                } else {
                    return "0L";
                }
            case "Boolean":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    if (columnDefault.trim().equalsIgnoreCase("b'1'")) {
                        return "false";
                    } else if (columnDefault.trim().equalsIgnoreCase("b'0'")) {
                        return "true";
                    }
                    return "" + (columnDefault.trim().equalsIgnoreCase("0") ? "false" : "true");
                } else {
                    return "false";
                }
            case "Float":
            case "Double":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    return "" + columnDefault;
                } else {
                    return "0";
                }
            case "java.math.BigDecimal":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    return "java.math.BigDecimal.valueOf(" + columnDefault + ")";
                } else {
                    return "java.math.BigDecimal.ZERO";
                }
            case "java.util.Date":
            default:
                break;
        }
        return ""; // = ""
    }

    public static boolean isAutoUpdateDateColumn(Map<String, Object> column) {
        String extra = column.get("EXTRA") == null ? "" : column.get("EXTRA").toString();
        if ("on update CURRENT_TIMESTAMP".equalsIgnoreCase(extra)) {
            return true;
        }
        return false;
    }

    public static boolean isAutoInsertDateColumn(Map<String, Object> column){
        String defaultData = column.get("COLUMN_DEFAULT") == null ? "" : column.get("COLUMN_DEFAULT").toString();
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultData)) {
            return true;
        }
        return false;
    }

    public static boolean isColumnInTable(Map<String, Object> column, Map<String, Object> table){
        return column.get("TABLE_NAME").toString().equalsIgnoreCase(table.get("TABLE_NAME").toString());
    }

    public static int getOridinalPosition(Map<String, Object> column){
        return Integer.parseInt(column.get("ORDINAL_POSITION").toString());
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

    public static String getColumnDbDataType(Map<String, Object> column) {
        return column.get("DATA_TYPE").toString();
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
            comment = SqlSchemaUtils.ANNOTATION_PATTERN.matcher(comment).replaceAll("");
        }
        return comment.trim();
    }
}
