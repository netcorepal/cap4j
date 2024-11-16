package org.netcorepal.cap4j.ddd.codegen.misc;

import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.SqlSchemaUtils.*;

/**
 * postgresql 数据库schema工具
 *
 * @author binking338
 * @date 2024/9/18
 */
public class SqlSchemaUtils4Postgresql {
    public static List<Map<String, Object>> resolveTables(String connectionString, String user, String pwd) {
        String tableSql = "SELECT rel.relname AS table_name, obj_description(rel.oid) AS table_comment\n" +
                "FROM pg_class rel\n" +
                "WHERE rel.relkind = 'r' AND rel.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '" + mojo.schema + "')";
        if (StringUtils.isNotBlank(mojo.table)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.table.split(mojo.PATTERN_SPLITTER)).map(t -> "rel.relname like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            tableSql += " and (" + whereClause + ")";
        }
        if (StringUtils.isNotBlank(mojo.ignoreTable)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.ignoreTable.split(mojo.PATTERN_SPLITTER)).map(t -> "rel.relname like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            tableSql += " and not (" + whereClause + ")";
        }
        mojo.getLog().debug(tableSql);
        return executeQuery(tableSql, connectionString, user, pwd);
    }

    public static List<Map<String, Object>> resolveColumns(String connectionString, String user, String pwd) {
        String columnSql = "SELECT\n" +
                "    att.attnum  AS num, -- 列序号\n" +
                "    tbl.relname  AS table_name, -- 列序号\n" +
                "    att.attname AS column_name, -- 列名\n" +
                "    typ.typname AS data_type, -- 数据类型\n" +
                "\n" +
                "    CASE when\n" +
                "             SUBSTRING ( format_type ( att.atttypid, att.atttypmod ) FROM '\\(.*\\)' ) isNUll\n" +
                "             then '0'\n" +
                "         else\n" +
                "             substr(SUBSTRING ( format_type ( att.atttypid, att.atttypmod ) FROM '\\(.*\\)' ),2,CHAR_LENGTH(SUBSTRING ( format_type ( att.atttypid, att.atttypmod ) FROM '\\(.*\\)' ))-2)\n" +
                "        end as data_len, -- 数据长度\n" +
                "    att.attnotnull AS not_null, -- 非空\n" +
                "    att.attidentity AS identity, -- 自增主键\n" +
                "    format_type(att.atttypid, att.atttypmod) AS column_type, -- 列数据类型\n" +
                "    col.column_default AS column_default, -- 列默认值\n" +
                "    pgd.description AS column_comment,   -- 列注释\n" +
                "    kcu.constraint_type='PRIMARY KEY' as primary_key -- 主键\n" +
                "FROM\n" +
                "    pg_attribute att\n" +
                "        JOIN\n" +
                "    pg_class tbl ON att.attrelid = tbl.oid\n" +
                "        JOIN\n" +
                "    pg_type typ ON typ.oid = att.atttypid\n" +
                "        JOIN\n" +
                "    pg_namespace ns ON tbl.relnamespace = ns.oid\n" +
                "        LEFT JOIN\n" +
                "    pg_description pgd ON pgd.objoid = att.attrelid AND pgd.objsubid = att.attnum\n" +
                "        LEFT JOIN\n" +
                "    information_schema.columns col ON col.table_name = tbl.relname AND col.column_name = att.attname\n" +
                "        LEFT JOIN\n" +
                "    (\n" +
                "        SELECT tc.table_name, kcu.column_name, kcu.constraint_name, tc.constraint_type\n" +
                "        FROM information_schema.key_column_usage kcu\n" +
                "        LEFT JOIN information_schema.table_constraints tc\n" +
                "            ON tc.constraint_type = 'PRIMARY KEY' AND kcu.constraint_name = tc.constraint_name AND kcu.table_schema = tc.table_schema\n" +
                "    ) kcu ON kcu.table_name = tbl.relname AND kcu.column_name = att.attname\n" +
                "WHERE tbl.relkind = 'r'\n" +
                "  AND ns.nspname = '" + mojo.schema + "'        -- 模式名，替换为你的模式名（例如 public）\n" +
                "  AND att.attnum > 0               -- 排除系统列\n" +
                "  AND NOT att.attisdropped";
        if (StringUtils.isNotBlank(mojo.table)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.table.split(mojo.PATTERN_SPLITTER)).map(t -> "tbl.relname like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            columnSql += " and (" + whereClause + ")";
        }
        if (StringUtils.isNotBlank(mojo.ignoreTable)) {
            String whereClause = String.join(" or ", Arrays.stream(mojo.ignoreTable.split(mojo.PATTERN_SPLITTER)).map(t -> "tbl.relname like " + LEFT_QUOTES_4_LITERAL_STRING + t + RIGHT_QUOTES_4_LITERAL_STRING).collect(Collectors.toList()));
            columnSql += " and not (" + whereClause + ")";
        }
        mojo.getLog().debug(columnSql);
        return executeQuery(columnSql, connectionString, user, pwd);
    }

