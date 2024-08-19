package org.netcorepal.cap4j.ddd.codegen;

import org.codehaus.plexus.util.FileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.Inflector;
import org.netcorepal.cap4j.ddd.codegen.misc.MysqlSchemaUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils.toLowerCamelCase;
import static org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils.toUpperCamelCase;
import static org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils.writeLine;

/**
 * @author binking338
 * @date 2022-02-16
 */
@Mojo(name = "gen-entity")
public class GenEntityMojo extends MyAbstractMojo {

    private Map<String, Map<String, Object>> TableMap = new HashMap<>();
    private Map<String, List<Map<String, Object>>> ColumnsMap = new HashMap<>();
    private Map<String, Map<Integer, String[]>> EnumConfigMap = new HashMap<>();
    private Map<String, String> EnumPackageMap = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("开始生成实体代码");
        MysqlSchemaUtils.mojo = this;

        // 项目结构解析
        String absoluteCurrentDir, projectDir, domainModulePath, applicationModulePath, adapterModulePath;
        absoluteCurrentDir = new File("").getAbsolutePath();
        if (multiModule) {
            projectDir = new File(absoluteCurrentDir + File.separator + "pom.xml").exists()
                    ? absoluteCurrentDir
                    : new File(absoluteCurrentDir).getParent();

            domainModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Domain))
                    .findFirst().get().getAbsolutePath();
            applicationModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Application))
                    .findFirst().get().getAbsolutePath();
            adapterModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Adapter))
                    .findFirst().get().getAbsolutePath();
        } else {
            projectDir = absoluteCurrentDir;
            domainModulePath = absoluteCurrentDir;
            applicationModulePath = absoluteCurrentDir;
            adapterModulePath = absoluteCurrentDir;
        }
        String basePackage = StringUtils.isNotBlank(this.basePackage)
                ? this.basePackage
                : SourceFileUtils.resolveBasePackage(domainModulePath);
        getLog().info(multiModule ? "多模块项目" : "单模块项目");
        getLog().info("项目目录：" + projectDir);
        getLog().info("适配层目录：" + adapterModulePath);
        getLog().info("应用层目录：" + applicationModulePath);
        getLog().info("领域层目录：" + domainModulePath);
        getLog().info("基础包名：" + basePackage);

        if (StringUtils.isBlank(entityMetaInfoClassOutputPackage)) {
            entityMetaInfoClassOutputPackage = "domain._share.meta";
        }

        // 数据库解析
        String tableSql = "select * from `information_schema`.`tables` where table_schema= '" + schema + "'";
        String columnSql = "select * from `information_schema`.`columns` where table_schema= '" + schema + "'";
        if (StringUtils.isNotBlank(table)) {
            String whereClause = String.join(" or ", Arrays.stream(table.split(",")).map(t -> "table_name like '" + t + "'").collect(Collectors.toList()));
            tableSql += " and (" + whereClause + ")";
            columnSql += " and (" + whereClause + ")";
        }
        if (StringUtils.isNotBlank(ignoreTable)) {
            String whereClause = String.join(" or ", Arrays.stream(ignoreTable.split(",")).map(t -> "table_name like '" + t + "'").collect(Collectors.toList()));
            tableSql += " and not (" + whereClause + ")";
            columnSql += " and not (" + whereClause + ")";
        }
        List<Map<String, Object>> tables = executeQuery(tableSql, connectionString, user, pwd);
        List<Map<String, Object>> columns = executeQuery(columnSql, connectionString, user, pwd);
        Map<String, Map<String, String>> relations = new HashMap<>();
        Map<String, String> tablePackageMap = new HashMap<>();
        getLog().info("");
        getLog().info("待解析数据库表：");
        for (Map<String, Object> table :
                tables) {
            List<Map<String, Object>> tableColumns = columns.stream().filter(col -> col.get("TABLE_NAME").equals(table.get("TABLE_NAME")))
                    .sorted((a, b) -> (Integer.parseInt(a.get("ORDINAL_POSITION").toString())) - Integer.parseInt(b.get("ORDINAL_POSITION").toString()))
                    .collect(Collectors.toList());
            TableMap.put(MysqlSchemaUtils.getTableName(table), table);
            ColumnsMap.put(MysqlSchemaUtils.getTableName(table), tableColumns);

            getLog().info(String.format("%20s : (%s)",
                    MysqlSchemaUtils.getTableName(table),
                    String.join(", ", tableColumns.stream().map(c -> String.format("%s %s", c.get("DATA_TYPE"), MysqlSchemaUtils.getColumnName(c))).collect(Collectors.toList()))));
        }
        getLog().info("");
        getLog().info("");

        getLog().info("----------------开始字段扫描----------------");
        getLog().info("");
        for (Map<String, Object> table :
                TableMap.values()) {
            List<Map<String, Object>> tableColumns = ColumnsMap.get(MysqlSchemaUtils.getTableName(table));
            // 解析表关系
            getLog().info("开始解析表关系:" + MysqlSchemaUtils.getTableName(table));
            Map<String, Map<String, String>> relationTable = resolveRelationTable(table, tableColumns);
            for (Map.Entry<String, Map<String, String>> entry :
                    relationTable.entrySet()) {
                if (!relations.containsKey(entry.getKey())) {
                    relations.put(entry.getKey(), entry.getValue());
                } else {
                    relations.get(entry.getKey()).putAll(entry.getValue());
                }
            }
            tablePackageMap.put(MysqlSchemaUtils.getTableName(table), resolvePackage(table, basePackage, domainModulePath));
            getLog().info("结束解析表关系:" + MysqlSchemaUtils.getTableName(table));
            getLog().info("");
        }

        for (Map<String, Object> table :
                TableMap.values()) {
            if (isIgnoreTable(table)) {
                continue;
            }
            List<Map<String, Object>> tableColumns = ColumnsMap.get(MysqlSchemaUtils.getTableName(table));
            for (Map<String, Object> column :
                    tableColumns) {
                if (MysqlSchemaUtils.hasEnum(column)) {
                    Map<Integer, String[]> enumConfig = MysqlSchemaUtils.getEnum(column);
                    if (enumConfig.size() > 0) {
                        EnumConfigMap.put(MysqlSchemaUtils.getType(column), enumConfig);
                        EnumPackageMap.put(MysqlSchemaUtils.getType(column), basePackage + "." + getEntityPackage(MysqlSchemaUtils.getTableName(table)) + ".enums");
                    }
                }
            }
        }
        getLog().info("----------------完成字段扫描----------------");

        getLog().info("");
        getLog().info("");

        getLog().info("----------------开始生成枚举----------------");
        getLog().info("");
        for (Map.Entry<String, Map<Integer, String[]>> entry : EnumConfigMap.entrySet()) {
            try {
                writeEnumSourceFile(entry.getValue(), entry.getKey(), EnumPackageMap.get(entry.getKey()), domainModulePath, enumValueField, enumNameField);
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        getLog().info("----------------完成生成枚举----------------");

        getLog().info("");
        getLog().info("");

        getLog().info("----------------开始生成实体----------------");
        getLog().info("");
        if (generateSchema) {
            try {
                writeBaseSechemaSourceFile(basePackage, applicationModulePath);
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        for (Map<String, Object> table :
                TableMap.values()) {
            List<Map<String, Object>> tableColumns = ColumnsMap.get(MysqlSchemaUtils.getTableName(table));
            try {
                writeEntitySourceFile(table, tableColumns, tablePackageMap, relations, basePackage, domainModulePath);
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        if (generateBuild) {
            try {
                writeEntityBuilderSourceFile(basePackage, applicationModulePath, tablePackageMap, relations);
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        getLog().info("----------------完成生成实体----------------");
        getLog().info("");
    }

    public List<Map<String, Object>> executeQuery(String sql, String connectionString, String user, String pwd) {
        String URL = connectionString;
        String USER = user;
        String PASSWORD = pwd;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            //1.加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
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
            this.getLog().error(e);
        }

        return result;
    }


    /**
     * 获取模块
     *
     * @param tableName
     * @return
     */
    public String getModule(String tableName) {
        Map<String, Object> table = TableMap.get(tableName);
        String module = MysqlSchemaUtils.getModule(table);
        getLog().info("尝试解析模块:" + MysqlSchemaUtils.getTableName(table) + " " + module);
        while (!MysqlSchemaUtils.isAggregateRoot(table) && StringUtils.isBlank(module)) {
            table = TableMap.get(MysqlSchemaUtils.getParent(table));
            module = MysqlSchemaUtils.getModule(table);
            getLog().info("尝试父表模块:" + MysqlSchemaUtils.getTableName(table) + " " + module);
        }
        getLog().info("模块解析结果:" + MysqlSchemaUtils.getTableName(table) + " " + module);
        return module;
    }

    /**
     * 获取聚合
     *
     * @param tableName
     * @return
     */
    public String getAggregate(String tableName) {
        Map<String, Object> table = TableMap.get(tableName);
        String aggregate = MysqlSchemaUtils.getAggregate(table);
        getLog().info("尝试解析聚合:" + MysqlSchemaUtils.getTableName(table) + " " + aggregate);
        while (!MysqlSchemaUtils.isAggregateRoot(table) && StringUtils.isBlank(aggregate)) {
            table = TableMap.get(MysqlSchemaUtils.getParent(table));
            aggregate = MysqlSchemaUtils.getAggregate(table);
            getLog().info("尝试父表聚合:" + MysqlSchemaUtils.getTableName(table) + " " + aggregate);
        }
        getLog().info("聚合解析结果:" + tableName + " " + aggregate);
        return aggregate;
    }

    private Map<String, String> EntityJavaTypeMap = new HashMap<>();

    /**
     * 获取实体类 Class.SimpleName
     *
     * @param tableName
     * @return
     */
    public String getEntityJavaType(String tableName) {
        if (EntityJavaTypeMap.containsKey(tableName)) {
            return EntityJavaTypeMap.get(tableName);
        }
        Map<String, Object> table = TableMap.get(tableName);
        String type = MysqlSchemaUtils.getType(table);
        if (StringUtils.isBlank(type)) {
            type = toUpperCamelCase(tableName);
        }
        if (StringUtils.isNotBlank(type)) {
            getLog().info("解析实体类名:" + MysqlSchemaUtils.getTableName(table) + " --> " + type);
            EntityJavaTypeMap.put(tableName, type);
            return type;
        }
        throw new RuntimeException("实体类名未生成");
    }

    /**
     * 获取实体类 package
     *
     * @param tableName
     * @return
     */
    public String getEntityPackage(String tableName) {
        String module = getModule(tableName);
        String aggregate = getAggregate(tableName);
        String packageName = ("domain.aggregates"
                + (StringUtils.isNotBlank(module) ? "." + module : "")
                + (StringUtils.isNotBlank(aggregate) ? "." + aggregate : "")
        );
        return packageName;
    }


    public boolean isReservedColumn(Map<String, Object> column) {
        String columnName = MysqlSchemaUtils.getColumnName(column).toLowerCase();
        boolean isReserved = idField.equalsIgnoreCase(columnName)
                || versionField.equalsIgnoreCase(columnName)
                || columnName.startsWith("db_");
        return isReserved;
    }

    public boolean isReadOnlyColumn(Map<String, Object> column) {
        if (MysqlSchemaUtils.hasReadOnly(column)) {
            return true;
        }
        String columnName = MysqlSchemaUtils.getColumnName(column).toLowerCase();
        if (StringUtils.isNotBlank(readonlyFields)
                && Arrays.stream(readonlyFields.toLowerCase().split("[\\,\\;]")).anyMatch(
                c -> columnName.matches(c.replace("%", ".*")))) {
            return true;
        }

        return false;
    }

    public boolean isIgnoreTable(Map<String, Object> table) {
        if (MysqlSchemaUtils.isIgnore(table)) {
            return true;
        }
        return false;
    }

    public boolean isIgnoreColumn(Map<String, Object> column) {
        if (MysqlSchemaUtils.isIgnore(column)) {
            return true;
        }
        String columnName = MysqlSchemaUtils.getColumnName(column).toLowerCase();
        if (StringUtils.isNotBlank(ignoreFields)
                && Arrays.stream(ignoreFields.toLowerCase().split("[\\,\\;]")).anyMatch(
                c -> columnName.matches(c.replace("%", ".*")))) {
            return true;
        }
        return false;
    }

    /**
     * 获取列的Java映射类型
     *
     * @param column
     * @return
     */
    public String getColumnJavaType(Map<String, Object> column) {
        if (MysqlSchemaUtils.hasType(column)) {
            String customerType = MysqlSchemaUtils.getType(column);
            if (MysqlSchemaUtils.hasEnum(column) && EnumPackageMap.containsKey(customerType)) {
                return EnumPackageMap.get(customerType) + "." + customerType;
            } else {
                return customerType;
            }
        }
        String dataType = column.get("DATA_TYPE").toString().toLowerCase();
        String columnType = column.get("COLUMN_TYPE").toString().toLowerCase();
        String comment = MysqlSchemaUtils.getComment(column);
        String columnName = MysqlSchemaUtils.getColumnName(column).toLowerCase();
        if (typeRemapping != null && typeRemapping.containsKey(dataType)) {
            // 类型重映射
            return typeRemapping.get(dataType);
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
                if ("java.time".equalsIgnoreCase(datePackage4Java)) {
                    return "java.time.LocalDateTime";
                } else {
                    return "java.util.Date";
                }
            case "date":
                if ("java.time".equalsIgnoreCase(datePackage4Java)) {
                    return "java.time.LocalDate";
                } else {
                    return "java.util.Date";
                }
            case "time":
                if ("java.time".equalsIgnoreCase(datePackage4Java)) {
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
                if (deletedField.equalsIgnoreCase(columnName)) {
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

    /**
     * 关系格式 table1 table2 relation;join_column;[inverse_join_column;][table;][LAZY;]
     * <p>
     * 说明 relation
     * 聚合根 与 聚合根：OneToOne、ManyToOne、ManyToMany
     * 聚合内部实体间：OneToMany
     *
     * @param table
     * @param columns
     * @return
     */
    public Map<String, Map<String, String>> resolveRelationTable(Map<String, Object> table, List<Map<String, Object>> columns) {
        Map<String, Map<String, String>> result = new HashMap<>();
        String tableName = MysqlSchemaUtils.getTableName(table);

        if (isIgnoreTable(table)) {
            return result;
        }
        // 聚合内部关系 OneToMany
        if (!MysqlSchemaUtils.isAggregateRoot(table)) {
            String parent = MysqlSchemaUtils.getParent(table);
            result.putIfAbsent(parent, new HashMap<>());
            boolean rewrited = false;
            for (Map<String, Object> column : columns) {
                if (MysqlSchemaUtils.hasReference(column)) {
                    if (parent.equalsIgnoreCase(MysqlSchemaUtils.getReference(column))) {
                        boolean lazy = MysqlSchemaUtils.isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                        result.get(parent).putIfAbsent(tableName, "OneToMany;" + MysqlSchemaUtils.getColumnName(column) + (lazy ? ";LAZY" : ""));
                        rewrited = true;
                    }
                }
            }
            if (!rewrited) {
                Map<String, Object> column = columns.stream().filter(c -> MysqlSchemaUtils.getColumnName(c).equals(parent + "_id")).findFirst().orElseGet(() -> null);
                if (column != null) {
                    boolean lazy = MysqlSchemaUtils.isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                    result.get(parent).putIfAbsent(tableName, "OneToMany;" + parent + "_id" + (lazy ? ";LAZY" : ""));
                }
            }
        }

        // 聚合之间关系
        if (MysqlSchemaUtils.hasRelation(table)) {
            // ManyToMany
            String owner = "";
            String beowned = "";
            String joinCol = "";
            String inverseJoinColumn = "";
            boolean ownerLazy = false;
            for (Map<String, Object> column : columns) {
                if (MysqlSchemaUtils.hasReference(column)) {
                    String refTableName = MysqlSchemaUtils.getReference(column);
                    result.putIfAbsent(refTableName, new HashMap<>());
                    boolean lazy = MysqlSchemaUtils.isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                    if (StringUtils.isBlank(owner)) {
                        ownerLazy = lazy;
                        owner = refTableName;
                        joinCol = MysqlSchemaUtils.getColumnName(column);
                    } else {
                        beowned = refTableName;
                        inverseJoinColumn = MysqlSchemaUtils.getColumnName(column);
                        result.get(beowned).putIfAbsent(owner, "*ManyToMany;" + inverseJoinColumn + (lazy ? ";LAZY" : ""));
                    }
                }
            }
            result.get(owner).putIfAbsent(beowned, "ManyToMany;" + joinCol + ";" + inverseJoinColumn + ";" + tableName + (ownerLazy ? ";LAZY" : ""));
        }

        for (Map<String, Object> column : columns) {
            String colRel = MysqlSchemaUtils.getRelation(column);
            String colName = MysqlSchemaUtils.getColumnName(column);
            String refTableName = null;
            if (StringUtils.isNotBlank(colRel) || MysqlSchemaUtils.hasRelation(column)) {
                switch (colRel) {
                    case "OneToOne":
                    case "1:1":
                        refTableName = MysqlSchemaUtils.getReference(column);
                        result.putIfAbsent(tableName, new HashMap<>());
                        result.get(tableName).putIfAbsent(refTableName, "OneToOne;" + colName);
                        result.putIfAbsent(refTableName, new HashMap<>());
                        result.get(refTableName).putIfAbsent(tableName, "*OneToOne;" + colName);
                        break;
                    case "ManyToOne":
                    case "*:1":
                    default:
                        refTableName = MysqlSchemaUtils.getReference(column);
                        result.putIfAbsent(tableName, new HashMap<>());
                        result.get(tableName).putIfAbsent(refTableName, "ManyToOne;" + colName);
                        result.putIfAbsent(refTableName, new HashMap<>());
                        result.get(refTableName).putIfAbsent(tableName, "*OneToMany;" + colName);
                        break;
                }
            }
        }
        return result;
    }

    public String getColumnDefaultJavaLiteral(Map<String, Object> column) {
        String columnDefault = column.get("COLUMN_DEFAULT") == null ? null : column.get("COLUMN_DEFAULT").toString();
        switch (getColumnJavaType(column)) {
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

    public boolean readCustomerSourceFile(String filePath, List<String> importLines, List<String> annotationLines, List<String> customerLines) throws IOException {
        if (FileUtils.fileExists(filePath)) {
            String simpleClassName = SourceFileUtils.resolveSimpleClassName(filePath);
            String content = FileUtils.fileRead(filePath);
            List<String> lines = Arrays.asList(content.replace("\r\n", "\n").split("\n"));

            int startMapperLine = 0;
            int endMapperLine = 0;
            int startClassLine = 0;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("【字段映射开始】")) {
                    startMapperLine = i;
                } else if (line.contains("【字段映射结束】")) {
                    endMapperLine = i;
                } else if (line.trim().startsWith("public") && startClassLine == 0) {
                    startClassLine = i;
                } else if ((line.trim().startsWith("@") || annotationLines.size() > 0) && startClassLine == 0) {
                    annotationLines.add(line);
                    getLog().debug("a " + line);
                } else if ((annotationLines.size() == 0 && startClassLine == 0)) {
                    importLines.add(line);
                    getLog().debug("i " + line);
                } else if (startClassLine > 0 &&
                        (startMapperLine == 0 || endMapperLine > 0)
                ) {
                    customerLines.add(line);
                }
            }
            for (int i = customerLines.size() - 1; i >= 0; i--) {
                String line = customerLines.get(i);
                if (line.contains("}")) {
                    customerLines.remove(i);
                    if (!line.equalsIgnoreCase("}")) {
                        customerLines.add(i, line.substring(0, line.lastIndexOf("}")));
                    }
                    break;
                }
                customerLines.remove(i);
            }
            customerLines.forEach(l -> getLog().debug("c " + l));
            if (startMapperLine == 0 || endMapperLine == 0) {
                return false;
            }
            FileUtils.fileDelete(filePath);
        }
        return true;
    }

    public void processImportLines(Map<String, Object> table, String basePackage, List<String> importLines) {
        boolean importEmpty = importLines.size() == 0;
        if (importEmpty) {
            importLines.add("");
        }
        List<String> importNamespaces = Arrays.asList(
                "lombok.AllArgsConstructor",
                "lombok.Builder",
                "lombok.Getter",
                "lombok.NoArgsConstructor",
                "org.hibernate.annotations.GenericGenerator",
                "org.hibernate.annotations.DynamicInsert",
                "org.hibernate.annotations.DynamicUpdate",
                "org.hibernate.annotations.Fetch",
                "org.hibernate.annotations.FetchMode",
                "org.hibernate.annotations.SQLDelete",
                "org.hibernate.annotations.Where",
                "javax.persistence.*"
        );

        if (importEmpty) {
            for (String importNamespace : importNamespaces) {
                if (importNamespace.equalsIgnoreCase("javax.persistence.*")) {
                    importLines.add("");
                }
                importLines.add("import " + importNamespace + ";");
            }
            importLines.add("");
            importLines.add("/**");
            for (String comment : MysqlSchemaUtils.getComment(table).split("[\\r\\n]")) {
                if (StringUtils.isEmpty(comment)) {
                    continue;
                }
                importLines.add(" * " + comment);
            }
            importLines.add(" *");
            // importLines.add(" * " + MysqlSchemaUtils.getComment(table).replaceAll("[\\r\\n]", " "));
            importLines.add(" * 本文件由[gen-ddd-maven-plugin]生成");
            importLines.add(" * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明");
            importLines.add(" */");
        } else {
            for (String importNamespace : importNamespaces) {
                SourceFileUtils.addSortedIfNone(importLines,
                        "\\s*import\\s+" + importNamespace
                                .replace(".", "\\.")
                                .replace("*", "\\*") + "\\s*;",
                        "import " + importNamespace + ";");
            }
        }
    }

    public void processAnnotationLines(Map<String, Object> table, List<Map<String, Object>> columns, List<String> annotationLines) {
        String tableName = MysqlSchemaUtils.getTableName(table);
        boolean annotationEmpty = annotationLines.size() == 0;
        if (StringUtils.isNotBlank(aggregateRootAnnotation)) {
            String aggregateRootAnnotationSimpleClassName = aggregateRootAnnotation.substring(aggregateRootAnnotation.contains(".") ? aggregateRootAnnotation.lastIndexOf(".") + 1 : 0);
            if (MysqlSchemaUtils.isAggregateRoot(table)) {
                SourceFileUtils.addIfNone(annotationLines, "@[\\w\\.]*" + aggregateRootAnnotationSimpleClassName + "(\\(.*\\))?", "@" + aggregateRootAnnotation);
            } else {
                SourceFileUtils.removeText(annotationLines, "@" + aggregateRootAnnotation + "(\\(.*\\))?");
                SourceFileUtils.removeText(annotationLines, "@" + aggregateRootAnnotationSimpleClassName + "(\\(.*\\))?");
            }
        } else {
            if (MysqlSchemaUtils.isAggregateRoot(table)) {
                SourceFileUtils.addIfNone(annotationLines, "\\/\\* @AggregateRoot(\\(.*\\))? \\*\\/", "/* @AggregateRoot */");
            } else {
                SourceFileUtils.removeText(annotationLines, "\\/\\* @AggregateRoot(\\(.*\\))? \\*\\/");
            }
        }
        SourceFileUtils.addIfNone(annotationLines, "@Entity(\\(.*\\))?", "@Entity");
        SourceFileUtils.addIfNone(annotationLines, "@Table(\\(.*\\))?", "@Table(name = \"`" + tableName + "`\")");
        SourceFileUtils.addIfNone(annotationLines, "@DynamicInsert(\\(.*\\))?", "@DynamicInsert");
        SourceFileUtils.addIfNone(annotationLines, "@DynamicUpdate(\\(.*\\))?", "@DynamicUpdate");
        if (StringUtils.isNotBlank(deletedField) && MysqlSchemaUtils.hasColumn(deletedField, columns)) {
            if (MysqlSchemaUtils.hasColumn(versionField, columns)) {
                SourceFileUtils.addIfNone(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update `" + tableName + "` set `" + deletedField + "` = 1 where " + idField + " = ? and `" + versionField + "` = ? \")");
            } else {
                SourceFileUtils.addIfNone(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update `" + tableName + "` set `" + deletedField + "` = 1 where " + idField + " = ? \")");
            }
            if (MysqlSchemaUtils.hasColumn(versionField, columns) && !SourceFileUtils.hasLine(annotationLines, "@SQLDelete(\\(.*" + versionField + ".*\\))")) {
                SourceFileUtils.replaceText(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update `" + tableName + "` set `" + deletedField + "` = 1 where " + idField + " = ? and `" + versionField + "` = ? \")");
            }
            SourceFileUtils.addIfNone(annotationLines, "@Where(\\(.*\\))?", "@Where(clause = \"`" + deletedField + "` = 0\")");
        }
        if (annotationEmpty) {
            annotationLines.add("");
        }
        SourceFileUtils.addIfNone(annotationLines, "@AllArgsConstructor(\\(.*\\))?", "@AllArgsConstructor");
        SourceFileUtils.addIfNone(annotationLines, "@NoArgsConstructor(\\(.*\\))?", "@NoArgsConstructor");
        SourceFileUtils.replaceText(annotationLines, "@Builder(\\(.*\\))?", "@Builder");
        SourceFileUtils.addIfNone(annotationLines, "@Builder(\\(.*\\))?", "@Builder");
        SourceFileUtils.distinctText(annotationLines, "@Builder(\\(.*\\))?");
        SourceFileUtils.addIfNone(annotationLines, "@Getter(\\(.*\\))?", "@Getter");
        SourceFileUtils.removeText(annotationLines, "@Setter(\\(.*\\))?");
        SourceFileUtils.removeText(annotationLines, "@Data(\\(.*\\))?");
        SourceFileUtils.removeText(annotationLines, "@lombok\\.Setter(\\(.*\\))?");
        SourceFileUtils.removeText(annotationLines, "@lombok\\.Data(\\(.*\\))?");
    }

    public String resolvePackage(Map<String, Object> table, String basePackage, String baseDir) {
        String tableName = MysqlSchemaUtils.getTableName(table);
        String simpleClassName = getEntityJavaType(tableName);
        String packageName = (basePackage + "." + getEntityPackage(tableName));

        Optional<File> existFilePath = SourceFileUtils.findJavaFileBySimpleClassName(baseDir, simpleClassName);
        if (existFilePath.isPresent()) {
            packageName = SourceFileUtils.resolvePackage(existFilePath.get().getAbsolutePath());
        }
        return packageName;
    }

    public void writeEntitySourceFile(Map<String, Object> table, List<Map<String, Object>> columns, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations, String basePackage, String baseDir) throws IOException {
        String tableName = MysqlSchemaUtils.getTableName(table);
        if (isIgnoreTable(table)) {
            getLog().info("跳过忽略表：" + tableName);
            return;
        }
        if (MysqlSchemaUtils.hasRelation(table)
            // &&"ManyToMany".equalsIgnoreCase(MysqlSchemaUtils.getRelation(table))
        ) {
            getLog().info("跳过关系表：" + tableName);
            return;
        }

        String simpleClassName = getEntityJavaType(tableName);
        String packageName = tablePackageMap.get(tableName);

        new File(SourceFileUtils.resolveDirectory(baseDir, packageName)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, packageName, simpleClassName);

        List<String> enums = new ArrayList<>();
        List<String> importLines = new ArrayList<>();
        List<String> annotationLines = new ArrayList<>();
        List<String> customerLines = new ArrayList<>();
        if (!readCustomerSourceFile(filePath, importLines, annotationLines, customerLines)) {
            getLog().warn("文件被改动，无法自动更新！" + filePath);
            return;
        }
        processImportLines(table, basePackage, importLines);
        processAnnotationLines(table, columns, annotationLines);

        getLog().info("开始生成实体文件：" + filePath);
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        writeLine(out, "package " + packageName + ";");
        importLines.forEach(line -> writeLine(out, line));
        annotationLines.forEach(line -> writeLine(out, line));
        writeLine(out, "public class " + simpleClassName + (StringUtils.isNotBlank(entityBaseClass) ? " extends " + entityBaseClass : "") + " {");
        if (customerLines.size() > 0) {
            customerLines.forEach(line -> writeLine(out, line));
        } else {
            writeLine(out, "");
            writeLine(out, "    // 【行为方法开始】");
            writeLine(out, "");
            writeLine(out, "");
            writeLine(out, "");
            writeLine(out, "    // 【行为方法结束】");
            writeLine(out, "");
            writeLine(out, "");
            writeLine(out, "");
        }
        writeLine(out, "    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动");
        writeLine(out, "");
        writeLine(out, "    @Id");
        if (MysqlSchemaUtils.hasIdGenerator(table)) {
            writeLine(out, "    @GeneratedValue(generator = \"" + MysqlSchemaUtils.getIdGenerator(table) + "\")");
            writeLine(out, "    @GenericGenerator(name = \"" + MysqlSchemaUtils.getIdGenerator(table) + "\", strategy = \"" + MysqlSchemaUtils.getIdGenerator(table) + "\")");
        } else if (StringUtils.isNotBlank(idGenerator)) {
            writeLine(out, "    @GeneratedValue(generator = \"" + idGenerator + "\")");
            writeLine(out, "    @GenericGenerator(name = \"" + idGenerator + "\", strategy = \"" + idGenerator + "\")");
        } else {
            writeLine(out, "    @GeneratedValue(strategy = GenerationType.IDENTITY)");
        }
        writeLine(out, "    @Column(name = \"`" + idField + "`\")");
        writeLine(out, "    Long " + idField + ";");
        writeLine(out, "");
        for (Map<String, Object> column : columns) {
            writeColumnProperty(out, table, column, relations, enums);
        }
        writeRelationProperty(out, table, relations, tablePackageMap);
        if (MysqlSchemaUtils.hasColumn(versionField, columns)) {
            writeLine(out, "");
            writeLine(out, "    /**");
            writeLine(out, "     * 数据版本（支持乐观锁）");
            writeLine(out, "     */");
            writeLine(out, "    @Version");
            writeLine(out, "    @Column(name = \"`" + versionField + "`\")");
            if (generateDefault) {
                writeLine(out, "    @Builder.Default");
                writeLine(out, "    Integer " + toLowerCamelCase(versionField) + " = 0;");
            } else {
                writeLine(out, "    Integer " + toLowerCamelCase(versionField) + ";");
            }
        }
        writeLine(out, "");
        writeLine(out, "    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动");
        writeLine(out, "}");
        writeLine(out, "");
        out.close();
        if (generateSchema) {
            writeSchemaSourceFile(table, columns, tablePackageMap, relations, basePackage, baseDir.replace("-domain", "-application"));
        }
    }

    public boolean needGenerateField(Map<String, Object> table, Map<String, Object> column, Map<String, Map<String, String>> relations) {

        String tableName = MysqlSchemaUtils.getTableName(table);
        String columnName = MysqlSchemaUtils.getColumnName(column);
        if (isIgnoreColumn(column)) {
            return false;
        }
        if (isReservedColumn(column)) {
            return false;
        }

        if (!MysqlSchemaUtils.isAggregateRoot(table)) {
            if (columnName.equalsIgnoreCase(MysqlSchemaUtils.getParent(table) + "_id")) {
                return false;
            }
        }

        if (relations.containsKey(tableName)) {
            boolean skip = false;
            for (Map.Entry<String, String> entry : relations.get(tableName).entrySet()) {
                String[] refInfos = entry.getValue().split(";");
                if (("ManyToOne".equalsIgnoreCase(refInfos[0]) || "OneToOne".equalsIgnoreCase(refInfos[0])) && columnName.equalsIgnoreCase(refInfos[1])) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                return false;
            }
        }
        return true;
    }

    public void writeEntityBuilderSourceFile(String basePackage, String baseDir, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations) throws IOException {
        String packageName = basePackage + "." + entityMetaInfoClassOutputPackage;

        new File(SourceFileUtils.resolveDirectory(baseDir, packageName)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, packageName, "EntityBuilder");

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        writeLine(out, "package " + packageName + ";");
        for (Map.Entry<String, Map<String, Object>> tableEntry : TableMap.entrySet()) {
            Map<String, Object> table = tableEntry.getValue();
            if (isIgnoreTable(table)) {
                continue;
            }
            if (MysqlSchemaUtils.hasRelation(table)) {
                continue;
            }
            String tableName = MysqlSchemaUtils.getTableName(table);
            String simpleClassName = getEntityJavaType(tableName);
            writeLine(out, "import " + tablePackageMap.get(tableName) + "." + simpleClassName + ";");
        }
        writeLine(out, "");
        writeLine(out, "/**");
        writeLine(out, " * 本文件由[gen-ddd-maven-plugin]生成");
        writeLine(out, " * 警告：请勿手工修改该文件，重新生成会覆盖该文件");
        writeLine(out, " */");
        writeLine(out, "public class EntityBuilder {");
        for (Map.Entry<String, Map<String, Object>> tableEntry : TableMap.entrySet()) {
            Map<String, Object> table = tableEntry.getValue();
            if (isIgnoreTable(table)) {
                continue;
            }
            if (MysqlSchemaUtils.hasRelation(table)) {
                continue;
            }
            writeBuildEntityMethod(out, table, ColumnsMap.get(tableEntry.getKey()), relations);
        }
        writeLine(out, "");
        writeLine(out, "}");
        out.flush();
        out.close();
    }

    public void writeBuildEntityMethod(BufferedWriter out, Map<String, Object> table, List<Map<String, Object>> columns, Map<String, Map<String, String>> relations) {

        String tableName = MysqlSchemaUtils.getTableName(table);
        String simpleClassName = getEntityJavaType(tableName);
        writeLine(out, "");
        writeLine(out, "    /**");
        for (String comment : MysqlSchemaUtils.getComment(table).split("[\\r\\n]")) {
            if (StringUtils.isEmpty(comment)) {
                continue;
            }
            writeLine(out, " * " + comment);
        }
        writeLine(out, "     * @param fieldFillNull 字段默认值是否为空");
        writeLine(out, "     * @return");
        writeLine(out, "     */");
        writeLine(out, "    public static " + simpleClassName + "." + simpleClassName + "Builder build" + simpleClassName + "(boolean fieldFillNull) {");
        writeLine(out, "        return " + simpleClassName + ".builder()");
        writeLine(out, "                ." + idField + "(null)");
        for (Map<String, Object> column : columns) {
            if (!needGenerateField(table, column, relations)) {
                continue;
            }
            String defaultJavaLiteral = getColumnDefaultJavaLiteral(column);
            if (StringUtils.isBlank(defaultJavaLiteral)) {
                defaultJavaLiteral = "null";
            }
            if ("Byte".equalsIgnoreCase(getColumnJavaType(column))) {
                defaultJavaLiteral = "(byte)" + defaultJavaLiteral;
            }
            writeLine(out, "                ." + toLowerCamelCase(MysqlSchemaUtils.getColumnName(column)) + "(fieldFillNull ? null : " + defaultJavaLiteral + ")");

        }
        if (MysqlSchemaUtils.hasColumn(versionField, columns)) {
            writeLine(out, "                ." + toLowerCamelCase(versionField) + "(fieldFillNull ? null : 0)");
        }
        writeLine(out, "                ;");
        writeLine(out, "    }");
    }

    public void writeColumnProperty(BufferedWriter out, Map<String, Object> table, Map<String, Object> column, Map<String, Map<String, String>> relations, List<String> enums) {
        String columnName = MysqlSchemaUtils.getColumnName(column);
        String columnJavaType = getColumnJavaType(column);

        if (!needGenerateField(table, column, relations)) {
            return;
        }

        boolean updatable = true;
        boolean insertable = true;
        if (getColumnJavaType(column).contains("Date")) {
            String extra = column.get("EXTRA") == null ? "" : column.get("EXTRA").toString();
            if ("on update CURRENT_TIMESTAMP".equalsIgnoreCase(extra)) {
                updatable = false;
            }
            String defaultData = column.get("COLUMN_DEFAULT") == null ? "" : column.get("COLUMN_DEFAULT").toString();
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultData)) {
                insertable = true;
            }
        }
        if (isReadOnlyColumn(column)) {
            insertable = false;
            updatable = false;
        }
        if (MysqlSchemaUtils.hasIgnoreInsert(column)) {
            insertable = false;
        }
        if (MysqlSchemaUtils.hasIgnoreUpdate(column)) {
            updatable = false;
        }

        writeLine(out, "");
        writeColumnComment(out, column);
        if (MysqlSchemaUtils.hasEnum(column)) {
            enums.add(columnJavaType);
            writeLine(out, "    @Convert(converter = " + columnJavaType + ".Converter.class)");
        }
        if (!updatable || !insertable) {
            writeLine(out, "    @Column(name = \"`" + columnName + "`\", insertable = " + (insertable ? "true" : "false") + ", updatable = " + (updatable ? "true" : "false") + ")");
        } else {
            writeLine(out, "    @Column(name = \"`" + columnName + "`\")");
        }
        if (generateDefault) {
            String defaultJavaLiteral = getColumnDefaultJavaLiteral(column);
            if (StringUtils.isNotBlank(defaultJavaLiteral)) {
                writeLine(out, "    @Builder.Default");
            }
            writeLine(out, "    " + columnJavaType + " " + toLowerCamelCase(columnName) + (StringUtils.isNotBlank(defaultJavaLiteral) ? " = " + defaultJavaLiteral : "") + ";");
        } else {
            writeLine(out, "    " + columnJavaType + " " + toLowerCamelCase(columnName) + ";");
        }
    }

    public void writeColumnComment(BufferedWriter out, Map<String, Object> column) {
        String columnJavaType = getColumnJavaType(column);
        writeLine(out, "    /**");
        for (String comment : MysqlSchemaUtils.getComment(column).split("[\\r\\n]")) {
            if (StringUtils.isEmpty(comment)) {
                continue;
            }
            writeLine(out, "     * " + comment);
            if (MysqlSchemaUtils.hasEnum(column)) {
                getLog().info("获取枚举java类型=" + columnJavaType);
                Map<Integer, String[]> enumMap = EnumConfigMap.get(columnJavaType);
                if (enumMap == null) {
                    enumMap = EnumConfigMap.get(MysqlSchemaUtils.getType(column));
                }
                if (enumMap != null) {
                    writeLine(out, "     * " + String.join(";", enumMap.entrySet().stream()
                            .map(c -> c.getKey() + ":" + c.getValue()[0] + ":" + c.getValue()[1]).collect(Collectors.toList())));
                }
            }
        }
        if (generateDbType) {
            writeLine(out, "     * " + MysqlSchemaUtils.getColumnDbType(column));
        }
        writeLine(out, "     */");
    }

    public void writeRelationProperty(BufferedWriter out, Map<String, Object> table, Map<String, Map<String, String>> relations, Map<String, String> tablePackageMap) {
        String tableName = MysqlSchemaUtils.getTableName(table);
        if (relations.containsKey(tableName)) {
            for (Map.Entry<String, String> entry : relations.get(tableName).entrySet()) {
                String[] refInfos = entry.getValue().split(";");
                String fetchType;
                Map<String, Object> navTable = TableMap.get(entry.getKey());
                if (entry.getValue().endsWith(";LAZY")) {
                    fetchType = "LAZY";
                } else {
                    fetchType = "EAGER";
                }
                if (MysqlSchemaUtils.hasLazy(navTable)) {
                    fetchType = MysqlSchemaUtils.isLazy(navTable) ? "LAZY" : "EAGER";
                    getLog().warn(tableName + ":" + entry.getKey() + ":" + fetchType);
                }
                if ("ManyToOne".equals(refInfos[0])) {
                    continue;
                }
                writeLine(out, "");
                if (fetchType.equals("LAZY")) {
                    switch (refInfos[0]) {
                        case "OneToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\", nullable = false)");
                            boolean countIsOne = MysqlSchemaUtils.countIsOne(navTable);
                            if (countIsOne) {
                                writeLine(out, "    @Getter(lombok.AccessLevel.PROTECTED)");
                            }
                            String fieldName = Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey())));
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + fieldName + ";");
                            if (countIsOne) {
                                writeLine(out, "");
                                writeLine(out, "    public " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " get" + getEntityJavaType(entry.getKey()) + "() {\n" +
                                        "        return " + fieldName + " == null || " + fieldName + ".size() == 0 ? null : " + fieldName + ".get(0);\n" +
                                        "    }");
                            }
                            break;
                        case "ManyToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\")");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "OneToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\")");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "*OneToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        case "*OneToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "ManyToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    @JoinTable(name = \"`" + refInfos[3] + "`\", joinColumns = {@JoinColumn(name = \"`" + refInfos[1] + "`\")}, inverseJoinColumns = {@JoinColumn(name = \"`" + refInfos[2] + "`\")})");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        case "*ManyToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(tableName))) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        default:

                            break;
                    }
                } else if (fetchType.equals("EAGER")) {
                    String fetchMode = this.fetchMode;
                    switch (refInfos[0]) {
                        case "OneToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\", nullable = false)");
                            boolean countIsOne = MysqlSchemaUtils.countIsOne(navTable);
                            if (countIsOne) {
                                writeLine(out, "    @Getter(lombok.AccessLevel.PROTECTED)");
                            }
                            String fieldName = Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey())));
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + fieldName + ";");
                            if (countIsOne) {
                                writeLine(out, "");
                                writeLine(out, "    public " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " get" + getEntityJavaType(entry.getKey()) + "() {\n" +
                                        "        return " + fieldName + " == null || " + fieldName + ".size() == 0 ? null : " + fieldName + ".get(0);\n" +
                                        "    }");
                            }
                            break;
                        case "ManyToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\")");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "OneToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    @JoinColumn(name = \"`" + refInfos[1] + "`\")");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "*OneToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        case "*OneToOne":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                            break;
                        case "ManyToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    @JoinTable(name = \"`" + refInfos[3] + "`\", joinColumns = {@JoinColumn(name = \"`" + refInfos[1] + "`\")}, inverseJoinColumns = {@JoinColumn(name = \"`" + refInfos[2] + "`\")})");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        case "*ManyToMany":
                            writeLine(out, "    @" + refInfos[0].replace("*", "") + "(mappedBy = \"" + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(tableName))) + "\", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER) @Fetch(FetchMode." + fetchMode + ")");
                            writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                            break;
                        default:

                            break;
                    }
                }
            }
        }
    }

    public void writeEnumSourceFile(Map<Integer, String[]> enumConfigs, String enumType, String enumPackage, String baseDir, String enumValueField, String enumNameField) throws IOException {
        new File(SourceFileUtils.resolveDirectory(baseDir, enumPackage)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, enumPackage, enumType);

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        getLog().info("开始生成枚举文件：" + filePath);

        writeLine(out, "package " + enumPackage + ";");
        writeLine(out, "");
        writeLine(out, "import lombok.Getter;");
        writeLine(out, "");
        writeLine(out, "import javax.persistence.*;");
        writeLine(out, "import java.util.HashMap;");
        writeLine(out, "import java.util.Map;");
        writeLine(out, "");
        writeLine(out, "/**");
        writeLine(out, " * 本文件由[gen-ddd-maven-plugin]生成");
        writeLine(out, " * 警告：请勿手工修改该文件，重新生成会覆盖该文件");
        writeLine(out, " */");
        writeLine(out, "public enum " + enumType + " {");
        writeLine(out, "");
        for (Map.Entry<Integer, String[]> entry : enumConfigs.entrySet()) {
            getLog().info(entry.getValue()[0] + " = " + entry.getKey() + " : " + entry.getValue()[1]);
            writeLine(out, "    /**");
            writeLine(out, "     * " + entry.getValue()[1]);
            writeLine(out, "     */");
            writeLine(out, "    " + entry.getValue()[0] + "(" + entry.getKey() + ", \"" + entry.getValue()[1] + "\"),");
        }
        writeLine(out, ";");
        writeLine(out, "    @Getter");
        writeLine(out, "    private int " + enumValueField + ";");
        writeLine(out, "    @Getter");
        writeLine(out, "    private String " + enumNameField + ";");
        writeLine(out, "");
        writeLine(out, "    " + enumType + "(Integer " + enumValueField + ", String " + enumNameField + "){");
        writeLine(out, "        this." + enumValueField + " = " + enumValueField + ";");
        writeLine(out, "        this." + enumNameField + " = " + enumNameField + ";");
        writeLine(out, "    }");
        writeLine(out, "");
        writeLine(out, "" +
                "    private static Map<Integer, " + enumType + "> enums = null;\n" +
                "    public static " + enumType + " valueOf(Integer " + enumValueField + ") {\n" +
                "        if(enums == null) {\n" +
                "            enums = new HashMap<>();\n" +
                "            for (" + enumType + " val : " + enumType + ".values()) {\n" +
                "                enums.put(val." + enumValueField + ", val);\n" +
                "            }\n" +
                "        }\n" +
                "        if(enums.containsKey(" + enumValueField + ")){\n" +
                "            return enums.get(" + enumValueField + ");\n" +
                "        }\n" +
                (this.enumUnmatchedThrowException
                        ? "        throw new RuntimeException(\"枚举类型" + enumType + "枚举值转换异常，不存在的值\" + " + enumValueField + ");\n"
                        : "        return null;") +
                "    }");
        writeLine(out, "");
        writeLine(out, "    /**");
        writeLine(out, "     * JPA转换器");
        writeLine(out, "     */");
        writeLine(out, "    public static class Converter implements AttributeConverter<" + enumType + ", Integer>{");
        writeLine(out, "" +
                "        @Override\n" +
                "        public Integer convertToDatabaseColumn(" + enumType + "  val) {\n" +
                "            return val." + enumValueField + ";\n" +
                "        }\n" +
                "\n" +
                "        @Override\n" +
                "        public " + enumType + " convertToEntityAttribute(Integer " + enumValueField + ") {\n" +
                "            return " + enumType + ".valueOf(" + enumValueField + ");\n" +
                "        }");
        writeLine(out, "    }");
        writeLine(out, "}");
        writeLine(out, "");
        out.flush();
        out.close();
    }

    public void writeSchemaSourceFile(Map<String, Object> table, List<Map<String, Object>> columns, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations, String basePackage, String baseDir) throws IOException {
        String tableName = MysqlSchemaUtils.getTableName(table);
        String packageName = null;
        if ("abs".equalsIgnoreCase(entityMetaInfoClassOutputMode)) {
            packageName = basePackage + "." + entityMetaInfoClassOutputPackage + ".schemas";
        } else {
            packageName = tablePackageMap.get(tableName) + ".schemas";
        }
        String simpleClassName = getEntityJavaType(tableName);

        new File(SourceFileUtils.resolveDirectory(baseDir, packageName)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, packageName, simpleClassName + "Schema");

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        getLog().info("开始生成Schema文件：" + filePath);

        writeLine(out, "package " + packageName + ";");
        writeLine(out, "\n" +
                "import " + basePackage + "." + entityMetaInfoClassOutputPackage + ".Schema;\n" +
                "import " + tablePackageMap.get(tableName) + "." + simpleClassName + ";\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.apache.commons.collections4.CollectionUtils;\n" +
                "import org.springframework.data.domain.Sort;\n" +
                "import org.springframework.data.jpa.domain.Specification;\n" +
                "\n" +
                "import javax.persistence.criteria.*;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Collection;\n" +
                "import java.util.stream.Collectors;");
        writeLine(out, "");
        writeLine(out, "/**");
        writeLine(out, " * " + MysqlSchemaUtils.getComment(table).replaceAll("[\\r\\n]", " "));
        writeLine(out, " * 本文件由[gen-ddd-maven-plugin]生成");
        writeLine(out, " * 警告：请勿手工修改该文件，重新生成会覆盖该文件");
        writeLine(out, " */");
        writeLine(out, "@RequiredArgsConstructor");
        writeLine(out, "public class " + simpleClassName + "Schema {");
        writeLine(out, "    private final Path<" + simpleClassName + "> root;");
        writeLine(out, "    private final CriteriaBuilder criteriaBuilder;\n" +
                "\n" +
                "    public CriteriaBuilder criteriaBuilder() {\n" +
                "        return criteriaBuilder;\n" +
                "    }");
        writeLine(out, "\n" +
                "    public Schema.Field<Long> " + idField + "() {\n" +
                "        return root == null ? new Schema.Field<>(\"" + idField + "\") : new Schema.Field<>(root.get(\"" + idField + "\"));\n" +
                "    }");
        for (Map<String, Object> column : columns) {
            if (!needGenerateField(table, column, relations)) {
                continue;
            }
            writeLine(out, "");
            writeColumnComment(out, column);
            writeLine(out, "    public Schema.Field<" + getColumnJavaType(column) + "> " + toLowerCamelCase(MysqlSchemaUtils.getColumnName(column)) + "() {\n" +
                    "        return root == null ? new Schema.Field<>(\"" + toLowerCamelCase(MysqlSchemaUtils.getColumnName(column)) + "\") : new Schema.Field<>(root.get(\"" + toLowerCamelCase(MysqlSchemaUtils.getColumnName(column)) + "\"));\n" +
                    "    }");
        }
        writeLine(out, "");
        writeLine(out, "" +
                "    /**\n" +
                "     * 满足所有条件\n" +
                "     * @param restrictions\n" +
                "     * @return\n" +
                "     */\n" +
                "    public Predicate all(Predicate... restrictions) {\n" +
                "        return criteriaBuilder().and(restrictions);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 满足任一条件\n" +
                "     * @param restrictions\n" +
                "     * @return\n" +
                "     */\n" +
                "    public Predicate any(Predicate... restrictions) {\n" +
                "        return criteriaBuilder().or(restrictions);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 指定条件\n" +
                "     * @param builder\n" +
                "     * @return\n" +
                "     */\n" +
                "    public Predicate spec(Schema.PredicateBuilder<" + simpleClassName + "Schema> builder){\n" +
                "        return builder.build(this);\n" +
                "    }");
        writeJoinEntities(out, table, relations, tablePackageMap);
        writeLine(out, "");
        writeLine(out, "    /**\n" +
                "     * 构建查询条件\n" +
                "     * @param builder\n" +
                "     * @param distinct\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Specification<" + simpleClassName + "> specify(Schema.PredicateBuilder<" + simpleClassName + "Schema> builder, boolean distinct) {\n" +
                "        return (root, criteriaQuery, criteriaBuilder) -> {\n" +
                "            " + simpleClassName + "Schema " + toLowerCamelCase(simpleClassName) + " = new " + simpleClassName + "Schema(root, criteriaBuilder);\n" +
                "            criteriaQuery.where(builder.build(" + toLowerCamelCase(simpleClassName) + "));\n" +
                "            criteriaQuery.distinct(distinct);\n" +
                "            return null;\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 构建查询条件\n" +
                "     * @param builder\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Specification<" + simpleClassName + "> specify(Schema.PredicateBuilder<" + simpleClassName + "Schema> builder) {\n" +
                "        return (root, criteriaQuery, criteriaBuilder) -> {\n" +
                "            " + simpleClassName + "Schema " + toLowerCamelCase(simpleClassName) + " = new " + simpleClassName + "Schema(root, criteriaBuilder);\n" +
                "            criteriaQuery.where(builder.build(" + toLowerCamelCase(simpleClassName) + "));\n" +
                "            return null;\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 构建排序\n" +
                "     * @param builders\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Sort orderBy(Schema.OrderBuilder<" + simpleClassName + "Schema>... builders) {\n" +
                "        return orderBy(Arrays.asList(builders));\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 构建排序\n" +
                "     *\n" +
                "     * @param builders\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Sort orderBy(Collection<Schema.OrderBuilder<" + simpleClassName + "Schema>> builders) {\n" +
                "        if(CollectionUtils.isEmpty(builders)) {\n" +
                "            return Sort.unsorted();\n" +
                "        }\n" +
                "        return Sort.by(builders.stream()\n" +
                "                .map(builder -> builder.build(new " + simpleClassName + "Schema(null, null)))\n" +
                "                .collect(Collectors.toList())\n" +
                "        );\n" +
                "    }");
        writeLine(out, "");
        writeLine(out, "}");
        out.flush();
        out.close();
    }

    public void writeJoinEntities(BufferedWriter out, Map<String, Object> table, Map<String, Map<String, String>> relations, Map<String, String> tablePackageMap) {
        String tableName = MysqlSchemaUtils.getTableName(table);
        int count = 0;
        if (relations.containsKey(tableName)) {
            for (Map.Entry<String, String> entry : relations.get(tableName).entrySet()) {
                String[] refInfos = entry.getValue().split(";");
                switch (refInfos[0]) {
                    case "OneToMany":
                    case "*OneToMany":
                        writeLine(out,
                                "\n" +
                                        "    /**\n" +
                                        "     * " + getEntityJavaType(entry.getKey()) + " 关联查询条件定义\n" +
                                        "     *\n" +
                                        "     * @param joinType\n" +
                                        "     * @return\n" +
                                        "     */\n" +
                                        "    public " + getEntityJavaType(entry.getKey()) + "Schema join" + getEntityJavaType(entry.getKey()) + "(Schema.JoinType joinType) {\n" +
                                        "        JoinType type = transformJoinType(joinType);\n" +
                                        "        Join<" + getEntityJavaType(tableName) + ", " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> join = ((Root<" + getEntityJavaType(tableName) + ">) root).join(\"" + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + "\", type);\n" +
                                        "        " + getEntityJavaType(entry.getKey()) + "Schema schema = new " + getEntityJavaType(entry.getKey()) + "Schema(join, criteriaBuilder);\n" +
                                        "        return schema;\n" +
                                        "    }");
                        count++;
                        break;
                    default:
                        // 暂不支持
                        break;
                }

            }

            if (count > 0) {
                writeLine(out, "\n" +
                        "\n" +
                        "    private JoinType transformJoinType(Schema.JoinType joinType){\n" +
                        "        if(joinType == Schema.JoinType.INNER){\n" +
                        "            return JoinType.INNER;\n" +
                        "        } else if(joinType == Schema.JoinType.LEFT){\n" +
                        "            return JoinType.LEFT;\n" +
                        "        } else if(joinType == Schema.JoinType.RIGHT){\n" +
                        "            return JoinType.RIGHT;\n" +
                        "        }\n" +
                        "        return JoinType.LEFT;\n" +
                        "    }");
            }
        }
    }

    public void writeBaseSechemaSourceFile(String basePackage, String baseDir) throws IOException {
        String packageName = basePackage + "." + entityMetaInfoClassOutputPackage;
        String simpleClassName = "Schema";

        new File(SourceFileUtils.resolveDirectory(baseDir, packageName)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, packageName, simpleClassName);

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        writeLine(out, "package " + packageName + ";");
        writeLine(out, "\n" +
                "import com.google.common.collect.Lists;\n" +
                "import org.hibernate.query.criteria.internal.path.SingularAttributePath;\n" +
                "import org.springframework.data.domain.Sort;\n" +
                "\n" +
                "import javax.persistence.criteria.CriteriaBuilder;\n" +
                "import javax.persistence.criteria.Expression;\n" +
                "import javax.persistence.criteria.Path;\n" +
                "import javax.persistence.criteria.Predicate;\n" +
                "import java.util.Collection;\n" +
                "\n" +
                "/**\n" +
                " * Schema\n" +
                " *\n" +
                " * @author <template>\n" +
                " * @date \n" +
                " */\n" +
                "public class " + simpleClassName + " {\n" +
                "\n" +
                "    /**\n" +
                "     * 断言构建器\n" +
                "     */\n" +
                "    public static interface PredicateBuilder<S> {\n" +
                "        public Predicate build(S schema);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 排序构建器\n" +
                "     */\n" +
                "    public static interface OrderBuilder<S> {\n" +
                "        public Sort.Order build(S schema);\n" +
                "    }\n" +
                "\n" +
                "    public enum JoinType {\n" +
                "        INNER,\n" +
                "        LEFT,\n" +
                "        RIGHT\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 字段\n" +
                "     *\n" +
                "     * @param <T>\n" +
                "     */\n" +
                "    public static class Field<T> {\n" +
                "        private String name;\n" +
                "        private SingularAttributePath<T> path;\n" +
                "\n" +
                "        public Field(Path<T> path) {\n" +
                "            this.path = new SingularAttributePath<>(((SingularAttributePath<T>) path).criteriaBuilder(), ((SingularAttributePath<T>) path).getJavaType(), ((SingularAttributePath<T>) path).getPathSource(), ((SingularAttributePath<T>) path).getAttribute());\n" +
                "            this.name = this.path.getAttribute().getName();\n" +
                "        }\n" +
                "\n" +
                "        public Field(String name) {\n" +
                "            this.name = name;\n" +
                "        }\n" +
                "\n" +
                "        protected CriteriaBuilder criteriaBuilder() {\n" +
                "            return path == null ? null : path.criteriaBuilder();\n" +
                "        }\n" +
                "\n" +
                "        public Path<T> path(){\n" +
                "            return path;\n" +
                "        }\n" +
                "\n" +
                "        public Sort.Order asc() {\n" +
                "            return Sort.Order.asc(this.name);\n" +
                "        }\n" +
                "\n" +
                "        public Sort.Order desc() {\n" +
                "            return Sort.Order.desc(this.name);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate isTrue() {\n" +
                "            return criteriaBuilder().isTrue((Expression<Boolean>) this.path);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate isFalse() {\n" +
                "            return criteriaBuilder().isTrue((Expression<Boolean>) this.path);\n" +
                "\n" +
                "        }\n" +
                "\n" +
                "        public Predicate equal(Object val) {\n" +
                "            return criteriaBuilder().equal(this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate equal(Expression<?> val) {\n" +
                "            return criteriaBuilder().equal(this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notEqual(Object val) {\n" +
                "            return criteriaBuilder().notEqual(this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notEqual(Expression<?> val) {\n" +
                "            return criteriaBuilder().notEqual(this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate isNull() {\n" +
                "            return criteriaBuilder().isNull(this.path);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate isNotNull() {\n" +
                "            return criteriaBuilder().isNotNull(this.path);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate greaterThan(Y val) {\n" +
                "            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> val) {\n" +
                "            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Y val) {\n" +
                "            return criteriaBuilder().greaterThan((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> val) {\n" +
                "            return criteriaBuilder().greaterThanOrEqualTo((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lessThan(Y val) {\n" +
                "            return criteriaBuilder().lessThan((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> val) {\n" +
                "            return criteriaBuilder().lessThan((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Y val) {\n" +
                "            return criteriaBuilder().lessThanOrEqualTo((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> val) {\n" +
                "            return criteriaBuilder().lessThanOrEqualTo((Expression<Y>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate between(Y val1, Y val2) {\n" +
                "            return criteriaBuilder().between((Expression<Y>) this.path, val1, val2);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> val1, Expression<? extends Y> val2) {\n" +
                "            return criteriaBuilder().between((Expression<Y>) this.path, val1, val2);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate in(Object... vals) {\n" +
                "            return in(Lists.newArrayList(vals));\n" +
                "        }\n" +
                "\n" +
                "        public Predicate in(Collection<Object> vals) {\n" +
                "            CriteriaBuilder.In predicate = criteriaBuilder().in(this.path);\n" +
                "            for (Object o : vals) {\n" +
                "                predicate.value(o);\n" +
                "            }\n" +
                "            return predicate;\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notIn(Object... vals) {\n" +
                "            return notIn(Lists.newArrayList(vals));\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notIn(Collection<Object> vals) {\n" +
                "            return criteriaBuilder().not(in(vals));\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        public Predicate like(String val) {\n" +
                "            return criteriaBuilder().like((Expression<String>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate like(Expression<String> val) {\n" +
                "            return criteriaBuilder().like((Expression<String>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notLike(String val) {\n" +
                "            return criteriaBuilder().notLike((Expression<String>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate notLike(Expression<String> val) {\n" +
                "            return criteriaBuilder().notLike((Expression<String>) this.path, val);\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        public Predicate eq(Object val) {\n" +
                "            return equal(val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate eq(Expression<?> val) {\n" +
                "            return equal(val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate neq(Object val) {\n" +
                "            return notEqual(val);\n" +
                "        }\n" +
                "\n" +
                "        public Predicate neq(Expression<?> val) {\n" +
                "            return notEqual(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate gt(Y val) {\n" +
                "            return greaterThan(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate gt(Expression<? extends Y> val) {\n" +
                "            return greaterThan(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate ge(Y val) {\n" +
                "            return greaterThanOrEqualTo(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate ge(Expression<? extends Y> val) {\n" +
                "\n" +
                "            return greaterThanOrEqualTo(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lt(Y val) {\n" +
                "\n" +
                "            return lessThan(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate lt(Expression<? extends Y> val) {\n" +
                "            return lessThan(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate le(Y val) {\n" +
                "            return lessThanOrEqualTo(val);\n" +
                "        }\n" +
                "\n" +
                "        public <Y extends Comparable<? super Y>> Predicate le(Expression<? extends Y> val) {\n" +
                "            return lessThanOrEqualTo(val);\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
        out.flush();
        out.close();
    }
}
