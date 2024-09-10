package org.netcorepal.cap4j.ddd.codegen.misc;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.netcorepal.cap4j.ddd.codegen.GenEntityMojo;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author binking338
 * @date 2022-03-06
 */
public class SqlSchemaUtils {
    public static final String DB_TYPE_MYSQL = "mysql";
    public static final String DB_TYPE_POSTGRESQL = "postgresql";
    public static final String DB_TYPE_SQLSERVER = "sqlserver";
    public static final String DB_TYPE_ORACLE = "oracle";

    public static String LEFT_QUOTES_4_ID_ALIAS = "`";
    public static String RIGHT_QUOTES_4_ID_ALIAS = "`";
    public static String LEFT_QUOTES_4_LITERAL_STRING = "'";
    public static String RIGHT_QUOTES_4_LITERAL_STRING = "'";
    public static GenEntityMojo mojo;

    static Pattern ANNOTATION_PATTERN = Pattern.compile("@([A-Za-z]+)(\\=[^;]+)?;?");

    private static Log getLog() {
        return mojo.getLog();
    }


    /**
     * 识别数据库类型
     *
     * @param connectionString
     * @return
     */
    public static String recognizeDbType(String connectionString) {
        try {
            return connectionString.split(":")[1];
        } catch (Exception ex) {
            getLog().error("数据库连接串异常 " + connectionString, ex);
        }
        return DB_TYPE_MYSQL;
    }

    /**
     * 处理数据库方言语法配置
     *
     * @param dbType
     */
    public static void processSqlDialet(String dbType) {
        switch (dbType) {
            default:
            case DB_TYPE_MYSQL:
                LEFT_QUOTES_4_ID_ALIAS = "`";
                RIGHT_QUOTES_4_ID_ALIAS = "`";
                LEFT_QUOTES_4_LITERAL_STRING = "'";
                RIGHT_QUOTES_4_LITERAL_STRING = "'";
                break;
            case DB_TYPE_POSTGRESQL:
            case DB_TYPE_ORACLE:
                LEFT_QUOTES_4_ID_ALIAS = "\"";
                RIGHT_QUOTES_4_ID_ALIAS = "\"";
                LEFT_QUOTES_4_LITERAL_STRING = "'";
                RIGHT_QUOTES_4_LITERAL_STRING = "'";
                break;
            case DB_TYPE_SQLSERVER:
                LEFT_QUOTES_4_ID_ALIAS = "[";
                RIGHT_QUOTES_4_ID_ALIAS = "]";
                LEFT_QUOTES_4_LITERAL_STRING = "'";
                RIGHT_QUOTES_4_LITERAL_STRING = "'";
                break;
        }
    }

