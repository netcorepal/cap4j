package org.netcorepal.cap4j.ddd.codegen;

import com.alibaba.fastjson.JSON;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.Inflector;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SqlSchemaUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.TextUtils;
import org.netcorepal.cap4j.ddd.codegen.template.PathNode;
import org.netcorepal.cap4j.ddd.codegen.template.TemplateNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils.*;
import static org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils.*;
import static org.netcorepal.cap4j.ddd.codegen.misc.SqlSchemaUtils.*;

/**
 * 生成实体
 *
 * @author binking338
 * @date 2022-02-16
 */
@Mojo(name = "gen-entity")
public class GenEntityMojo extends GenArchMojo {
    static final String DEFAULT_SCHEMA_PACKAGE = "meta";
    static final String DEFAULT_SPEC_PACKAGE = "specs";
    static final String DEFAULT_FAC_PACKAGE = "factory";
    static final String DEFAULT_ENUM_PACKAGE = "enums";
    static final String DEFAULT_DOMAIN_EVENT_PACKAGE = "events";
    static final String DEFAULT_SCHEMA_BASE_CLASS_NAME = "Schema";

    public Map<String, Map<String, Object>> TableMap = new HashMap<>();
    public Map<String, String> TableModuleMap = new HashMap<>();
    public Map<String, String> TableAggregateMap = new HashMap<>();
    public Map<String, List<Map<String, Object>>> ColumnsMap = new HashMap<>();
    public Map<String, Map<Integer, String[]>> EnumConfigMap = new HashMap<>();
    public Map<String, String> EnumPackageMap = new HashMap<>();
    public Map<String, String> EnumTableNameMap = new HashMap<>();
    public Map<String, String> EntityJavaTypeMap = new HashMap<>();
    /**
     * 注解缓存，注释为Key
     */
    public Map<String, Map<String, String>> AnnotaionsCache = new HashMap<>();

    public String dbType = "mysql";

    public String aggregatesPath = "";
    public String schemaPath = "";
    public String subscriberPath = "";