    /**
     * 获取列的Java映射类型(Mysql)
     *
     * @param column
     * @return
     */
    public static String getColumnJavaType(Map<String, Object> column) {
        String dataType = column.get("data_type").toString().toLowerCase();
        String columnType = column.get("column_type").toString().toLowerCase();
        String comment = SqlSchemaUtils.getComment(column);
        String columnName = SqlSchemaUtils.getColumnName(column).toLowerCase();
        if (mojo.typeRemapping != null && mojo.typeRemapping.containsKey(dataType)) {
            // 类型重映射
            return mojo.typeRemapping.get(dataType);
        }
        switch (dataType) {
            case "char":
            case "varchar":
            case "text":
            case "name":
            case "character":
            case "nchar":
                return "String";
            case "timestamp":
            case "timestamptz":
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
            case "timetz":
                if ("java.time".equalsIgnoreCase(mojo.datePackage4Java)) {
                    return "java.time.LocalTime";
                } else {
                    return "java.util.Date";
                }
            case "int4":
            case "int":
            case "integer":
            case "serial":
                return "Integer";
            case "int8":
                return "Long";
            case "int2":
            case "smallint":
                return "Short";
            case "bool":
            case "boolean":
                return "Boolean";
            case "float4":
                return "Float";
            case "float8":
                return "Double";
            case "decimal":
            case "money":
            case "numeric":
                return "java.math.BigDecimal";
            default:
                break;
        }
        throw new RuntimeException("包含未支持字段类型！" + dataType);
    }

    public static String getColumnDefaultJavaLiteral(Map<String, Object> column) {
        String columnDefault = column.get("column_default") == null ? null : column.get("column_default").toString();
        switch (SqlSchemaUtils.getColumnJavaType(column)) {
            case "String":
                if (StringUtils.isNotEmpty(columnDefault)) {
                    String defaultString = columnDefault.split(":")[0];
                    if (defaultString.startsWith("'") && defaultString.endsWith("'")) {
                        return "\"" + defaultString.substring(1, defaultString.length() - 2).replace("\"", "\\\"") + "\"";
                    } else {
                        return "\"" + defaultString.replace("\"", "\\\"") + "\"";
                    }
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
                    if (columnDefault.trim().equalsIgnoreCase("false")) {
                        return "false";
                    } else if (columnDefault.trim().equalsIgnoreCase("true")) {
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
            default:
                break;
        }
        return ""; // = ""
    }

    public static boolean isAutoUpdateDateColumn(Map<String, Object> column) {
        return false;
    }

    public static boolean isAutoInsertDateColumn(Map<String, Object> column) {
        String defaultData = column.get("column_default") == null ? "" : column.get("column_default").toString();
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultData)) {
            return true;
        }
        return false;
    }

    public static boolean isColumnInTable(Map<String, Object> column, Map<String, Object> table) {
        return column.get("table_name").toString().equalsIgnoreCase(table.get("table_name").toString());
    }

    public static int getOridinalPosition(Map<String, Object> column) {
        return Integer.parseInt(column.get("num").toString());
    }


    public static boolean hasColumn(String columnName, List<Map<String, Object>> columns) {
        return columns.stream().anyMatch(col -> col.get("column_name").toString().equalsIgnoreCase(columnName));
    }

    public static String getName(Map<String, Object> tableOrColumn) {
        return tableOrColumn.containsKey("column_name") ? getColumnName(tableOrColumn) : getTableName(tableOrColumn);
    }

    public static String getColumnName(Map<String, Object> column) {
        return column.get("column_name").toString();
    }

    public static String getTableName(Map<String, Object> table) {
        return table.get("table_name").toString();
    }

    public static String getColumnDbType(Map<String, Object> column) {
        return column.get("column_type").toString();
    }

    public static String getColumnDbDataType(Map<String, Object> column) {
        return column.get("data_type").toString();
    }

    public static boolean isColumnNullable(Map<String, Object> column) {
        return "true".equalsIgnoreCase(column.get("not_null").toString());
    }

    public static boolean isColumnPrimaryKey(Map<String, Object> column) {
        return "true".equalsIgnoreCase(column.get("primary_key").toString());
    }

    public static String getComment(Map<String, Object> tableOrColumn, boolean cleanAnnotations) {
        String comment = "";
        if (tableOrColumn.containsKey("table_comment")) {
            comment = tableOrColumn.get("table_comment") == null ? "" : tableOrColumn.get("table_comment").toString();
        } else if (tableOrColumn.containsKey("column_comment")) {
            comment = tableOrColumn.get("column_comment") == null ? "" : tableOrColumn.get("column_comment").toString();
        }

        if (cleanAnnotations) {
            comment = SqlSchemaUtils.ANNOTATION_PATTERN.matcher(comment).replaceAll("");
        }
        return comment.trim();
    }
}