    /**
     * 执行SQL查询
     *
     * @param sql
     * @param connectionString
     * @param user
     * @param pwd
     * @return
     */
    static List<Map<String, Object>> executeQuery(String sql, String connectionString, String user, String pwd) {
        String URL = connectionString;
        String USER = user;
        String PASSWORD = pwd;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            //1.加载驱动程序
            switch (recognizeDbType(connectionString)) {
                default:
                case DB_TYPE_MYSQL:
                    Class.forName("com.mysql.jdbc.Driver");
                    break;
            }
            //2.获得数据库链接
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            //3.通过数据库的连接操作数据库，实现增删改查（使用Statement类）
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            //4.处理数据库的返回结果(使用ResultSet类)
            while (rs.next()) {
                HashMap<String, Object> map = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), rs.getObject(i));
                    if (rs.getObject(i) != null && rs.getObject(i) instanceof byte[]) {
                        map.put(metaData.getColumnName(i), new String((byte[]) rs.getObject(i)));
                    }
                }
                result.add(map);
            }
            //关闭资源
            rs.close();
            st.close();
            conn.close();
        } catch (Throwable e) {
            getLog().error(e);
        }

        return result;
    }

    /**
     * 获取表的信息
     *
     * @param connectionString
     * @param user
     * @param pwd
     * @return
     */
    public static List<Map<String, Object>> resolveTables(String connectionString, String user, String pwd) {
        String dbType = recognizeDbType(connectionString);
        switch (dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.resolveTables(connectionString, user, pwd);
        }
    }

    /**
     * 获取表的列信息
     *
     * @param connectionString
     * @param user
     * @param pwd
     * @return
     */
    public static List<Map<String, Object>> resolveColumns(String connectionString, String user, String pwd) {
        String dbType = recognizeDbType(connectionString);
        switch (dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.resolveColumns(connectionString, user, pwd);
        }
    }

    /**
     * 获取列的Java映射类型
     *
     * @param column
     * @return
     */
    public static String getColumnJavaType(Map<String, Object> column) {
        if (SqlSchemaUtils.hasType(column)) {
            String customerType = SqlSchemaUtils.getType(column);
            if (SqlSchemaUtils.hasEnum(column) && mojo.EnumPackageMap.containsKey(customerType)) {
                return mojo.EnumPackageMap.get(customerType) + "." + customerType;
            } else {
                return customerType;
            }
        }
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getColumnJavaType(column);
        }
    }

    /**
     * 获取字段的默认Java字面量
     *
     * @param column
     * @return
     */
    public static String getColumnDefaultJavaLiteral(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getColumnDefaultJavaLiteral(column);
        }
    }

    /**
     * 判断字段是否为自动更新时间戳
     *
     * @param column
     * @return
     */
    public static boolean isAutoUpdateDateColumn(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.isAutoUpdateDateColumn(column);
        }
    }

    /**
     * 判断字段是否为自动插入时间戳
     *
     * @param column
     * @return
     */
    public static boolean isAutoInsertDateColumn(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.isAutoInsertDateColumn(column);
        }
    }

    /**
     * 判断字段是否在表内
     *
     * @param column
     * @param table
     * @return
     */
    public static boolean isColumnInTable(Map<String, Object> column, Map<String, Object> table) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.isColumnInTable(column, table);
        }
    }

    /**
     * 获取列的序号
     *
     * @param column
     * @return
     */
    public static int getOridinalPosition(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getOridinalPosition(column);
        }
    }

    /**
     * 判断是否包含指定字段
     *
     * @param columnName
     * @param columns
     * @return
     */
    public static boolean hasColumn(String columnName, List<Map<String, Object>> columns) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.hasColumn(columnName, columns);
        }
    }

    /**
     * 获取名称
     *
     * @param tableOrColumn
     * @return
     */
    public static String getName(Map<String, Object> tableOrColumn) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getName(tableOrColumn);
        }
    }

    /**
     * 获取列名
     *
     * @param column
     * @return
     */
    public static String getColumnName(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getColumnName(column);
        }
    }

    /**
     * 获取表名
     *
     * @param tableOrColumn
     * @return
     */
    public static String getTableName(Map<String, Object> tableOrColumn) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getTableName(tableOrColumn);
        }
    }

    /**
     * 获取数据库类型
     *
     * @param column
     * @return
     */
    public static String getColumnDbType(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getColumnDbType(column);
        }
    }

    /**
     * 获取数据库类型
     *
     * @param column
     * @return
     */
    public static String getColumnDbDataType(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getColumnDbDataType(column);
        }
    }

    /**
     * 是否可空字段
     *
     * @param column
     * @return
     */
    public static boolean isColumnNullable(Map<String, Object> column) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.isColumnNullable(column);
        }
    }

    /**
     * 读取注释
     *
     * @param tableOrColumn
     * @param cleanAnnotations
     * @return
     */
    public static String getComment(Map<String, Object> tableOrColumn, boolean cleanAnnotations) {
        switch (mojo.dbType) {
            default:
            case DB_TYPE_MYSQL:
                return SqlSchemaUtils4Mysql.getComment(tableOrColumn, cleanAnnotations);
        }
    }

    /**
     * 读取注释
     *
     * @param tableOrColumn
     * @return
     */
    public static String getComment(Map<String, Object> tableOrColumn) {
        return getComment(tableOrColumn, true);
    }

    /**
     * 读取注释注解
     *
     * @param tableOrColumn
     * @return
     */
    public static Map<String, String> getAnnotations(Map<String, Object> tableOrColumn) {
        String comment = getComment(tableOrColumn, false);
        if (mojo.AnnotaionsCache.containsKey(comment)) {
            return mojo.AnnotaionsCache.get(comment);
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
        mojo.AnnotaionsCache.putIfAbsent(comment, annotations);
        return annotations;
    }

    /**
     * 判断是否包含指定注解
     *
     * @param tableOrColumn
     * @param annotation
     * @return
     */
    public static boolean hasAnnotation(Map<String, Object> tableOrColumn, String annotation) {
        return getAnnotations(tableOrColumn).containsKey(annotation);
    }

    /**
     * 获取单个注解值
     *
     * @param tableOrColumn
     * @param annotation
     * @return
     */
    public static String getAnnotation(Map<String, Object> tableOrColumn, String annotation) {
        return getAnnotations(tableOrColumn).getOrDefault(annotation, "");
    }

    /**
     * 获取任意注解值
     *
     * @param tableOrColumn
     * @param annotations
     * @return
     */
    public static String getAnyAnnotation(Map<String, Object> tableOrColumn, List<String> annotations) {
        for (String annotaion :
                annotations) {
            if (hasAnnotation(tableOrColumn, annotaion)) {
                return getAnnotation(tableOrColumn, annotaion);
            }
        }
        return "";
    }

    /**
     * 是否包含任意注解
     *
     * @param tableOrColumn
     * @param annotations
     * @return
     */
    public static boolean hasAnyAnnotation(Map<String, Object> tableOrColumn, List<String> annotations) {
        for (String annotaion :
                annotations) {
            if (hasAnnotation(tableOrColumn, annotaion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含懒加载注解
     *
     * @param table
     * @return
     */
    public static boolean hasLazy(Map<String, Object> table) {
        return hasAnyAnnotation(table, Arrays.asList("Lazy", "L"));
    }

    /**
     * 是否启用JPA懒加载
     *
     * @param table
     * @return
     */
    public static boolean isLazy(Map<String, Object> table) {
        return isLazy(table, false);
    }

    /**
     * 是否启用JPA懒加载
     *
     * @param table
     * @param defaultLazy
     * @return
     */
    public static boolean isLazy(Map<String, Object> table, boolean defaultLazy) {
        String val = getAnyAnnotation(table, Arrays.asList("Lazy", "L"));
        if (defaultLazy) {
            return "false".equalsIgnoreCase(val) || "0".equalsIgnoreCase(val) ? false : true;
        } else {
            return "true".equalsIgnoreCase(val) || "1".equalsIgnoreCase(val) ? true : false;
        }
    }

    /**
     * 是否是计数为1的实体成员
     *
     * @param table
     * @return
     */
    public static boolean countIsOne(Map<String, Object> table) {
        String val = getAnyAnnotation(table, Arrays.asList("Count", "C"));
        return "One".equalsIgnoreCase(val) || "1".equalsIgnoreCase(val) ? true : false;
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
            String[] enumConfigs = TextUtils.splitWithTrim(enumsConfig, "\\|");
            for (int i = 0; i < enumConfigs.length; i++) {
                String enumConfig = enumConfigs[i];
                getLog().debug(enumConfig);
                List<String> pair = Arrays.stream(enumConfig.split("\\:"))
                        .map(c -> c.trim()
                                .replace("\n", "")
                                .replace("\r", "")
                                .replace("\t", ""))
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

    /**
     * 是否生成工厂
     *
     * @param table
     * @return
     */
    public static boolean hasFactory(Map<String, Object> table) {
        return isAggregateRoot(table) && hasAnyAnnotation(table, Arrays.asList("Factory", "Fac"));
    }

    /**
     * 是否生成规约
     *
     * @param table
     * @return
     */
    public static boolean hasSpecification(Map<String, Object> table) {
        return isAggregateRoot(table) && hasAnyAnnotation(table, Arrays.asList("Specification", "Spec"));
    }

    public static boolean hasDomainEvent(Map<String, Object> table) {
        return isAggregateRoot(table) && hasAnyAnnotation(table, Arrays.asList("DomainEvent", "DE", "Event", "Evt"));
    }

    public static List<String> getDomainEvent(Map<String, Object> table) {
        if (!isAggregateRoot(table)) {
            return Collections.emptyList();
        }
        String literalDomainEvents = getAnyAnnotation(table, Arrays.asList("DomainEvent", "DE", "Event", "Evt"));
        if (StringUtils.isBlank(literalDomainEvents)) {
            return Collections.emptyList();
        }
        return Arrays.stream(TextUtils.splitWithTrim(literalDomainEvents, "\\|")).collect(Collectors.toList());
    }
}