    public Map<String, List<TemplateNode>> templateNodeMap = new HashMap<>();


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.renderFileSwitch = false;
        super.execute();
        genEntity();
    }

    public String alias4Design(String name) {
        switch (name.toLowerCase()) {
            case "entity":
            case "aggregate":
            case "entities":
            case "aggregates":
                return "aggregate";
            case "schema":
            case "schemas":
                return "schema";
            case "enum":
            case "enums":
                return "enum";
            case "enumitem":
            case "enum_item":
                return "enum_item";
            case "factories":
            case "factory":
            case "fac":
                return "factory";
            case "specifications":
            case "specification":
            case "specs":
            case "spec":
            case "spe":
                return "specification";
            case "domain_events":
            case "domain_event":
            case "d_e":
            case "de":
                return "domain_event";
            case "domain_event_handlers":
            case "domain_event_handler":
            case "d_e_h":
            case "deh":
            case "domain_event_subscribers":
            case "domain_event_subscriber":
            case "d_e_s":
            case "des":
                return "domain_event_handler";
            case "domain_service":
            case "service":
            case "svc":
                return "domain_service";
            default:
                return name;
        }
    }

    @Override
    public void renderTemplate(List<TemplateNode> templateNodes, String parentPath) throws IOException {

        for (TemplateNode templateNode :
                templateNodes) {
            String alias = alias4Design(templateNode.getTag());
            if ("aggregate".equals(alias)) {
                aggregatesPath = parentPath;
            } else if ("schema_base".equals(alias)) {
                schemaPath = parentPath;
            } else if ("domain_event_handler".equals(alias)) {
                subscriberPath = parentPath;
            }
            if (!templateNodeMap.containsKey(alias)) {
                templateNodeMap.put(alias, new ArrayList<>());
            }
            templateNodeMap.get(alias).add(templateNode);
        }
    }

    public void genEntity() {
        SqlSchemaUtils.mojo = this;
        getLog().info("数据库连接：" + connectionString);
        getLog().info("数据库账号：" + user);
        getLog().info("数据库密码：" + pwd);
        getLog().info("数据库名称：" + schema);
        getLog().info("包含表：" + table);
        getLog().info("忽略表：" + ignoreTable);
        getLog().info("主键字段：" + idField);
        getLog().info("乐观锁字段：" + versionField);
        getLog().info("软删字段：" + deletedField);
        getLog().info("只读字段：" + readonlyFields);
        getLog().info("忽略字段：" + ignoreFields);
        getLog().info("");
        // 项目结构解析
        this.basePackage = StringUtils.isNotBlank(this.basePackage)
                ? this.basePackage
                : SourceFileUtils.resolveDefaultBasePackage(getDomainModulePath());
        getLog().info("实体基类：" + entityBaseClass);
        getLog().info("聚合根标注注解：" + getAggregateRootAnnotation());
        getLog().info("主键ID生成器：" + (StringUtils.isBlank(idGenerator) ? "自增" : idGenerator));
        getLog().info("日期类型映射Java包：" + datePackage4Java);
        getLog().info("枚举值Java字段名称：" + enumValueField);
        getLog().info("枚举名Java字段名称：" + enumNameField);
        getLog().info("枚举类型Jpa类型映射转换期不匹配是否抛出异常：" + enumUnmatchedThrowException);
        getLog().info("类型强制映射规则：");
        for (Map.Entry<String, String> entry :
                typeRemapping.entrySet()) {
            getLog().info("  " + entry.getKey() + " <-> " + entry.getValue());
        }
        getLog().info("生成Schema：" + (generateSchema ? "是" : "否"));
        if (generateSchema) {
            getLog().info("  输出模式：" + getEntitySchemaOutputMode());
            getLog().info("  输出路径：" + getEntitySchemaOutputPackage());
        }
        getLog().info("");
        getLog().info("");

        // 数据库解析
        this.dbType = recognizeDbType(connectionString);
        processSqlDialet(this.dbType);
        List<Map<String, Object>> tables = resolveTables(connectionString, user, pwd);
        List<Map<String, Object>> columns = resolveColumns(connectionString, user, pwd);
        Map<String, Map<String, String>> relations = new HashMap<>();
        Map<String, String> tablePackageMap = new HashMap<>();
        getLog().info("----------------解析数据库表----------------");
        getLog().info("");
        getLog().info("数据库类型：" + this.dbType);
        int maxTableNameLength = tables.stream()
                .map(table -> getTableName(table).length())
                .max(Comparator.naturalOrder())
                .orElse(20);
        for (Map<String, Object> table :
                tables) {
            List<Map<String, Object>> tableColumns = columns.stream().filter(column -> isColumnInTable(column, table))
                    .sorted(Comparator.comparingInt(SqlSchemaUtils::getOridinalPosition))
                    .collect(Collectors.toList());
            TableMap.put(getTableName(table), table);
            ColumnsMap.put(getTableName(table), tableColumns);
            getLog().info(String.format("%" + maxTableNameLength + "s   %s",
                    "",
                    getComment(table)));
            getLog().info(String.format("%" + maxTableNameLength + "s : (%s)",
                    getTableName(table),
                    String.join(", ", tableColumns.stream()
                            .map(column ->
                                    String.format(
                                            "%s %s",
                                            getColumnDbDataType(column),
                                            getColumnName(column)
                                    )).collect(Collectors.toList())
                    )
            ));
        }
        getLog().info("");
        getLog().info("");

        getLog().info("----------------开始字段扫描----------------");
        getLog().info("");
        for (Map<String, Object> table :
                TableMap.values()) {
            List<Map<String, Object>> tableColumns = ColumnsMap.get(getTableName(table));
            // 解析表关系
            getLog().info("开始解析表关系:" + getTableName(table));
            Map<String, Map<String, String>> relationTable = resolveRelationTable(table, tableColumns);
            for (Map.Entry<String, Map<String, String>> entry :
                    relationTable.entrySet()) {
                if (!relations.containsKey(entry.getKey())) {
                    relations.put(entry.getKey(), entry.getValue());
                } else {
                    relations.get(entry.getKey()).putAll(entry.getValue());
                }
            }
            tablePackageMap.put(getTableName(table), resolveEntityFullPackage(table, basePackage, getDomainModulePath()));
            getLog().info("结束解析表关系:" + getTableName(table));
            getLog().info("");
        }

        for (Map<String, Object> table :
                TableMap.values()) {
            if (isIgnoreTable(table)) {
                continue;
            }
            List<Map<String, Object>> tableColumns = ColumnsMap.get(getTableName(table));
            for (Map<String, Object> column :
                    tableColumns) {
                if (hasEnum(column) && !isIgnoreColumn(column)) {
                    Map<Integer, String[]> enumConfig = getEnum(column);
                    if (enumConfig.size() > 0) {
                        EnumConfigMap.put(getType(column), enumConfig);
                        String enumPackage = templateNodeMap.containsKey("enum") && templateNodeMap.get("enum").size() > 0
                                ? templateNodeMap.get("enum").get(0).getName()
                                : DEFAULT_ENUM_PACKAGE;
                        if (StringUtils.isNotBlank(enumPackage)) {
                            enumPackage = "." + enumPackage;
                        }
                        EnumPackageMap.put(getType(column), basePackage + "." + getEntityPackage(getTableName(table)) + enumPackage);
                        EnumTableNameMap.put(getType(column), getTableName(table));
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
                writeEnumSourceFile(
                        entry.getValue(),
                        entry.getKey(),
                        enumValueField,
                        enumNameField,
                        tablePackageMap,
                        getDomainModulePath());
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
                writeSchemaBaseSourceFile(getDomainModulePath());
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        for (Map<String, Object> table :
                TableMap.values()) {
            List<Map<String, Object>> tableColumns = ColumnsMap.get(getTableName(table));
            try {
                buildEntitySourceFile(table, tableColumns, tablePackageMap, relations, basePackage, getDomainModulePath());
            } catch (IOException e) {
                e.printStackTrace();
                getLog().error(e);
            }
        }
        getLog().info("----------------完成生成实体----------------");
        getLog().info("");
    }

    /**
     * 获取聚合根文件目录
     *
     * @return
     */
    public String getAggregatesPath() {
        if (StringUtils.isNotBlank(aggregatesPath)) {
            return aggregatesPath;
        }
        return resolveDirectory(
                getDomainModulePath(),
                basePackage + "." + AGGREGATE_PACKAGE
        );
    }

    /**
     * 获取聚合根包名，不包含basePackage
     *
     * @return
     */
    public String getAggregatesPackage() {
        return SourceFileUtils.resolvePackage(getAggregatesPath() + File.separator + "X.java")
                .substring(basePackage.length() + 1);
    }

    /**
     * 获取实体schema文件目录
     *
     * @return
     */
    public String getSchemaPath() {
        if (StringUtils.isNotBlank(schemaPath)) {
            return schemaPath;
        }
        return resolveDirectory(
                getDomainModulePath(),
                basePackage + "." + getEntitySchemaOutputPackage()
        );
    }

    /**
     * 获取schema包名，不包含basePackage
     *
     * @return
     */
    public String getSchemaPackage() {
        return SourceFileUtils.resolvePackage(getSchemaPath() + File.separator + "X.java")
                .substring(StringUtils.isBlank(basePackage) ? 0 : (basePackage.length() + 1));
    }

    /**
     * 获取领域事件订阅者文件目录
     *
     * @return
     */
    public String getSubscriberPath() {
        if (StringUtils.isNotBlank(subscriberPath)) {
            return subscriberPath;
        }
        return resolveDirectory(
                getApplicationModulePath(),
                basePackage + "." + DOMAIN_EVENT_SUBSCRIBER_PACKAGE
        );
    }

    /**
     * 获取领域事件订阅者包名，不包含basePackage
     *
     * @return
     */
    public String getSubscriberPackage() {
        return SourceFileUtils.resolvePackage(getSubscriberPath() + File.separator + "X.java")
                .substring(basePackage.length() + 1);
    }

    /**
     * 获取模块
     *
     * @param tableName
     * @return
     */
    public String getModule(String tableName) {
        return TableModuleMap.computeIfAbsent(tableName, tn -> {
            Map<String, Object> table = TableMap.get(tableName);
            String module = SqlSchemaUtils.getModule(table);
            getLog().info("尝试解析模块:" + getTableName(table) + " " + (StringUtils.isBlank(module) ? "[缺失]" : module));
            while (!isAggregateRoot(table) && StringUtils.isBlank(module)) {
                String parent = getParent(table);
                if (StringUtils.isBlank(parent)) {
                    break;
                }
                table = TableMap.get(parent);
                if (table == null) {
                    getLog().error("表 " + tableName + " @Parent 注解值填写错误，不存在表名为 " + parent + " 的表");
                }
                module = SqlSchemaUtils.getModule(table);
                getLog().info("尝试父表模块:" + getTableName(table) + " " + (StringUtils.isBlank(module) ? "[缺失]" : module));
            }
            getLog().info("模块解析结果:" + tableName + " " + (StringUtils.isBlank(module) ? "[无]" : module));
            return module;
        });
    }

    /**
     * 获取聚合
     *
     * @param tableName
     * @return
     */
    public String getAggregate(String tableName) {
        return TableAggregateMap.computeIfAbsent(tableName, tn -> {
            Map<String, Object> table = TableMap.get(tableName);
            String aggregate = SqlSchemaUtils.getAggregate(table);
            getLog().info("尝试解析聚合:" + getTableName(table) + " " + (StringUtils.isBlank(aggregate) ? "[缺失]" : aggregate));
            while (!isAggregateRoot(table) && StringUtils.isBlank(aggregate)) {
                String parent = getParent(table);
                if (StringUtils.isBlank(parent)) {
                    break;
                }
                table = TableMap.get(parent);
                aggregate = SqlSchemaUtils.getAggregate(table);
                getLog().info("尝试父表聚合:" + getTableName(table) + " " + (StringUtils.isBlank(aggregate) ? "[缺失]" : aggregate));
            }
            if (StringUtils.isBlank(aggregate)) {
                aggregate = toSnakeCase(getEntityJavaType(getTableName(table)));
            }
            getLog().info("聚合解析结果:" + tableName + " " + (StringUtils.isBlank(aggregate) ? "[缺失]" : aggregate));
            return aggregate;
        });
    }

    /**
     * 获取聚合名称
     * 格式: 模块.聚合
     *
     * @param tableName
     * @return
     */
    public String getAggregateWithModule(String tableName) {
        String module = getModule(tableName);
        if (StringUtils.isNotBlank(module)) {
            return SourceFileUtils.concatPackage(module, getAggregate(tableName));
        } else {
            return getAggregate(tableName);
        }
    }

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
        String type = getType(table);
        if (StringUtils.isBlank(type)) {
            type = toUpperCamelCase(tableName);
        }
        if (StringUtils.isNotBlank(type)) {
            getLog().info("解析实体类名:" + getTableName(table) + " --> " + type);
            EntityJavaTypeMap.put(tableName, type);
            return type;
        }
        throw new RuntimeException("实体类名未生成");
    }

    /**
     * 获取实体类所在包，不包含basePackage
     *
     * @param tableName
     * @return
     */
    public String getEntityPackage(String tableName) {
        String module = getModule(tableName);
        String aggregate = getAggregate(tableName);
        String packageName = SourceFileUtils.concatPackage(
                getAggregatesPackage()
                , module
                , aggregate.toLowerCase()
        );
        return packageName;
    }


    public boolean isReservedColumn(Map<String, Object> column) {
        String columnName = getColumnName(column).toLowerCase();
        boolean isReserved = versionField.equalsIgnoreCase(columnName);
        return isReserved;
    }

    public boolean isReadOnlyColumn(Map<String, Object> column) {
        if (hasReadOnly(column)) {
            return true;
        }
        String columnName = getColumnName(column).toLowerCase();
        if (StringUtils.isNotBlank(readonlyFields)
                && Arrays.stream(readonlyFields.toLowerCase().split(PATTERN_SPLITTER)).anyMatch(
                c -> columnName.matches(c.replace("%", ".*")))) {
            return true;
        }

        return false;
    }

    public boolean isIgnoreTable(Map<String, Object> table) {
        if (isIgnore(table)) {
            return true;
        }
        return false;
    }

    public boolean isIgnoreColumn(Map<String, Object> column) {
        if (isIgnore(column)) {
            return true;
        }
        String columnName = getColumnName(column).toLowerCase();
        if (StringUtils.isNotBlank(ignoreFields)
                && Arrays.stream(ignoreFields.toLowerCase().split(PATTERN_SPLITTER)).anyMatch(
                c -> columnName.matches(c.replace("%", ".*")))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否需要生成实体字段
     *
     * @param table
     * @param column
     * @param relations
     * @return
     */
    public boolean isColumnNeedGenerate(Map<String, Object> table, Map<String, Object> column, Map<String, Map<String, String>> relations) {

        String tableName = getTableName(table);
        String columnName = getColumnName(column);
        if (isIgnoreColumn(column)) {
            return false;
        }
        if (isReservedColumn(column)) {
            return false;
        }

        if (!isAggregateRoot(table)) {
            if (columnName.equalsIgnoreCase(getParent(table) + "_id")) {
                return false;
            }
        }

        if (relations.containsKey(tableName)) {
            for (Map.Entry<String, String> entry : relations.get(tableName).entrySet()) {
                String[] refInfos = entry.getValue().split(";");
                switch (refInfos[0]) {
                    case "ManyToOne":
                    case "OneToOne":
                        if (columnName.equalsIgnoreCase(refInfos[1])) {
                            return false;
                        }
                        break;
                    default:
                        // 暂不支持
                        break;
                }
            }
        }
        return true;
    }

    /**
     * 获取Id列
     *
     * @param columns
     * @return
     */
    private Map<String, Object> getIdColumn(List<Map<String, Object>> columns) {
        return columns.stream().filter(column -> Objects.equals(idField, getColumnName(column)))
                .findFirst().orElse(null);
    }

    /**
     * 是否Id列
     * @param column
     * @return
     */
    private boolean isIdColumn(Map<String, Object> column){
        return Objects.equals(getColumnName(column), idField);
    }

    /**
     * 是否Version列
     * @param column
     * @return
     */
    private boolean isVersionColumn(Map<String, Object> column){
        return Objects.equals(getColumnName(column), versionField);
    }

    /**
     * 获取指定列
     *
     * @param columns
     * @param columnName
     * @return
     */
    private Map<String, Object> getColumn(List<Map<String, Object>> columns, String columnName) {
        return columns.stream().filter(column -> Objects.equals(columnName, getColumnName(column)))
                .findFirst().orElse(null);
    }

    /**
     * 生成字段注释
     *
     * @param column
     * @return
     */
    public List<String> generateFieldComment(Map<String, Object> column) {
        List<String> comments = new ArrayList<>();
        String fieldName = getColumnName(column);
        String fieldType = getColumnJavaType(column);
        comments.add("/**");
        for (String comment : getComment(column).split(PATTERN_LINE_BREAK)) {
            if (StringUtils.isEmpty(comment)) {
                continue;
            }
            comments.add(" * " + comment);
            if (hasEnum(column)) {
                getLog().info("获取枚举java类型：" + fieldName + " -> " + fieldType);
                Map<Integer, String[]> enumMap = EnumConfigMap.get(fieldType);
                if (enumMap == null) {
                    enumMap = EnumConfigMap.get(getType(column));
                }
                if (enumMap != null) {
                    comments.addAll(enumMap.entrySet().stream()
                            .map(c -> " * " + c.getKey() + ":" + c.getValue()[0] + ":" + c.getValue()[1])
                            .collect(Collectors.toList())
                    );
                }
            }
        }
        if (Objects.equals(fieldName, versionField)) {
            comments.add(" * 数据版本（支持乐观锁）");
        }
        if (generateDbType) {
            comments.add(" * " + getColumnDbType(column));
        }
        comments.add(" */");
        return comments;
    }

    /**
     * 解析实体类全路径包名，含basePackage
     *
     * @param table
     * @param basePackage
     * @param baseDir
     * @return
     */
    public String resolveEntityFullPackage(Map<String, Object> table, String basePackage, String baseDir) {
        String tableName = getTableName(table);
        String simpleClassName = getEntityJavaType(tableName);
        String packageName = SourceFileUtils.concatPackage(basePackage, getEntityPackage(tableName));

//        Optional<File> existFilePath = SourceFileUtils.findJavaFileBySimpleClassName(baseDir, simpleClassName);
//        if (existFilePath.isPresent()) {
//            packageName = SourceFileUtils.resolvePackage(existFilePath.get().getAbsolutePath());
//        }
        return packageName;
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
        String tableName = getTableName(table);

        if (isIgnoreTable(table)) {
            return result;
        }
        // 聚合内部关系 OneToMany
        // OneToOne关系也用OneToMany实现，避免持久化存储结构变更
        if (!isAggregateRoot(table)) {
            String parent = getParent(table);
            result.putIfAbsent(parent, new HashMap<>());
            boolean rewrited = false;// 是否显式声明引用字段
            for (Map<String, Object> column : columns) {
                if (hasReference(column)) {
                    if (parent.equalsIgnoreCase(getReference(column))) {
                        boolean lazy = isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                        result.get(parent).putIfAbsent(tableName, "OneToMany;" + getColumnName(column) + (lazy ? ";LAZY" : ""));
                        if (generateParent) {
                            result.putIfAbsent(tableName, new HashMap<>());
                            result.get(tableName).putIfAbsent(parent, "*ManyToOne;" + getColumnName(column) + (lazy ? ";LAZY" : ""));
                        }
                        rewrited = true;
                    }
                }
            }
            if (!rewrited) {
                Map<String, Object> column = columns.stream().filter(c -> getColumnName(c).equals(parent + "_id")).findFirst().orElseGet(() -> null);
                if (column != null) {
                    boolean lazy = isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                    result.get(parent).putIfAbsent(tableName, "OneToMany;" + getColumnName(column) + (lazy ? ";LAZY" : ""));
                    if (generateParent) {
                        result.putIfAbsent(tableName, new HashMap<>());
                        result.get(tableName).putIfAbsent(parent, "*ManyToOne;" + getColumnName(column) + (lazy ? ";LAZY" : ""));
                    }
                }
            }
        }

        // 聚合之间关系
        if (hasRelation(table)) {
            // ManyToMany
            String owner = "";
            String beowned = "";
            String joinCol = "";
            String inverseJoinColumn = "";
            boolean ownerLazy = false;
            for (Map<String, Object> column : columns) {
                if (hasReference(column)) {
                    String refTableName = getReference(column);
                    result.putIfAbsent(refTableName, new HashMap<>());
                    boolean lazy = isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
                    if (StringUtils.isBlank(owner)) {
                        ownerLazy = lazy;
                        owner = refTableName;
                        joinCol = getColumnName(column);
                    } else {
                        beowned = refTableName;
                        inverseJoinColumn = getColumnName(column);
                        result.get(beowned).putIfAbsent(owner, "*ManyToMany;" + inverseJoinColumn + (lazy ? ";LAZY" : ""));
                    }
                }
            }
            result.get(owner).putIfAbsent(beowned, "ManyToMany;" + joinCol + ";" + inverseJoinColumn + ";" + tableName + (ownerLazy ? ";LAZY" : ""));
        }

        for (Map<String, Object> column : columns) {
            String colRel = getRelation(column);
            String colName = getColumnName(column);
            String refTableName = null;
            boolean lazy = isLazy(column, "LAZY".equalsIgnoreCase(this.fetchType));
            if (StringUtils.isNotBlank(colRel) || hasReference(column)) {
                switch (colRel) {
                    case "OneToOne":
                    case "1:1":
                        refTableName = getReference(column);
                        result.putIfAbsent(tableName, new HashMap<>());
                        result.get(tableName).putIfAbsent(refTableName, "OneToOne;" + colName + (lazy ? ";LAZY" : ""));
//                        result.putIfAbsent(refTableName, new HashMap<>());
//                        result.get(refTableName).putIfAbsent(tableName, "*OneToOne;" + colName + (lazy ? ";LAZY" : ""));
                        break;
                    case "ManyToOne":
                    case "*:1":
                    default:
                        refTableName = getReference(column);
                        result.putIfAbsent(tableName, new HashMap<>());
                        result.get(tableName).putIfAbsent(refTableName, "ManyToOne;" + colName + (lazy ? ";LAZY" : ""));
//                        result.putIfAbsent(refTableName, new HashMap<>());
//                        result.get(refTableName).putIfAbsent(tableName, "*OneToMany;" + colName + (lazy ? ";LAZY" : ""));
                        break;
                }
            }
        }
        return result;
    }

    public boolean readEntityCustomerSourceFile(String filePath, List<String> importLines, List<String> annotationLines, List<String> customerLines) throws IOException {
        if (FileUtils.fileExists(filePath)) {
            String content = FileUtils.fileRead(filePath, this.outputEncoding);
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
                    getLog().debug("[annotation] " + line);
                } else if ((annotationLines.size() == 0 && startClassLine == 0)) {
                    importLines.add(line);
                    getLog().debug("[import] " + line);
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
            customerLines.forEach(l -> getLog().debug("[customer] " + l));
            if (startMapperLine == 0 || endMapperLine == 0) {
                return false;
            }
            FileUtils.fileDelete(filePath);
        }
        return true;
    }

    public void processImportLines(Map<String, Object> table, List<String> importLines, String content) {
        boolean importEmpty = importLines.size() == 0;
        if (importEmpty) {
            importLines.add("");
        }

        List<String> entityClassExtraImports = getEntityClassExtraImports();
        if (isValueObject(table)) {
            int idx = entityClassExtraImports.indexOf("org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate");
            if (idx > 0) {
                entityClassExtraImports.add(idx, "org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject");
            } else {
                entityClassExtraImports.add("org.netcorepal.cap4j.ddd.domain.aggregate.ValueObject");
            }
        }
        if (importEmpty) {
            boolean breakLine = false;
            for (String entityClassExtraImport : entityClassExtraImports) {
                if (entityClassExtraImport.startsWith("javax") && !breakLine) {
                    breakLine = true;
                    importLines.add("");
                }
                importLines.add("import " + entityClassExtraImport + ";");
            }
            importLines.add("");
            importLines.add("/**");
            for (String comment : getComment(table).split(PATTERN_LINE_BREAK)) {
                if (StringUtils.isEmpty(comment)) {
                    continue;
                }
                importLines.add(" * " + comment);
            }
            importLines.add(" *");
            // importLines.add(" * " + getComment(table).replaceAll(PATTERN_LINE_BREAK, " "));
            importLines.add(" * 本文件由[cap4j-ddd-codegen-maven-plugin]生成");
            importLines.add(" * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明");
            importLines.add(" * @author cap4j-ddd-codegen");
            importLines.add(" * @date " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            importLines.add(" */");
        } else {
            for (String entityClassExtraImport : entityClassExtraImports) {
                SourceFileUtils.addIfNone(importLines,
                        "\\s*import\\s+" + (entityClassExtraImport
                                .replace(".", "\\.")
                                .replace("*", "\\*")) + "\\s*;",
                        "import " + entityClassExtraImport + ";",
                        (list, line) -> {
                            Optional<String> firstLargeLine = list.stream().filter(l -> !l.isEmpty() && l.compareTo(line) > 0).findFirst();
                            if (firstLargeLine.isPresent()) {
                                return list.indexOf(firstLargeLine.get());
                            }
                            List<String> imports = list.stream().filter(l -> !l.isEmpty() && !l.contains(" java") && l.startsWith("import")).collect(Collectors.toList());
                            return list.indexOf(imports.get(imports.size() - 1)) + 1;
                        });
            }
        }
        for (int i = 0; i < importLines.size(); i++) {
            String importLine = importLines.get(i);
            if (importLine.contains(" org.hibernate.annotations.")
                    && !importLine.contains("*")) {
                String hibernateAnnotation = importLine.substring(importLine.lastIndexOf(".") + 1).replace(";", "").trim();
                if (!content.contains(hibernateAnnotation)) {
                    importLines.remove(importLine);
                    i--;
                }
            }
        }
    }

    public void processAnnotationLines(Map<String, Object> table, List<Map<String, Object>> columns, List<String> annotationLines) {
        String tableName = getTableName(table);
        boolean annotationEmpty = annotationLines.size() == 0;
        SourceFileUtils.removeText(annotationLines, "@Aggregate\\(.*\\)");
        SourceFileUtils.addIfNone(annotationLines, "@Aggregate\\(.*\\)", "@Aggregate(" +
                "aggregate = \"" + getAggregateWithModule(tableName) + "\", " +
                "name = \"" + getEntityJavaType(tableName) + "\", " +
                "root = " + (isAggregateRoot(table) ? "true" : "false") + ", " +
                "type = " + (isValueObject(table) ? "Aggregate.TYPE_VALUE_OBJECT" : "Aggregate.TYPE_ENTITY") + ", " +
                (isAggregateRoot(table) ? "" : ("relevant = { \"" + getEntityJavaType(getParent(table)) + "\" }, ")) +
                "description = \"" + getComment(table).replaceAll(PATTERN_LINE_BREAK, "\\\\n") + "\"" +
                ")", (list, line) -> 0);
        if (StringUtils.isNotBlank(getAggregateRootAnnotation())) {
            if (isAggregateRoot(table)) {
                SourceFileUtils.addIfNone(annotationLines, getAggregateRootAnnotation() + "(\\(.*\\))?", getAggregateRootAnnotation());
            } else {
                SourceFileUtils.removeText(annotationLines, getAggregateRootAnnotation() + "(\\(.*\\))?");
                SourceFileUtils.removeText(annotationLines, "@AggregateRoot(\\(.*\\))?");
            }
        }

        SourceFileUtils.addIfNone(annotationLines, "@Entity(\\(.*\\))?", "@Entity");
        SourceFileUtils.addIfNone(annotationLines, "@Table(\\(.*\\))?", "@Table(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + tableName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\")");
        SourceFileUtils.addIfNone(annotationLines, "@DynamicInsert(\\(.*\\))?", "@DynamicInsert");
        SourceFileUtils.addIfNone(annotationLines, "@DynamicUpdate(\\(.*\\))?", "@DynamicUpdate");
        if (StringUtils.isNotBlank(deletedField) && hasColumn(deletedField, columns)) {
            if (hasColumn(versionField, columns)) {
                SourceFileUtils.addIfNone(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + tableName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " set " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + deletedField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = 1 where " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + idField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = ? and " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + versionField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = ? \")");
            } else {
                SourceFileUtils.addIfNone(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + tableName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " set " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + deletedField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = 1 where " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + idField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = ? \")");
            }
            if (hasColumn(versionField, columns) && !SourceFileUtils.hasLine(annotationLines, "@SQLDelete(\\(.*" + versionField + ".*\\))")) {
                SourceFileUtils.replaceText(annotationLines, "@SQLDelete(\\(.*\\))?", "@SQLDelete(sql = \"update " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + tableName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " set " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + deletedField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = 1 where " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + idField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = ? and " + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + versionField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = ? \")");
            }
            SourceFileUtils.addIfNone(annotationLines, "@Where(\\(.*\\))?", "@Where(clause = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + deletedField + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + " = 0\")");
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

    public void buildEntitySourceFile(Map<String, Object> table, List<Map<String, Object>> columns, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations, String basePackage, String baseDir) throws IOException {
        String tableName = getTableName(table);
        if (isIgnoreTable(table)) {
            getLog().info("跳过忽略表：" + tableName);
            return;
        }
        if (hasRelation(table)
            // &&"ManyToMany".equalsIgnoreCase(getRelation(table))
        ) {
            getLog().info("跳过关系表：" + tableName);
            return;
        }

        String entityType = getEntityJavaType(tableName);
        String entityFullPackage = tablePackageMap.get(tableName);

        FileUtils.mkdir(SourceFileUtils.resolveDirectory(baseDir, entityFullPackage));

        String filePath = SourceFileUtils.resolveSourceFile(baseDir, entityFullPackage, entityType);

        List<String> enums = new ArrayList<>();
        List<String> importLines = new ArrayList<>();
        List<String> annotationLines = new ArrayList<>();
        List<String> customerLines = new ArrayList<>();
        if (!readEntityCustomerSourceFile(filePath, importLines, annotationLines, customerLines)) {
            getLog().warn("文件被改动，无法自动更新！" + filePath);
            return;
        }

        processAnnotationLines(table, columns, annotationLines);
        String mainSource = writeEntityClass(table, columns, tablePackageMap, relations, enums, annotationLines, customerLines);
        processImportLines(table, importLines, mainSource);

        getLog().info("开始生成实体文件：" + filePath);
        FileUtils.fileWrite(filePath, outputEncoding,
                "package " + entityFullPackage + ";\n" +
                        String.join("\n", importLines) + "\n" +
                        mainSource + "\n"
        );
        if (generateSchema) {
            writeSchemaSourceFile(table, columns, tablePackageMap, relations, basePackage, baseDir);
        }
        if (isAggregateRoot(table)) {
            if (hasFactory(table)) {
                writeFactorySourceFile(table, tablePackageMap, baseDir);
            }
            if (hasSpecification(table)) {
                writeSpecificationSourceFile(table, tablePackageMap, baseDir);
            }
            if (hasDomainEvent(table)) {
                List<String> domainEvents = getDomainEvent(table);
                for (String domainEvent : domainEvents) {
                    if (StringUtils.isBlank(domainEvent)) {
                        continue;
                    }
                    String[] segments = TextUtils.splitWithTrim(domainEvent, ":");
                    String domainEventClassName = generateDomainEventName(segments[0]);
                    String domainEventDescription = segments.length > 1 ? segments[1] : "todo: 领域事件说明";
                    writeDomainEventSourceFile(table, tablePackageMap, domainEventClassName, domainEventDescription, baseDir);
                }
            }
        }
    }

    private String writeEntityClass(Map<String, Object> table, List<Map<String, Object>> columns, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations, List<String> enums, List<String> annotationLines, List<String> customerLines) throws IOException {
        String tableName = getTableName(table);
        String entityType = getEntityJavaType(tableName);
        String idType = getIdColumn(columns) == null ? "Long" : getColumnJavaType(getIdColumn(columns));

        StringWriter stringWriter = new StringWriter();
        BufferedWriter out = new BufferedWriter(stringWriter);
        annotationLines.forEach(line -> writeLine(out, line));
        writeLine(out, "public class " + entityType +
                (StringUtils.isNotBlank(entityBaseClass) ? " extends " + entityBaseClass : "") +
                (isValueObject(table) ? " implements ValueObject<" + idType + ">" : "") + " {");
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
        writeLine(out, "    // 【字段映射开始】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动");
        if (isValueObject(table)) {
            String hashTemplate = "";
            if (StringUtils.isNotBlank(hashMethod4ValueObject)) {
                hashTemplate = "    " + hashMethod4ValueObject.trim();
            } else if (getIdColumn(columns) == null) {
                hashTemplate = "    @Override\n" +
                        "    public Long hash() {\n" +
                        "        return (Long) " + getEntityIdGenerator(table) + ".hash(this, \"${idField}\");\n" +
                        "    }";
            } else {
                hashTemplate = "    @Override\n" +
                        "    public ${idType} hash() {\n" +
                        "        if(null == ${idField}) {\n" +
                        "            ${idField} = (${idType}) " + getEntityIdGenerator(table) + ".hash(this, \"${idField}\");\n" +
                        "        }\n" +
                        "        return ${idField};\n" +
                        "    }";
            }

            writeLine(out, "");
            writeLine(out, hashTemplate
                    .replace("${idField}", idField)
                    .replace("${IdField}", idField)
                    .replace("${ID_FIELD}", idField)
                    .replace("${id_field}", idField)
                    .replace("${idType}", idType)
                    .replace("${IdType}", idType)
                    .replace("${ID_TYPE}", idType)
                    .replace("${id_type}", idType)
            );
            writeLine(out, "");
            writeLine(out,
                    "    @Override\n" +
                            "    public boolean equals(Object o) {\n" +
                            "        if (null == o) {\n" +
                            "            return false;\n" +
                            "        }\n" +
                            "        if (!(o instanceof Address)) {\n" +
                            "            return false;\n" +
                            "        }\n" +
                            "        return hashCode() == o.hashCode();\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public int hashCode() {\n" +
                            "        return hash().hashCode();\n" +
                            "    }");
            writeLine(out, "");
        }
        if (null == getIdColumn(columns)) {
            throw new RuntimeException("实体表缺失【主键】：" + tableName);
        }
        writeRelationProperty(out, table, relations, tablePackageMap);
        for (Map<String, Object> column : columns) {
            writeColumnProperty(out, table, column, relations, enums);
        }
        writeLine(out, "");
        writeLine(out, "    // 【字段映射结束】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动");
        writeLine(out, "}");
        writeLine(out, "");
        out.flush();
        out.close();
        return stringWriter.toString();
    }

    private String getEntityIdGenerator(Map<String, Object> table) {
        String entityIdGenerator = null;
        if (hasIdGenerator(table)) {
            entityIdGenerator = getIdGenerator(table);
        } else if (isValueObject(table)) {
            if (StringUtils.isNotBlank(idGenerator4ValueObject)) {
                entityIdGenerator = idGenerator4ValueObject;
            } else {
                // ValueObject 值对象 默认使用MD5
                entityIdGenerator = "org.netcorepal.cap4j.ddd.domain.repo.Md5HashIdentifierGenerator";
            }
        } else {
            if (StringUtils.isNotBlank(idGenerator)) {
                entityIdGenerator = idGenerator;
            }
        }
        return entityIdGenerator;
    }

    public void writeColumnProperty(BufferedWriter out, Map<String, Object> table, Map<String, Object> column, Map<String, Map<String, String>> relations, List<String> enums) {
        String columnName = getColumnName(column);
        String columnJavaType = getColumnJavaType(column);

        if (!isColumnNeedGenerate(table, column, relations) && !Objects.equals(columnName, versionField)) {
            return;
        }

        boolean updatable = true;
        boolean insertable = true;
        if (getColumnJavaType(column).contains("Date")) {
            updatable = !isAutoUpdateDateColumn(column);
            insertable = !isAutoInsertDateColumn(column);
        }
        if (isReadOnlyColumn(column)) {
            insertable = false;
            updatable = false;
        }
        if (hasIgnoreInsert(column)) {
            insertable = false;
        }
        if (hasIgnoreUpdate(column)) {
            updatable = false;
        }

        writeLine(out, "");
        writeFieldComment(out, column);
        if (isIdColumn(column)) {
            writeLine(out, "    @Id");
            String entityIdGenerator = getEntityIdGenerator(table);
            if (null != entityIdGenerator) {
                writeLine(out, "    @GeneratedValue(generator = \"" + entityIdGenerator + "\")");
                writeLine(out, "    @GenericGenerator(name = \"" + entityIdGenerator + "\", strategy = \"" + entityIdGenerator + "\")");
            } else {
                // 无ID生成器 使用数据库自增
                writeLine(out, "    @GeneratedValue(strategy = GenerationType.IDENTITY)");
            }
        }
        if (isVersionColumn(column)) {
            writeLine(out, "    @Version");
        }
        if (hasEnum(column)) {
            enums.add(columnJavaType);
            writeLine(out, "    @Convert(converter = " + columnJavaType + ".Converter.class)");
        }
        if (!updatable || !insertable) {
            writeLine(out, "    @Column(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + columnName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", insertable = " + (insertable ? "true" : "false") + ", updatable = " + (updatable ? "true" : "false") + ")");
        } else {
            writeLine(out, "    @Column(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + columnName + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\")");
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

    public void writeFieldComment(BufferedWriter out, Map<String, Object> column) {
        for (String line : generateFieldComment(column)) {
            writeLine(out, "    " + line);
        }
    }

    public void writeRelationProperty(BufferedWriter out, Map<String, Object> table, Map<String, Map<String, String>> relations, Map<String, String> tablePackageMap) {
        String tableName = getTableName(table);
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
                if (hasLazy(navTable)) {
                    fetchType = isLazy(navTable) ? "LAZY" : "EAGER";
                    getLog().info(tableName + ":" + entry.getKey() + ":" + fetchType);
                }

                String relation = refInfos[0];
                String joinColumn = refInfos[1];
                String fetchAnnotation = ""; // fetchType.equals("LAZY") ? "" : (" @Fetch(FetchMode." + this.fetchMode + ")");


                writeLine(out, "");
                switch (relation) {
                    case "OneToMany":// 专属聚合内关系
                        writeLine(out, "    @" + relation.replace("*", "") + "(cascade = { CascadeType.ALL }, fetch = FetchType." + fetchType + ", orphanRemoval = true)" + fetchAnnotation);
                        writeLine(out, "    @JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + joinColumn + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false)");
                        boolean countIsOne = countIsOne(navTable);
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
                    case "*ManyToOne":
                        writeLine(out, "    @" + relation.replace("*", "") + "(cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    @JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + joinColumn + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false, insertable = false, updatable = false)");
                        writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                        break;
                    case "ManyToOne":
                        writeLine(out, "    @" + relation.replace("*", "") + "(cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    @JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + joinColumn + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false)");
                        writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                        break;
                    case "*OneToMany":// 当前不会用到，无法控制集合数量规模
                        writeLine(out, "    @" + relation.replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\"" +
                                ", cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                        break;
                    case "OneToOne":
                        writeLine(out, "    @" + relation.replace("*", "") + "(cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    @JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + joinColumn + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false)");
                        writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                        break;
                    case "*OneToOne":
                        writeLine(out, "    @" + relation.replace("*", "") + "(mappedBy = \"" + toLowerCamelCase(getEntityJavaType(tableName)) + "\"" +
                                ", cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    private " + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + " " + toLowerCamelCase(getEntityJavaType(entry.getKey())) + ";");
                        break;
                    case "ManyToMany":
                        writeLine(out, "    @" + relation.replace("*", "") + "(cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    @JoinTable(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + refInfos[3] + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\"" +
                                ", joinColumns = {@JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + joinColumn + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false)}" +
                                ", inverseJoinColumns = {@JoinColumn(name = \"" + LEFT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + refInfos[2] + RIGHT_QUOTES_4_ID_ALIAS.replace("\"", "\\\"") + "\", nullable = false)})");
                        writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                        break;
                    case "*ManyToMany":
                        writeLine(out, "    @" + relation.replace("*", "") + "(mappedBy = \"" + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(tableName))) + "\"" +
                                ", cascade = { }, fetch = FetchType." + fetchType + ")" + fetchAnnotation);
                        writeLine(out, "    private java.util.List<" + tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey()) + "> " + Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))) + ";");
                        break;
                    default:

                        break;
                }

            }
        }
    }

    public void writeFactorySourceFile(Map<String, Object> table, Map<String, String> tablePackageMap, String baseDir) throws IOException {
        String tag = "factory";
        String tableName = getTableName(table);
        String aggregate = getAggregateWithModule(tableName);

        String entityFullPackage = tablePackageMap.get(tableName);
        String entityType = getEntityJavaType(tableName);
        String entityVar = toLowerCamelCase(entityType);


        Map<String, String> context = getEscapeContext();
        putContext(tag, "Name", entityType + "Factory", context);
        putContext(tag, "Factory", context.get("Name"), context);
        putContext(tag, "templatePackage", refPackage(getAggregatesPackage()), context);
        putContext(tag, "package", refPackage(aggregate), context);
        putContext(tag, "path", aggregate.replace(".", File.separator), context);
        putContext(tag, "Aggregate", aggregate, context);
        putContext(tag, "Comment", "", context);
        putContext(tag, "CommentEscaped", "", context);
        putContext(tag, "entityPackage", SourceFileUtils.refPackage(entityFullPackage, basePackage), context);
        putContext(tag, "Entity", entityType, context);
        putContext(tag, "AggregateRoot", context.get("Entity"), context);
        putContext(tag, "EntityVar", entityVar, context);

        List<TemplateNode> factoryTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultFactoryPayloadTemplateNode(), getDefaultFactoryTemplateNode());
        try {
            for (TemplateNode templateNode : factoryTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info(SourceFileUtils.resolveDirectory(
                        baseDir,
                        concatPackage(basePackage, context.get("templatePackage"))
                ));
                getLog().info("开始生成聚合工厂：" + path);
            }
        } catch (IOException e) {
            getLog().error("聚合工厂模板文件写入失败！", e);
        }
    }

    public void writeSpecificationSourceFile(Map<String, Object> table, Map<String, String> tablePackageMap, String baseDir) throws IOException {
        String tag = "specification";
        String tableName = getTableName(table);
        String aggregate = getAggregateWithModule(tableName);

        String entityFullPackage = tablePackageMap.get(tableName);
        String entityType = getEntityJavaType(tableName);
        String entityVar = toLowerCamelCase(entityType);


        Map<String, String> context = getEscapeContext();
        putContext(tag, "Name", entityType + "Specification", context);
        putContext(tag, "Specification", context.get("Name"), context);
        putContext(tag, "templatePackage", refPackage(getAggregatesPackage()), context);
        putContext(tag, "package", refPackage(aggregate), context);
        putContext(tag, "path", aggregate.replace(".", File.separator), context);
        putContext(tag, "Aggregate", aggregate, context);
        putContext(tag, "Comment", "", context);
        putContext(tag, "CommentEscaped", "", context);
        putContext(tag, "entityPackage", SourceFileUtils.refPackage(entityFullPackage, basePackage), context);
        putContext(tag, "Entity", entityType, context);
        putContext(tag, "AggregateRoot", context.get("Entity"), context);
        putContext(tag, "EntityVar", entityVar, context);

        List<TemplateNode> domainEventTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultSpecificationTemplateNode());
        try {
            for (TemplateNode templateNode : domainEventTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info("开始生成实体规约：" + path);
            }
        } catch (IOException e) {
            getLog().error("实体规约模板文件写入失败！", e);
        }

    }

    public void writeDomainEventSourceFile(Map<String, Object> table, Map<String, String> tablePackageMap, String domainEventClassName, String domainEventDesc, String baseDir) {
        String tag = "domain_event";
        String handlerTag = "domain_event_handler";
        String tableName = getTableName(table);
        String aggregate = getAggregateWithModule(tableName);

        String entityFullPackage = tablePackageMap.get(tableName);
        String entityType = getEntityJavaType(tableName);
        String entityVar = toLowerCamelCase(entityType);

        String domainEventDescEscaped = domainEventDesc.replaceAll("(\\r\\n)|(\\r)|(\\n)", "\\n");

        Map<String, String> context = getEscapeContext();
        putContext(tag, "Name", domainEventClassName, context);
        putContext(tag, "DomainEvent", context.get("Name"), context);
        putContext(tag, "domainEventPackage", refPackage(getAggregatesPackage()), context);
        putContext(tag, "domainEventHandlerPackage", refPackage(getSubscriberPackage()), context);
        putContext(tag, "package", refPackage(aggregate), context);
        putContext(tag, "path", aggregate.replace(".", File.separator), context);
        putContext(tag, "persist", "false", context);
        putContext(tag, "Aggregate", aggregate, context);
        putContext(tag, "Comment", domainEventDescEscaped, context);
        putContext(tag, "CommentEscaped", domainEventDescEscaped, context);
        putContext(tag, "entityPackage", SourceFileUtils.refPackage(entityFullPackage, basePackage), context);
        putContext(tag, "Entity", entityType, context);
        putContext(tag, "AggregateRoot", context.get("Entity"), context);
        putContext(tag, "EntityVar", entityVar, context);

        putContext(tag, "templatePackage", context.get("domainEventPackage"), context);
        List<TemplateNode> domainEventTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultDomainEventTemplateNode());
        try {
            for (TemplateNode templateNode : domainEventTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info("开始生成领域事件文件：" + path);
            }
        } catch (IOException e) {
            getLog().error("领域事件模板文件写入失败！", e);
        }

        putContext(tag, "templatePackage", context.get("domainEventHandlerPackage"), context);
        List<TemplateNode> domainEventHandlerTemplateNodes = templateNodeMap.containsKey(handlerTag)
                ? templateNodeMap.get(handlerTag)
                : Arrays.asList(getDefaultDomainEventHandlerTemplateNode());
        try {
            for (TemplateNode templateNode : domainEventHandlerTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info("开始生成领域事件订阅：" + path);
            }
        } catch (IOException e) {
            getLog().error("领域事件订阅模板文件写入失败！", e);
        }
    }

    public void writeEnumSourceFile(Map<Integer, String[]> enumConfigs, String enumClassName, String enumValueField, String enumNameField, Map<String, String> tablePackageMap, String baseDir) throws IOException {
        String tag = "enum";
        String itemTag = "enum_item";
        String tableName = EnumTableNameMap.get(enumClassName);
        String aggregate = getAggregateWithModule(tableName);

        String entityFullPackage = tablePackageMap.get(tableName);
        String entityType = getEntityJavaType(tableName);
        String entityVar = toLowerCamelCase(entityType);

        Map<String, String> context = getEscapeContext();
        putContext(tag, "templatePackage", refPackage(getAggregatesPackage()), context);
        putContext(tag, "package", refPackage(aggregate), context);
        putContext(tag, "path", aggregate.replace(".", File.separator), context);
        putContext(tag, "Aggregate", aggregate, context);
        putContext(tag, "Comment", "", context);
        putContext(tag, "CommentEscaped", "", context);
        putContext(tag, "entityPackage", SourceFileUtils.refPackage(entityFullPackage, basePackage), context);
        putContext(tag, "Entity", entityType, context);
        putContext(tag, "AggregateRoot", context.get("Entity"), context);
        putContext(tag, "EntityVar", entityVar, context);
        putContext(tag, "Enum", enumClassName, context);
        putContext(tag, "EnumValueField", enumValueField, context);
        putContext(tag, "EnumNameField", enumNameField, context);
        String enumItems = "";
        for (Map.Entry<Integer, String[]> entry : enumConfigs.entrySet()) {
            String itemValue = entry.getKey().toString();
            String itemName = entry.getValue()[0];
            String itemDesc = entry.getValue()[1];
            getLog().info("  " + itemDesc + " : " + itemName + " = " + entry.getKey());
            Map<String, String> itemContext = new HashMap<>(context);
            putContext(itemTag, "itemName", itemName, itemContext);
            putContext(itemTag, "itemValue", itemValue, itemContext);
            putContext(itemTag, "itemDesc", itemDesc, itemContext);
            PathNode enumItemsPathNode = ((templateNodeMap.containsKey(itemTag) && templateNodeMap.get(itemTag).size() > 0)
                    ? templateNodeMap.get(itemTag).get(templateNodeMap.get(itemTag).size() - 1)
                    : getDefaultEnumItemTemplateNode()
            ).clone().resolve(itemContext);
            enumItems += enumItemsPathNode.getData();
        }
        putContext(tag, "ENUM_ITEMS", enumItems, context);
        List<TemplateNode> enumTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultEnumTemplateNode());
        try {
            for (TemplateNode templateNode : enumTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info(JSON.toJSONString(context));
                getLog().info("开始生成枚举文件：" + path);
            }
        } catch (IOException e) {
            getLog().error("枚举模板文件写入失败！", e);
        }
    }

    public void writeSchemaSourceFile(Map<String, Object> table, List<Map<String, Object>> columns, Map<String, String> tablePackageMap, Map<String, Map<String, String>> relations, String basePackage, String baseDir) throws IOException {
        String tag = "schema";
        String fieldTag = "schema_field";
        String joinTag = "schema_join";
        String tableName = getTableName(table);
        String aggregate = getAggregateWithModule(tableName);
        String schemaPackage = null;
        if ("abs".equalsIgnoreCase(entitySchemaOutputMode)) {
            schemaPackage = getSchemaPackage();
        } else {
            schemaPackage = getAggregatesPackage();
        }

        String entityFullPackage = tablePackageMap.get(tableName);
        String entityType = getEntityJavaType(tableName);
        String entityVar = toLowerCamelCase(entityType);

        String comment = getComment(table).replaceAll(PATTERN_LINE_BREAK, " ");

        String schemaBaseFullPackage = StringUtils.isNotBlank(schemaPath)
                ? SourceFileUtils.resolvePackage(schemaPath)
                : SourceFileUtils.concatPackage(basePackage, entitySchemaOutputPackage);

        Map<String, String> context = getEscapeContext();

        putContext(tag, "templatePackage", refPackage(schemaPackage), context);
        putContext(tag, "package", refPackage(aggregate), context);
        putContext(tag, "path", aggregate.replace(".", File.separator), context);
        putContext(tag, "Aggregate", aggregate, context);
        putContext(tag, "Comment", comment, context);
        putContext(tag, "CommentEscaped", comment.replaceAll(PATTERN_LINE_BREAK, " "), context);
        putContext(tag, "entityPackage", SourceFileUtils.refPackage(entityFullPackage, basePackage), context);
        putContext(tag, "Entity", entityType, context);
        putContext(tag, "EntityVar", entityVar, context);
        putContext(tag, "schemaBasePackage", SourceFileUtils.refPackage(schemaBaseFullPackage, basePackage), context);
        putContext(tag, "SchemaBase", DEFAULT_SCHEMA_BASE_CLASS_NAME, context);
        putContext(tag, "IdField", idField, context);

        String fieldItems = "";
        for (Map<String, Object> column : columns) {
            if (!isColumnNeedGenerate(table, column, relations)) {
                continue;
            }
            String fieldType = getColumnJavaType(column);
            String fieldName = toLowerCamelCase(getColumnName(column));
            String fieldComment = generateFieldComment(column).stream().reduce((a, b) -> a + "\n    " + b).orElse("");
            Map<String, String> itemContext = new HashMap<>(context);
            putContext(fieldTag, "fieldType", fieldType, itemContext);
            putContext(fieldTag, "fieldName", fieldName, itemContext);
            putContext(fieldTag, "fieldComment", fieldComment, itemContext);
            fieldItems += (
                    templateNodeMap.containsKey(fieldTag) && templateNodeMap.get(fieldTag).size() > 0
                            ? templateNodeMap.get(fieldTag).get(templateNodeMap.get(fieldTag).size() - 1)
                            : getDefaultSchemaFieldTemplateNode().clone()
            ).resolve(itemContext).getData();
        }

        String joinItems = "";
        if (relations.containsKey(tableName)) {
            for (Map.Entry<String, String> entry : relations.get(tableName).entrySet()) {
                String[] refInfos = entry.getValue().split(";");
                Map<String, String> joinContext = new HashMap<>(context);
                switch (refInfos[0]) {
                    case "OneToMany":
                    case "*OneToMany":
                        putContext(joinTag, "joinEntityPackage", tablePackageMap.get(entry.getKey()), joinContext);
                        putContext(joinTag, "joinEntityType", getEntityJavaType(entry.getKey()), joinContext);
                        putContext(joinTag, "joinEntityVars", Inflector.getInstance().pluralize(toLowerCamelCase(getEntityJavaType(entry.getKey()))), joinContext);
                        if (!("abs".equalsIgnoreCase(entitySchemaOutputMode))) {
                            putContext(joinTag, "joinEntitySchemaPackage", concatPackage(tablePackageMap.get(entry.getKey()), DEFAULT_SCHEMA_PACKAGE) + ".", joinContext);
                        }
                        joinItems += (
                                (templateNodeMap.containsKey(joinTag) && templateNodeMap.get(joinTag).size() > 0)
                                        ? templateNodeMap.get(joinTag).get(templateNodeMap.get(joinTag).size() - 1)
                                        : getDefaultSchemaJoinTemplateNode()
                        ).clone().resolve(joinContext).getData();
                        break;
                    case "OneToOne":
                    case "ManyToOne":
                        putContext(joinTag, "joinEntityPackage", tablePackageMap.get(entry.getKey()), joinContext);
                        putContext(joinTag, "joinEntityType", getEntityJavaType(entry.getKey()), joinContext);
                        putContext(joinTag, "joinEntityVars", toLowerCamelCase(getEntityJavaType(entry.getKey())), joinContext);
                        if (!("abs".equalsIgnoreCase(entitySchemaOutputMode))) {
                            putContext(joinTag, "joinEntitySchemaPackage", concatPackage(tablePackageMap.get(entry.getKey()), DEFAULT_SCHEMA_PACKAGE) + ".", joinContext);
                        }
                        joinItems += (
                                (templateNodeMap.containsKey(joinTag) && templateNodeMap.get(joinTag).size() > 0)
                                        ? templateNodeMap.get(joinTag).get(templateNodeMap.get(joinTag).size() - 1)
                                        : getDefaultSchemaJoinTemplateNode()
                        ).clone().resolve(joinContext).getData();

                        String fieldType = tablePackageMap.get(entry.getKey()) + "." + getEntityJavaType(entry.getKey());
                        String fieldName = toLowerCamelCase(getEntityJavaType(entry.getKey()));
                        String fieldComment = generateFieldComment(getColumn(columns, refInfos[1])).stream().reduce((a, b) -> a + "\n    " + b).orElse("");
                        Map<String, String> itemContext = new HashMap<>(context);
                        putContext(fieldTag, "fieldType", fieldType, itemContext);
                        putContext(fieldTag, "fieldName", fieldName, itemContext);
                        putContext(fieldTag, "fieldComment", fieldComment, itemContext);
                        fieldItems += (
                                templateNodeMap.containsKey(fieldTag) && templateNodeMap.get(fieldTag).size() > 0
                                        ? templateNodeMap.get(fieldTag).get(templateNodeMap.get(fieldTag).size() - 1)
                                        : getDefaultSchemaFieldTemplateNode().clone()
                        ).resolve(itemContext).getData();
                        break;
                    default:
                        // 暂不支持
                        break;
                }

            }
        }

        putContext(tag, "FIELD_ITEMS", fieldItems, context);
        putContext(tag, "JOIN_ITEMS", joinItems, context);

        List<TemplateNode> schemaTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultSchemaTemplateNode());
        try {
            for (TemplateNode templateNode : schemaTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                String path = forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
                getLog().info("开始生成Schema文件：" + path);
            }
        } catch (IOException e) {
            getLog().error("Schema模板文件写入失败！", e);
        }
    }

    public void writeSchemaBaseSourceFile(String baseDir) throws IOException {
        String tag = "schema_base";
        String schemaFullPackage = concatPackage(basePackage, getSchemaPackage());

        List<TemplateNode> schemaBaseTemplateNodes = templateNodeMap.containsKey(tag)
                ? templateNodeMap.get(tag)
                : Arrays.asList(getDefaultSchemaBaseTemplateNode());
        Map<String, String> context = getEscapeContext();
        putContext(tag, "templatePackage", SourceFileUtils.refPackage(schemaFullPackage, basePackage), context);
        putContext(tag, "SchemaBase", DEFAULT_SCHEMA_BASE_CLASS_NAME, context);
        try {
            for (TemplateNode templateNode : schemaBaseTemplateNodes) {
                PathNode pathNode = templateNode.clone().resolve(context);
                forceRender(
                        pathNode,
                        SourceFileUtils.resolveDirectory(
                                baseDir,
                                concatPackage(basePackage, context.get("templatePackage"))
                        )
                );
            }
        } catch (IOException e) {
            getLog().error("模板文件写入失败！", e);
        }
    }

    public TemplateNode getDefaultFactoryTemplateNode() {
        String template = "package ${basePackage}${templatePackage}${package}." + DEFAULT_FAC_PACKAGE + ";\n" +
                "\n" +
                "import ${basePackage}${entityPackage}${package}.${Entity};\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "/**\n" +
                " * ${Entity}聚合工厂\n" +
                " * ${Comment}\n" +
                " *\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Aggregate(aggregate = \"${Aggregate}\", name = \"${Entity}Factory\", type = Aggregate.TYPE_FACTORY, description = \"${CommentEscaped}\")\n" +
                "@Service\n" +
                "public class ${Entity}Factory implements AggregateFactory<${Entity}Payload, ${Entity}> {\n" +
                "\n" +
                "    @Override\n" +
                "    public ${Entity} create(${Entity}Payload payload) {\n" +
                "\n" +
                "        return ${Entity}.builder()\n" +
                "\n" +
                "                .build();\n" +
                "    }\n" +
                "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("factory");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_FAC_PACKAGE + "${SEPARATOR}${Entity}Factory.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultFactoryPayloadTemplateNode() {
        String template = "package ${basePackage}${templatePackage}${package}." + DEFAULT_FAC_PACKAGE + ";\n" +
                "\n" +
                "import ${basePackage}${entityPackage}${package}.${Entity};\n" +
                "import lombok.AllArgsConstructor;\n" +
                "import lombok.Builder;\n" +
                "import lombok.Data;\n" +
                "import lombok.NoArgsConstructor;\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;\n" +
                "\n" +
                "/**\n" +
                " * ${Entity}工厂负载\n" +
                " * ${Comment}\n" +
                " *\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Aggregate(aggregate = \"${Aggregate}\", name = \"${Entity}Payload\", type = Aggregate.TYPE_FACTORY_PAYLOAD, description = \"${CommentEscaped}\")\n" +
                "@Data\n" +
                "@Builder\n" +
                "@NoArgsConstructor\n" +
                "@AllArgsConstructor\n" +
                "public class ${Entity}Payload implements AggregatePayload<${Entity}> {\n" +
                "    String name;\n" +
                "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("factory");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_FAC_PACKAGE + "${SEPARATOR}${Entity}Payload.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultSpecificationTemplateNode() {
        String template = "package ${basePackage}${templatePackage}${package}." + DEFAULT_SPEC_PACKAGE + ";\n" +
                "\n" +
                "import ${basePackage}${entityPackage}${package}.${Entity};\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "/**\n" +
                " * ${Entity}规格约束\n" +
                " * ${Comment}\n" +
                " *\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Aggregate(aggregate = \"${Aggregate}\", name = \"${Entity}Specification\", type = Aggregate.TYPE_SPECIFICATION, description = \"${CommentEscaped}\")\n" +
                "@Service\n" +
                "public class ${Entity}Specification implements Specification<${Entity}> {\n" +
                "    @Override\n" +
                "    public Result specify(${Entity} entity) {\n" +
                "        return Result.fail(\"未实现\");\n" +
                "    }\n" +
                "}";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("specification");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_SPEC_PACKAGE + "${SEPARATOR}${Entity}Specification.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultDomainEventHandlerTemplateNode() {
        String template = "package ${basePackage}${templatePackage};\n" +
                "\n" +
                "import ${basePackage}${domainEventPackage}${package}.${DomainEvent};\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.context.event.EventListener;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "/**\n" +
                " * ${Entity}.${DomainEvent}领域事件订阅\n" +
                " * todo: 领域事件订阅描述\n" +
                " */\n" +
                "@Service\n" +
                "@RequiredArgsConstructor\n" +
                "public class ${DomainEvent}Subscriber {\n" +
                "\n" +
                "    @EventListener(${DomainEvent}.class)\n" +
                "    public void on(${DomainEvent} event) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}";

        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("domain_event_handler");
        templateNode.setName("${DomainEvent}Subscriber.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultDomainEventTemplateNode() {
        String template = "package ${basePackage}${templatePackage}${package}." + DEFAULT_DOMAIN_EVENT_PACKAGE + ";\n" +
                "\n" +
                "import lombok.AllArgsConstructor;\n" +
                "import lombok.Builder;\n" +
                "import lombok.Data;\n" +
                "import lombok.NoArgsConstructor;\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                "import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;\n" +
                "\n" +
                "/**\n" +
                " * ${Entity}.${DomainEvent}领域事件\n" +
                " * ${Comment}\n" +
                " *\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@DomainEvent(persist = false)\n" +
                "@Aggregate(aggregate = \"${Aggregate}\", name = \"${DomainEvent}\", type = Aggregate.TYPE_DOMAIN_EVENT, description = \"${CommentEscaped}\")\n" +
                "@Data\n" +
                "@Builder\n" +
                "@AllArgsConstructor\n" +
                "@NoArgsConstructor\n" +
                "public class ${DomainEvent} {\n" +
                "    Long id;\n" +
                "}";

        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("domain_event");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_DOMAIN_EVENT_PACKAGE + "${SEPARATOR}${DomainEvent}.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultEnumTemplateNode() {
        String template =
                "package ${basePackage}${templatePackage}${package}." + DEFAULT_ENUM_PACKAGE + ";\n" +
                        "\n" +
                        "import lombok.Getter;\n" +
                        "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                        "\n" +
                        "import javax.persistence.*;\n" +
                        "import java.util.HashMap;\n" +
                        "import java.util.Map;\n" +
                        "\n" +
                        "/**\n" +
                        " * 本文件由[cap4j-ddd-codegen-maven-plugin]生成\n" +
                        " * 警告：请勿手工修改该文件，重新生成会覆盖该文件\n" +
                        " * @author cap4j-ddd-codegen\n" +
                        " * @date ${date}\n" +
                        " */\n" +
                        "@Aggregate(aggregate = \"${Aggregate}\", name = \"${EnumType}\", type = \"enum\", description = \"${CommentEscaped}\")\n" +
                        "public enum ${EnumType} {\n" +
                        "\n" +
                        "${ENUM_ITEMS}\n" +
                        ";\n" +
                        "    @Getter\n" +
                        "    private int ${EnumValueField};\n" +
                        "    @Getter\n" +
                        "    private String ${EnumNameField};\n" +
                        "\n" +
                        "    ${EnumType}(Integer ${EnumValueField}, String ${EnumNameField}){\n" +
                        "        this.${EnumValueField} = ${EnumValueField};\n" +
                        "        this.${EnumNameField} = ${EnumNameField};\n" +
                        "    }\n" +
                        "\n" +
                        "\n" +
                        "    private static Map<Integer, ${EnumType}> enums = null;\n" +
                        "    public static ${EnumType} valueOf(Integer ${EnumValueField}) {\n" +
                        "        if(enums == null) {\n" +
                        "            enums = new HashMap<>();\n" +
                        "            for (${EnumType} val : ${EnumType}.values()) {\n" +
                        "                enums.put(val.${EnumValueField}, val);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        if(enums.containsKey(${EnumValueField})){\n" +
                        "            return enums.get(${EnumValueField});\n" +
                        "        }\n" +
                        (this.enumUnmatchedThrowException
                                ? "        throw new RuntimeException(\"枚举类型${EnumType}枚举值转换异常，不存在的值\" + ${EnumValueField});\n"
                                : "        return null;\n"
                        ) +
                        "    }\n" +
                        "\n" +
                        "    /**\n" +
                        "     * JPA转换器\n" +
                        "     */\n" +
                        "    public static class Converter implements AttributeConverter<${EnumType}, Integer>{\n" +
                        "\n" +
                        "        @Override\n" +
                        "        public Integer convertToDatabaseColumn(${EnumType}  val) {\n" +
                        "            return val.${EnumValueField};\n" +
                        "        }\n" +
                        "\n" +
                        "        @Override\n" +
                        "        public ${EnumType} convertToEntityAttribute(Integer ${EnumValueField}) {\n" +
                        "            return ${EnumType}.valueOf(${EnumValueField});\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("enum");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_ENUM_PACKAGE + "${SEPARATOR}${EnumType}.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultEnumItemTemplateNode() {
        String template = "    /**\n" +
                "     * ${itemDesc}\n" +
                "     */\n" +
                "    ${itemName}(${itemValue}, \"${itemDesc}\"),\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("segment");
        templateNode.setTag("enum_item");
        templateNode.setName("");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultSchemaFieldTemplateNode() {
        String template = "\n" +
                "    ${fieldComment}\n" +
                "    public ${SchemaBase}.Field<${fieldType}> ${fieldName}() {\n" +
                "        return root == null ? new ${SchemaBase}.Field<>(\"${fieldName}\") : new ${SchemaBase}.Field<>(root.get(\"${fieldName}\"));\n" +
                "    }\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("segment");
        templateNode.setTag("schema_field");
        templateNode.setName("");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultSchemaJoinTemplateNode() {
        String template = "\n" +
                "    /**\n" +
                "     * ${joinEntityType} 关联查询条件定义\n" +
                "     *\n" +
                "     * @param joinType\n" +
                "     * @return\n" +
                "     */\n" +
                "    public ${joinEntitySchemaPackage}${joinEntityType}Schema join${joinEntityType}(${SchemaBase}.JoinType joinType) {\n" +
                "        JoinType type = joinType.toJpaJoinType();\n" +
                "        Join<${Entity}, ${joinEntityPackage}.${joinEntityType}> join = ((Root<${Entity}>) root).join(\"${joinEntityVars}\", type);\n" +
                "        ${joinEntitySchemaPackage}${joinEntityType}Schema schema = new ${joinEntitySchemaPackage}${joinEntityType}Schema(join, criteriaBuilder);\n" +
                "        return schema;\n" +
                "    }";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("segment");
        templateNode.setTag("schema_join");
        templateNode.setName("");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    public TemplateNode getDefaultSchemaTemplateNode() {
        String template = "package ${basePackage}${templatePackage}${package}." + DEFAULT_SCHEMA_PACKAGE + ";\n" +
                "\n" +
                "import ${basePackage}${schemaBasePackage}.${SchemaBase};\n" +
                "import ${basePackage}${entityPackage}.${Entity};\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.data.domain.Sort;\n" +
                "import org.springframework.data.jpa.domain.Specification;\n" +
                "\n" +
                "import javax.persistence.criteria.*;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Collection;\n" +
                "import java.util.stream.Collectors;\n" +
                "\n" +
                "/**\n" +
                " * ${Comment}\n" +
                " * 本文件由[cap4j-ddd-codegen-maven-plugin]生成\n" +
                " * 警告：请勿手工修改该文件，重新生成会覆盖该文件\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@RequiredArgsConstructor\n" +
                "public class ${Entity}Schema {\n" +
                "    private final Path<${Entity}> root;\n" +
                "    private final CriteriaBuilder criteriaBuilder;\n" +
                "\n" +
                "    public CriteriaBuilder criteriaBuilder() {\n" +
                "        return criteriaBuilder;\n" +
                "    }\n" +
//                "\n" +
//                "    public ${SchemaBase}.Field<Long> ${IdField}() {\n" +
//                "        return root == null ? new ${SchemaBase}.Field<>(\"${IdField}\") : new ${SchemaBase}.Field<>(root.get(\"${IdField}\"));\n" +
//                "    }\n" +
                "${FIELD_ITEMS}\n" +
                "\n" +
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
                "    public Predicate spec(${SchemaBase}.PredicateBuilder<${Entity}Schema> builder){\n" +
                "        return builder.build(this);\n" +
                "    }\n" +
                "${JOIN_ITEMS}\n" +
                "\n" +
                "    /**\n" +
                "     * 构建查询条件\n" +
                "     * @param builder\n" +
                "     * @param distinct\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Specification<${Entity}> specify(${SchemaBase}.PredicateBuilder<${Entity}Schema> builder, boolean distinct) {\n" +
                "        return (root, criteriaQuery, criteriaBuilder) -> {\n" +
                "            ${Entity}Schema ${EntityVar} = new ${Entity}Schema(root, criteriaBuilder);\n" +
                "            criteriaQuery.where(builder.build(${EntityVar}));\n" +
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
                "    public static Specification<${Entity}> specify(${SchemaBase}.PredicateBuilder<${Entity}Schema> builder) {\n" +
                "        return (root, criteriaQuery, criteriaBuilder) -> {\n" +
                "            ${Entity}Schema ${EntityVar} = new ${Entity}Schema(root, criteriaBuilder);\n" +
                "            criteriaQuery.where(builder.build(${EntityVar}));\n" +
                "            return null;\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 构建排序\n" +
                "     * @param builders\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Sort orderBy(${SchemaBase}.OrderBuilder<${Entity}Schema>... builders) {\n" +
                "        return orderBy(Arrays.asList(builders));\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 构建排序\n" +
                "     *\n" +
                "     * @param builders\n" +
                "     * @return\n" +
                "     */\n" +
                "    public static Sort orderBy(Collection<${SchemaBase}.OrderBuilder<${Entity}Schema>> builders) {\n" +
                "        if(null == builders || builders.isEmpty()) {\n" +
                "            return Sort.unsorted();\n" +
                "        }\n" +
                "        return Sort.by(builders.stream()\n" +
                "                .map(builder -> builder.build(new ${Entity}Schema(null, null)))\n" +
                "                .collect(Collectors.toList())\n" +
                "        );\n" +
                "    }\n" +
                "\n" +
                "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("schema");
        templateNode.setName("${path}${SEPARATOR}" + DEFAULT_SCHEMA_PACKAGE + "${SEPARATOR}${Entity}Schema.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("overwrite");
        return templateNode;
    }

    public TemplateNode getDefaultSchemaBaseTemplateNode() {
        String template = "package ${basePackage}${templatePackage};\n" +
                "\n" +
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
                " * @author cap4j-ddd-codegen\n" +
                " */\n" +
                "public class ${SchemaBase} {\n" +
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
                "        RIGHT;\n" +
                "\n" +
                "        public javax.persistence.criteria.JoinType toJpaJoinType(){\n" +
                "            if(this == ${SchemaBase}.JoinType.INNER){\n" +
                "                return javax.persistence.criteria.JoinType.INNER;\n" +
                "            } else if(this == ${SchemaBase}.JoinType.LEFT){\n" +
                "                return javax.persistence.criteria.JoinType.LEFT;\n" +
                "            } else if(this == ${SchemaBase}.JoinType.RIGHT){\n" +
                "                return javax.persistence.criteria.JoinType.RIGHT;\n" +
                "            }\n" +
                "            return javax.persistence.criteria.JoinType.LEFT;\n" +
                "        }\n" +
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
                "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("schema_base");
        templateNode.setName("${SchemaBase}.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("overwrite");
        return templateNode;
    }
}
