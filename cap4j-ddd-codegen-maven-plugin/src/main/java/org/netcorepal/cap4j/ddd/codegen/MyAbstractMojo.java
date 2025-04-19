package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils;
import org.netcorepal.cap4j.ddd.codegen.template.PathNode;
import org.netcorepal.cap4j.ddd.codegen.template.Template;
import org.netcorepal.cap4j.ddd.codegen.template.TemplateNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基Mojo
 *
 * @author binking338
 * @date 2024/8/18
 */
public abstract class MyAbstractMojo extends AbstractMojo {

    public static final String FLAG_DO_NOT_OVERWRITE = "[cap4j-ddd-codegen-maven-plugin:do-not-overwrite]";
    public final String PATTERN_SPLITTER = "[\\,\\;]";
    public final String PATTERN_DESIGN_PARAMS_SPLITTER = "[\\:]";
    public final String PATTERN_LINE_BREAK = "\\r\\n|[\\r\\n]";
    static final String AGGREGATE_REPOSITORY_PACKAGE = "adapter.domain.repositories";
    static final String AGGREGATE_PACKAGE = "domain.aggregates";
    static final String DOMAIN_EVENT_SUBSCRIBER_PACKAGE = "application.subscribers.domain";
    static final String INTEGRATION_EVENT_SUBSCRIBER_PACKAGE = "application.subscribers.integration";

    static final String DEFAULT_MUL_PRI_KEY_NAME = "Key";

    /**
     * 代码模板配置文件地址
     *
     * @parameter expression="${archTemplate}"
     */
    @Parameter(property = "archTemplate", defaultValue = "")
    public String archTemplate = "";

    /**
     * 代码模板配置文件编码，默认UFT-8
     *
     * @parameter expression="${archTemplateEncoding}"
     */
    @Parameter(property = "archTemplateEncoding", defaultValue = "UTF-8")
    public String archTemplateEncoding = "UTF-8";

    /**
     * 生成代码文件编码，默认UFT-8
     *
     * @parameter expression="${outputEncoding}"
     */
    @Parameter(property = "outputEncoding", defaultValue = "UTF-8")
    public String outputEncoding = "UTF-8";

    /**
     * 基础包路径
     *
     * @parameter expression="${basePackage}"
     */
    @Parameter(property = "basePackage", defaultValue = "")
    public String basePackage = "";

    /**
     * 是否多模块项目
     *
     * @parameter expression="${multiModule}"
     */
    @Parameter(property = "multiModule", defaultValue = "false")
    public Boolean multiModule = false;
    /**
     * adapter模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Adapter}"
     */
    @Parameter(property = "moduleNameSuffix4Adapter", defaultValue = "-adapter")
    public String moduleNameSuffix4Adapter = "-adapter";
    /**
     * domain模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Domain}"
     */
    @Parameter(property = "moduleNameSuffix4Domain", defaultValue = "-domain")
    public String moduleNameSuffix4Domain = "-domain";
    /**
     * application模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Application}"
     */
    @Parameter(property = "moduleNameSuffix4Application", defaultValue = "-application")
    public String moduleNameSuffix4Application = "-application";

    /**
     * 添加应用层或领域层设计元素（命令cmd、查询qry、集成事件event、防腐客户端cli...）
     *
     * @parameter expression="${design}"
     */
    @Parameter(property = "design", defaultValue = "")
    public String design = "";

    /**
     * 添加应用层或领域层设计元素（命令cmd、查询qry、集成事件event、防腐客户端cli...）
     *
     * @parameter expression="${designFile}"
     */
    @Parameter(property = "designFile", defaultValue = "")
    public String designFile = "";


    /**
     * 数据库连接地址
     *
     * @parameter expression="${connectionString}"
     */
    @Parameter(property = "connectionString")
    public String connectionString = "";
    /**
     * 数据库连接用户
     *
     * @parameter expression="${user}"
     */
    @Parameter(property = "user")
    public String user = "";
    /**
     * 数据库连接密码
     *
     * @parameter expression="${pwd}"
     */
    @Parameter(property = "pwd")
    public String pwd = "";
    /**
     * 数据库过滤
     *
     * @parameter expression="${schema}"
     */
    @Parameter(property = "schema")
    public String schema = "";
    /**
     * 数据表过滤
     * 逗号','或分号';'分割，支持通配符'%'
     *
     * @parameter expression="${table}"
     */
    @Parameter(property = "table", defaultValue = "")
    public String table = "";
    /**
     * 数据表忽略
     * 被忽略的表不生成实体
     *
     * @parameter expression="${ignoreTable}"
     */
    @Parameter(property = "ignoreTable", defaultValue = "")
    public String ignoreTable = "";
    /**
     * 乐观锁字段 默认'version'
     *
     * @parameter expression="${versionField}"
     */
    @Parameter(property = "versionField", defaultValue = "version")
    public String versionField = "version";
    /**
     * 软删字段 默认'deleted'
     *
     * @parameter expression="${deletedField}"
     */
    @Parameter(property = "deletedField", defaultValue = "deleted")
    public String deletedField = "deleted";
    /**
     * 只读字段
     * 逗号','或分号';'分割，不会通过ORM更新到数据库
     *
     * @parameter expression="${readonlyFields}"
     */
    @Parameter(property = "readonlyFields", defaultValue = "")
    public String readonlyFields = "";
    /**
     * 忽略字段
     * 逗号','或分号';'分割，不会通过ORM绑定到实体
     *
     * @parameter expression="${ignoreFields}"
     */
    @Parameter(property = "ignoreFields", defaultValue = "")
    public String ignoreFields = "";

    /**
     * 实体基类
     *
     * @parameter expression="${entityBaseClass}"
     */
    @Parameter(property = "entityBaseClass", defaultValue = "")
    public String entityBaseClass = "";

    /**
     * 根实体基类
     *
     * @parameter expression="${rootEntityBaseClass}"
     */
    @Parameter(property = "rootEntityBaseClass", defaultValue = "")
    public String rootEntityBaseClass = "";

    /**
     * 实体类附加导入包
     *
     * @parameter expression="${entityClassExtraImports}"
     */
    @Parameter(property = "entityClassExtraImports", defaultValue = "")
    public String entityClassExtraImports = "";

    public List<String> getEntityClassExtraImports() {
        List<String> importList = Arrays.asList(
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
                "org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate",
                "jakarta.persistence.*"
        );
        List<String> imports = new ArrayList<>(importList);
        imports.addAll(Arrays.stream(entityClassExtraImports.split(";"))
                .map(i -> i.trim().replaceAll(PATTERN_LINE_BREAK, ""))
                .map(i -> i.startsWith("import ") ? i.substring(6).trim() : i)
                .filter(i -> !StringUtils.isBlank(i))
                .collect(Collectors.toList()));
        return imports.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 实体辅助类输出模式，绝对路径或相对路径，abs|ref
     *
     * @parameter expression="${entitySchemaOutputMode}"
     */
    @Parameter(property = "entitySchemaOutputMode", defaultValue = "ref")
    public String entitySchemaOutputMode = "ref";

    public String getEntitySchemaOutputMode() {
        if (StringUtils.isBlank(entitySchemaOutputMode)) {
            entitySchemaOutputMode = "ref";
        }
        return entitySchemaOutputMode;
    }

    /**
     * 实体辅助类输出包
     *
     * @parameter expression="${entitySchemaOutputPackage}"
     */
    @Parameter(property = "entitySchemaOutputPackage", defaultValue = "domain._share.meta")
    public String entitySchemaOutputPackage = "domain._share.meta";

    public String getEntitySchemaOutputPackage() {
        if (StringUtils.isBlank(entitySchemaOutputPackage)) {
            entitySchemaOutputPackage = "domain._share.meta";
        }
        return entitySchemaOutputPackage;
    }

    /**
     * 实体辅助类输出名称模板
     *
     * @parameter expression="${entitySchemaNameTemplate}"
     */
    @Parameter(property = "entitySchemaNameTemplate", defaultValue = "S${Entity}")
    public String entitySchemaNameTemplate = "S${Entity}";


    /**
     * 关联实体加载模式 LAZY | EAGER
     *
     * @parameter expression="${fetchType}"
     */
    @Parameter(property = "fetchType", defaultValue = "EAGER")
    public String fetchType = "EAGER";

    /**
     * 主键生成器 默认自增策略
     *
     * @parameter expression="${idGenerator}"
     */
    @Parameter(property = "idGenerator", defaultValue = "")
    public String idGenerator = "";

    /**
     * 值对象主键生成器 默认md5哈希
     *
     * @parameter expression="${idGenerator4ValueObject}"
     */
    @Parameter(property = "idGenerator4ValueObject", defaultValue = "")
    public String idGenerator4ValueObject = "";
    /**
     * 值对象hash函数实现语句
     *
     * @parameter expression="${hashMethod4ValueObject}"
     */
    @Parameter(property = "hashMethod4ValueObject", defaultValue = "")
    public String hashMethod4ValueObject = "";
    /**
     * 枚举类型【值】字段名称
     *
     * @parameter expression="${enumValueField}"
     */
    @Parameter(property = "enumValueField", defaultValue = "value")
    public String enumValueField = "value";
    /**
     * 枚举类型【名】字段名称
     *
     * @parameter expression="${enumNameField}"
     */
    @Parameter(property = "enumNameField", defaultValue = "name")
    public String enumNameField = "name";
    /**
     * 枚举值转换不匹配时，是否抛出异常
     *
     * @parameter expression="${enumUnmatchedThrowException}"
     */
    @Parameter(property = "enumUnmatchedThrowException", defaultValue = "true")
    public Boolean enumUnmatchedThrowException = true;

    /**
     * 日期类型映射使用的包，java.util | java.time，默认java.util
     *
     * @parameter expression="${datePackage}"
     */
    @Parameter(property = "datePackage4Java", defaultValue = "java.time")
    public String datePackage4Java = "java.time";
    /**
     * 自定义数据库字段【类型】到【代码类型】映射
     *
     * @parameter expression="${typeRemapping}"
     */
    @Parameter(property = "typeRemapping", defaultValue = "")
    public Map<String, String> typeRemapping = new HashMap<>();

    /**
     * 实体字段是否生成默认值，来源数据库默认值
     *
     * @parameter expression="${generateDefault}"
     */
    @Parameter(property = "generateDefault", defaultValue = "false")
    public Boolean generateDefault = false;
    /**
     * 实体字段注释是否包含生成数据库字段类型
     *
     * @parameter expression="${generateDbType}"
     */
    @Parameter(property = "generateDbType", defaultValue = "false")
    public Boolean generateDbType = false;
    /**
     * 是否生成Schema类，辅助Jpa查询
     *
     * @parameter expression="${generateSchema}"
     */
    @Parameter(property = "generateSchema", defaultValue = "false")
    public Boolean generateSchema = false;
    /**
     * 是否生成聚合封装类
     *
     * @parameter expression="${generateAggregate}"
     */
    @Parameter(property = "generateAggregate", defaultValue = "false")
    public Boolean generateAggregate = false;
    /**
     * 是否生成关联父实体字段
     *
     * @parameter expression="${generateParent}"
     */
    @Parameter(property = "generateParent", defaultValue = "false")
    public Boolean generateParent = false;

    /**
     * 聚合根名称模板
     *
     * @parameter expression="${repositoryNameTemplate}"
     */
    @Parameter(property = "repositoryNameTemplate", defaultValue = "${Entity}Repository")
    public String repositoryNameTemplate = "${Entity}Repository";

    /**
     * 是否支持querydsl方式检索仓储实体
     *
     * @parameter expression="${generateRepository}"
     */
    @Parameter(property = "repositorySupportQuerydsl", defaultValue = "true")
    public Boolean repositorySupportQuerydsl = true;

    /**
     * 聚合根名称模板
     *
     * @parameter expression="${aggregateNameTemplate}"
     */
    @Parameter(property = "aggregateNameTemplate", defaultValue = "Agg${Entity}")
    public String aggregateNameTemplate = "Agg${Entity}";

    /**
     * 聚合根注解
     *
     * @parameter expression="${aggregateRootAnnotation}"
     */
    @Parameter(property = "aggregateRootAnnotation", defaultValue = "")
    public String aggregateRootAnnotation = "";


    public String getAggregateRootAnnotation() {
        if (StringUtils.isNotEmpty(aggregateRootAnnotation)) {
            aggregateRootAnnotation = aggregateRootAnnotation.trim();
            if (!aggregateRootAnnotation.startsWith("@")) {
                aggregateRootAnnotation = "@" + aggregateRootAnnotation;
            }
        }
        return aggregateRootAnnotation;
    }

    public String getProjectDir() {
        String projectDir = new File("").getAbsolutePath();
        if (multiModule) {
            projectDir = new File(new File(projectDir).getParent() + File.separator + "pom.xml").exists()
                    ? new File(projectDir).getParent()
                    : projectDir;
        }
        return projectDir;
    }


    public String getAdapterModulePath() {
        String adapterModulePath = "";
        if (multiModule) {
            adapterModulePath = getProjectDir() + File.separator + getProjectArtifactId() + moduleNameSuffix4Adapter;
        } else {
            adapterModulePath = getProjectDir();
        }
        return adapterModulePath;
    }

    public String getDomainModulePath() {
        String adapterModulePath = "";
        if (multiModule) {
            adapterModulePath = getProjectDir() + File.separator + getProjectArtifactId() + moduleNameSuffix4Domain;
        } else {
            adapterModulePath = getProjectDir();
        }
        return adapterModulePath;
    }

    public String getApplicationModulePath() {
        String adapterModulePath = "";
        if (multiModule) {
            adapterModulePath = getProjectDir() + File.separator + getProjectArtifactId() + moduleNameSuffix4Application;
        } else {
            adapterModulePath = getProjectDir();
        }
        return adapterModulePath;
    }


    protected Template template = null;
    boolean renderFileSwitch = true;

    public String forceRender(PathNode pathNode, String parentPath) throws IOException {
        boolean temp = renderFileSwitch;
        renderFileSwitch = true;
        String path = render(pathNode, parentPath);
        renderFileSwitch = temp;
        return path;
    }

    /**
     * 构建路径节点
     *
     * @param pathNode
     * @param parentPath
     * @throws IOException
     */
    public String render(PathNode pathNode, String parentPath) throws IOException {
        String path = parentPath;
        switch (pathNode.getType()) {
            case "file":
                path = renderFile(pathNode, parentPath);
                break;
            case "dir":
                path = renderDir(pathNode, parentPath);
                if (pathNode.getChildren() != null) {
                    for (PathNode childPathNode : pathNode.getChildren()) {
                        render(childPathNode, path);
                    }
                }
                break;
            case "root":
                if (pathNode.getChildren() != null) {
                    for (PathNode childPathNode : pathNode.getChildren()) {
                        render(childPathNode, parentPath);
                    }
                }
                break;
        }
        return path;
    }

    /**
     * @param pathNode
     * @param parentPath
     * @return
     * @throws IOException
     */
    public String renderDir(PathNode pathNode, String parentPath) throws IOException {
        if (!"dir".equalsIgnoreCase(pathNode.getType())) {
            throw new RuntimeException("节点类型必须是目录");
        }
        if (pathNode.getName() == null || pathNode.getName().isEmpty()) {
            return parentPath;
        }
        String path = StringUtils.isNotBlank(pathNode.getName())
                ? parentPath + File.separator + pathNode.getName()
                : parentPath;
        if (FileUtils.fileExists(path)) {
            switch (pathNode.getConflict()) {
                case "warn":
                    getLog().warn("目录存在：" + path);
                    break;
                case "overwrite":
                    getLog().info("目录覆盖：" + path);
                    FileUtils.deleteDirectory(path);
                    FileUtils.mkdir(path);
                    break;
                case "skip":
                    getLog().debug("目录存在：" + path);
                    break;
            }
        } else {
            getLog().info("目录创建：" + path);
            FileUtils.mkdir(path);
        }

        if (StringUtils.isNotBlank(pathNode.getTag())) {
            String[] tags = pathNode.getTag().split(PATTERN_SPLITTER);
            for (String tag : tags) {
                List<TemplateNode> templateNodes = template.select(tag);
                if (null != templateNodes) {
                    renderTemplate(templateNodes, path);
                }
            }
        }

        return path;
    }

    /**
     * 生成设计框架代码
     *
     * @param templateNodes
     * @param parentPath
     * @throws IOException
     */
    public void renderTemplate(List<TemplateNode> templateNodes, String parentPath) throws IOException {

    }

    /**
     * @param pathNode
     * @param parentPath
     * @return
     */
    public String renderFile(PathNode pathNode, String parentPath) throws IOException {
        if (!"file".equalsIgnoreCase(pathNode.getType())) {
            throw new RuntimeException("节点类型必须是文件");
        }
        if (pathNode.getName() == null || pathNode.getName().isEmpty()) {
            throw new RuntimeException("模板节点配置 name 不得为空 parentPath = " + parentPath);
        }
        String path = parentPath + File.separator + pathNode.getName();
        if (!renderFileSwitch) {
            return path;
        }

        String content = pathNode.getData();
        if (FileUtils.fileExists(path)) {
            switch (pathNode.getConflict()) {
                case "warn":
                    getLog().warn("文件存在：" + path);
                    break;
                case "overwrite":
                    if (!FileUtils.fileRead(path, outputEncoding).contains(FLAG_DO_NOT_OVERWRITE)) {
                        getLog().info("文件覆盖：" + path);
                        FileUtils.fileDelete(path);
                        FileUtils.fileWrite(path, outputEncoding, content);
                    } else {
                        getLog().info("跳过覆盖，文件内容包含 " + FLAG_DO_NOT_OVERWRITE + "：" + path);
                    }
                    break;
                case "skip":
                default:
                    getLog().debug("文件跳过：" + path);
                    break;
            }
        } else {
            getLog().info("文件创建：" + path);
            FileUtils.mkdir(FileUtils.getPath(path));
            FileUtils.fileWrite(path, outputEncoding, content);
        }
        return path;
    }

    public String generateDomainEventName(String eventName) {
        String domainEventClassName = NamingUtils.toUpperCamelCase(eventName);
        if (!domainEventClassName.endsWith("Event") && !domainEventClassName.endsWith("Evt")) {
            domainEventClassName += "DomainEvent";
        }
        return domainEventClassName;
    }

    public String generateDomainServiceName(String svcName) {
        if (!svcName.endsWith("Svc") && !svcName.endsWith("Service")) {
            svcName += "DomainService";
        }
        return svcName;
    }

    public List<String> alias4Template(String tag, String var) {
        List<String> aliases = null;
        switch (tag + "." + var) {
            case "schema.Comment":
            case "enum.Comment":
            case "domain_event.Comment":
            case "domain_event_handler.Comment":
            case "specification.Comment":
            case "factory.Comment":
            case "domain_service.Comment":
            case "integration_event.Comment":
            case "integration_event_handler.Comment":
            case "client.Comment":
            case "query.Comment":
            case "command.Comment":
            case "client_handler.Comment":
            case "query_handler.Comment":
            case "command_handler.Comment":
            case "saga.Comment":
                aliases = Arrays.asList(
                        var,
                        "comment",
                        "COMMENT"
                );
                break;
            case "schema.CommentEscaped":
            case "enum.CommentEscaped":
            case "domain_event.CommentEscaped":
            case "domain_event_handler.CommentEscaped":
            case "specification.CommentEscaped":
            case "factory.CommentEscaped":
            case "domain_service.CommentEscaped":
            case "integration_event.CommentEscaped":
            case "integration_event_handler.CommentEscaped":
            case "client.CommentEscaped":
            case "query.CommentEscaped":
            case "command.CommentEscaped":
            case "client_handler.CommentEscaped":
            case "query_handler.CommentEscaped":
            case "command_handler.CommentEscaped":
            case "saga.CommentEscaped":
                aliases = Arrays.asList(
                        var,
                        "commentEscaped",
                        "COMMENT_ESCAPED",
                        "Comment_Escaped"
                );
                break;
            case "schema.Aggregate":
            case "enum.Aggregate":
            case "domain_event.Aggregate":
            case "domain_event_handler.Aggregate":
            case "specification.Aggregate":
            case "factory.Aggregate":
                aliases = Arrays.asList(
                        var,
                        "aggregate",
                        "AGGREGATE"
                );
                break;
            case "schema.entityPackage":
            case "enum.entityPackage":
            case "domain_event.entityPackage":
            case "domain_event_handler.entityPackage":
            case "specification.entityPackage":
            case "factory.entityPackage":
                aliases = Arrays.asList(
                        var,
                        "EntityPackage",
                        "ENTITY_PACKAGE",
                        "entity_package",
                        "Entity_Package"
                );
                break;
            case "schema.templatePackage":
            case "enum.templatePackage":
            case "domain_event.templatePackage":
            case "domain_event_handler.templatePackage":
            case "specification.templatePackage":
            case "factory.templatePackage":
                aliases = Arrays.asList(
                        var,
                        "TemplatePackage",
                        "TEMPLATE_PACKAGE",
                        "template_package",
                        "Template_Package"
                );
                break;
            case "schema.Entity":
            case "enum.Entity":
            case "domain_event.Entity":
            case "domain_event_handler.Entity":
            case "specification.Entity":
            case "factory.Entity":
                aliases = Arrays.asList(
                        var,
                        "entity",
                        "ENTITY",
                        "entityType",
                        "EntityType",
                        "ENTITY_TYPE",
                        "Entity_Type",
                        "entity_type"
                );
                break;
            case "schema.EntityVar":
            case "enum.EntityVar":
            case "domain_event.EntityVar":
            case "domain_event_handler.EntityVar":
            case "specification.EntityVar":
            case "factory.EntityVar":
                aliases = Arrays.asList(
                        var,
                        "entityVar",
                        "ENTITY_VAR",
                        "entity_var",
                        "Entity_Var"
                );
                break;
            case "schema_base.SchemaBase":
            case "schema.SchemaBase":
                aliases = Arrays.asList(
                        var,
                        "schema_base",
                        "SCHEMA_BASE"
                );
                break;
            case "schema.IdField":
                aliases = Arrays.asList(
                        var,
                        "idField",
                        "ID_FIELD",
                        "id_field",
                        "Id_Field"
                );
                break;
            case "schema.FIELD_ITEMS":
                aliases = Arrays.asList(
                        var,
                        "fieldItems",
                        "field_items",
                        "Field_Items"
                );
                break;
            case "schema.JOIN_ITEMS":
                aliases = Arrays.asList(
                        var,
                        "joinItems",
                        "join_items",
                        "Join_Items"
                );
                break;
            case "schema_field.fieldType":
                aliases = Arrays.asList(
                        var,
                        "FIELD_TYPE",
                        "field_type",
                        "Field_Type"
                );
                break;
            case "schema_field.fieldName":
                aliases = Arrays.asList(
                        var,
                        "FIELD_NAME",
                        "field_name",
                        "Field_Name"
                );
                break;
            case "schema_field.fieldComment":
                aliases = Arrays.asList(
                        var,
                        "FIELD_COMMENT",
                        "field_comment",
                        "Field_Comment"
                );
                break;
            case "enum.Enum":
                aliases = Arrays.asList(
                        var,
                        "enum",
                        "ENUM",
                        "EnumType",
                        "enumType",
                        "ENUM_TYPE",
                        "enum_type",
                        "Enum_Type"
                );
                break;
            case "enum.EnumValueField":
                aliases = Arrays.asList(
                        var,
                        "enumValueField",
                        "ENUM_VALUE_FIELD",
                        "enum_value_field",
                        "Enum_Value_Field"
                );
                break;
            case "enum.EnumNameField":
                aliases = Arrays.asList(
                        var,
                        "enumNameField",
                        "ENUM_NAME_FIELD",
                        "enum_name_field",
                        "Enum_Name_Field"
                );
                break;
            case "enum.ENUM_ITEMS":
                aliases = Arrays.asList(
                        var,
                        "enumItems",
                        "enum_items",
                        "Enum_Items"
                );
                break;
            case "enum_item.itemName":
                aliases = Arrays.asList(
                        var,
                        "ItemName",
                        "ITEM_NAME",
                        "item_name",
                        "Item_Name"
                );
                break;
            case "enum_item.itemValue":
                aliases = Arrays.asList(
                        var,
                        "ItemValue",
                        "ITEM_VALUE",
                        "item_value",
                        "Item_Value"
                );
                break;
            case "enum_item.itemDesc":
                aliases = Arrays.asList(
                        var,
                        "ItemDesc",
                        "ITEM_DESC",
                        "item_desc",
                        "Item_Desc"
                );
                break;
            case "domain_event.DomainEvent":
            case "domain_event_handler.DomainEvent":
                aliases = Arrays.asList(
                        var,
                        "domainEvent",
                        "DOMAIN_EVENT",
                        "domain_event",
                        "Domain_Event",
                        "Event",
                        "EVENT",
                        "event",
                        "DE",
                        "D_E",
                        "de",
                        "d_e"
                );
                break;
            case "domain_event.persist":
            case "domain_event_handler.persist":
                aliases = Arrays.asList(
                        var,
                        "Persist",
                        "PERSIST"
                );
                break;
            case "domain_service.DomainService":
                aliases = Arrays.asList(
                        var,
                        "domainService",
                        "DOMAIN_SERVICE",
                        "domain_service",
                        "Domain_Service",
                        "Service",
                        "SERVICE",
                        "service",
                        "Svc",
                        "SVC",
                        "svc",
                        "DS",
                        "D_S",
                        "ds",
                        "d_s"
                );
                break;
            case "specification.Specification":
                aliases = Arrays.asList(
                        var,
                        "specification",
                        "SPECIFICATION",
                        "Spec",
                        "SPEC",
                        "spec"
                );
                break;
            case "factory.Factory":
                aliases = Arrays.asList(
                        var,
                        "factory",
                        "FACTORY",
                        "Fac",
                        "FAC",
                        "fac"
                );
                break;
            case "integration_event.IntegrationEvent":
            case "integration_event_handler.IntegrationEvent":
                aliases = Arrays.asList(
                        var,
                        "integrationEvent",
                        "integration_event",
                        "INTEGRATION_EVENT",
                        "Integration_Event",
                        "Event",
                        "EVENT",
                        "event",
                        "IE",
                        "I_E",
                        "ie",
                        "i_e"
                );
                break;
            case "specification.AggregateRoot":
            case "factory.AggregateRoot":
            case "domain_event.AggregateRoot":
            case "domain_event_handler.AggregateRoot":
                aliases = Arrays.asList(
                        var,
                        "aggregateRoot",
                        "aggregate_root",
                        "AGGREGATE_ROOT",
                        "Aggregate_Root",
                        "Root",
                        "ROOT",
                        "root",
                        "AR",
                        "A_R",
                        "ar",
                        "a_r"
                );
                break;
            case "client.Client":
            case "client_handler.Client":
                aliases = Arrays.asList(
                        var,
                        "client",
                        "CLIENT",
                        "Cli",
                        "CLI",
                        "cli"
                );
                break;
            case "query.Query":
            case "query_handler.Query":
                aliases = Arrays.asList(
                        var,
                        "query",
                        "QUERY",
                        "Qry",
                        "QRY",
                        "qry"
                );
                break;
            case "command.Command":
            case "command_handler.Command":
                aliases = Arrays.asList(
                        var,
                        "command",
                        "COMMAND",
                        "Cmd",
                        "CMD",
                        "cmd"
                );
                break;
            case "client.Request":
            case "client_handler.Request":
            case "query.Request":
            case "query_handler.Request":
            case "command.Request":
            case "command_handler.Request":
                aliases = Arrays.asList(
                        var,
                        "request",
                        "REQUEST",
                        "Req",
                        "REQ",
                        "req",
                        "Param",
                        "PARAM",
                        "param"
                );
                break;
            case "client.Response":
            case "client_handler.Response":
            case "query.Response":
            case "query_handler.Response":
            case "command.Response":
            case "command_handler.Response":
            case "saga.Response":
            case "saga_handler.Response":
                aliases = Arrays.asList(
                        var,
                        "response",
                        "RESPONSE",
                        "Res",
                        "RES",
                        "res",
                        "ReturnType",
                        "returnType",
                        "RETURN_TYPE",
                        "return_type",
                        "Return_Type",
                        "Return",
                        "RETURN",
                        "return"
                );
                break;
            default:
                aliases = Arrays.asList(var);
                break;
        }
        return aliases;
    }

    public void putContext(String tag, String var, String val, Map<String, String> context) {
        List<String> aliases = alias4Template(tag, var);
        for (String alias :
                aliases) {
            context.put(alias, val);
        }
    }

    public String escape(String content) {
        return content
                .replace("\\\\", "${symbol_escape}")
                .replace("\\:", "${symbol_colon}")
                .replace("\\,", "${symbol_comma}")
                .replace("\\;", "${symbol_semicolon}");
    }

    public String unescape(String escape) {
        return escape
                .replace("${symbol_escape}", "\\")
                .replace("${symbol_colon}", ":")
                .replace("${symbol_comma}", ",")
                .replace("${symbol_semicolon}", ";");
    }

    protected String projectGroupId = "";
    protected String projectArtifactId = "";
    protected String projectVersion = "";

    protected void resolveMavenProject() {
        MavenProject mavenProject = ((MavenProject) getPluginContext().get("project"));
        if (mavenProject != null && StringUtils.isBlank(projectArtifactId)) {
            if (!multiModule || !mavenProject.hasParent()) {
                projectGroupId = mavenProject.getGroupId();
                projectArtifactId = mavenProject.getArtifactId();
                projectVersion = mavenProject.getVersion();
            } else {
                projectGroupId = mavenProject.getParent().getGroupId();
                projectArtifactId = mavenProject.getParent().getArtifactId();
                projectVersion = mavenProject.getParent().getVersion();
            }
        }
    }

    protected String getProjectGroupId() {
        resolveMavenProject();
        return projectGroupId;
    }

    protected String getProjectArtifactId() {
        resolveMavenProject();
        return projectArtifactId;
    }

    protected String getProjectVersion() {
        resolveMavenProject();
        return projectVersion;
    }

    public Map<String, String> getEscapeContext() {
        Map<String, String> context = new HashMap<>();
        context.put("cap4jPluginConfiguration", stringfyCap4jPluginConfiguration());
        context.put("groupId", getProjectGroupId());
        context.put("artifactId", getProjectArtifactId());
        context.put("version", getProjectVersion());
        context.put("archTemplate", archTemplate);
        context.put("archTemplateEncoding", archTemplateEncoding);
        context.put("outputEncoding", outputEncoding);
        context.put("designFile", designFile);
        context.put("basePackage", basePackage);
        context.put("basePackage__as_path", basePackage.replace(".", File.separator));
        context.put("multiModule", multiModule ? "true" : "false");
        context.put("moduleNameSuffix4Adapter", moduleNameSuffix4Adapter);
        context.put("moduleNameSuffix4Domain", moduleNameSuffix4Domain);
        context.put("moduleNameSuffix4Application", moduleNameSuffix4Application);
        context.put("connectionString", connectionString);
        context.put("user", user);
        context.put("pwd", pwd);
        context.put("schema", schema);
        context.put("table", table);
        context.put("ignoreTable", ignoreTable);
        context.put("versionField", versionField);
        context.put("deletedField", deletedField);
        context.put("readonlyFields", readonlyFields);
        context.put("ignoreFields", ignoreFields);
        context.put("entityBaseClass", entityBaseClass);
        context.put("rootEntityBaseClass", rootEntityBaseClass);
        context.put("entityClassExtraImports", entityClassExtraImports);
        context.put("entitySchemaOutputPackage", entitySchemaOutputPackage);
        context.put("entitySchemaOutputMode", entitySchemaOutputMode);
        context.put("entitySchemaNameTemplate", entitySchemaNameTemplate);
        context.put("idGenerator", idGenerator);
        context.put("idGenerator4ValueObject", idGenerator4ValueObject);
        context.put("hashMethod4ValueObject", hashMethod4ValueObject);
        context.put("fetchType", fetchType);
        context.put("enumValueField", enumValueField);
        context.put("enumNameField", enumNameField);
        context.put("enumUnmatchedThrowException", enumUnmatchedThrowException ? "true" : "false");
        context.put("datePackage4Java", datePackage4Java);
        context.put("typeRemapping", stringfyTypeRemapping());
        context.put("generateDefault", generateDefault ? "true" : "false");
        context.put("generateDbType", generateDbType ? "true" : "false");
        context.put("generateSchema", generateSchema ? "true" : "false");
        context.put("generateAggregate", generateAggregate ? "true" : "false");
        context.put("generateParent", generateParent ? "true" : "false");
        context.put("aggregateRootAnnotation", aggregateRootAnnotation);
        context.put("aggregateNameTemplate", aggregateNameTemplate);
        context.put("repositoryNameTemplate", repositoryNameTemplate);
        context.put("repositorySupportQuerydsl", repositorySupportQuerydsl ? "true" : "false");
        context.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        context.put("SEPARATOR", File.separator);
        context.put("separator", File.separator);
        return context;
    }

    protected String stringfyCap4jPluginConfiguration() {
        return "<configuration>\n" +
                "                    <basePackage>" + basePackage + "</basePackage>\n" +
                "                    <archTemplate>" + archTemplate + "</archTemplate>\n" +
                "                    <archTemplateEncoding>" + archTemplateEncoding + "</archTemplateEncoding>\n" +
                "                    <outputEncoding>" + outputEncoding + "</outputEncoding>\n" +
                "                    <designFile>" + designFile + "</designFile>\n" +
                "                    <multiModule>" + multiModule + "</multiModule>\n" +
                (StringUtils.equalsIgnoreCase("-adapter", moduleNameSuffix4Adapter)
                        ? ""
                        : "                    <moduleNameSuffix4Adapter>" + moduleNameSuffix4Adapter + "</moduleNameSuffix4Adapter>\n"
                ) +
                (StringUtils.equalsIgnoreCase("-domain", moduleNameSuffix4Domain)
                        ? ""
                        : "                    <moduleNameSuffix4Domain>" + moduleNameSuffix4Domain + "</moduleNameSuffix4Domain>\n"
                ) +
                (StringUtils.equalsIgnoreCase("-application", moduleNameSuffix4Application)
                        ? ""
                        : "                    <moduleNameSuffix4Application>" + moduleNameSuffix4Application + "</moduleNameSuffix4Application>\n"
                ) +
                "                    <connectionString>\n" +
                "                        <![CDATA[" + connectionString + "]]>\n" +
                "                    </connectionString>\n" +
                "                    <user>" + user + "</user>\n" +
                "                    <pwd>" + pwd + "</pwd>\n" +
                "                    <schema>" + schema + "</schema>\n" +
                "                    <table>" + table + "</table>\n" +
                (StringUtils.isBlank(ignoreTable)
                        ? ""
                        : "                    <ignoreTable>" + ignoreTable + "</ignoreTable>\n") +
                (StringUtils.isBlank(ignoreFields)
                        ? ""
                        : "                    <ignoreFields>" + ignoreFields + "</ignoreFields>\n") +
                (StringUtils.isBlank(versionField)
                        ? ""
                        : "                    <versionField>" + versionField + "</versionField>\n") +
                (StringUtils.isBlank(deletedField)
                        ? ""
                        : "                    <deletedField>" + deletedField + "</deletedField>\n") +
                (StringUtils.isBlank(readonlyFields)
                        ? ""
                        : "                    <readonlyFields>" + readonlyFields + "</readonlyFields>\n") +
                (StringUtils.isBlank(entityBaseClass)
                        ? ""
                        : "                    <entityBaseClass>" + entityBaseClass + "</entityBaseClass>\n") +
                (StringUtils.isBlank(rootEntityBaseClass)
                        ? ""
                        : "                    <rootEntityBaseClass>" + rootEntityBaseClass + "</rootEntityBaseClass>\n") +
                (StringUtils.isBlank(entityClassExtraImports)
                        ? ""
                        : "                    <entityClassExtraImports>" + entityClassExtraImports + "</entityClassExtraImports>\n") +
                (StringUtils.equalsIgnoreCase("ref", entitySchemaOutputMode)
                        ? ""
                        : "                    <entitySchemaOutputMode>" + entitySchemaOutputMode + "</entitySchemaOutputMode>\n") +
                (StringUtils.equalsIgnoreCase("domain._share.meta", entitySchemaOutputPackage)
                        ? ""
                        : "                    <entitySchemaOutputPackage>" + entitySchemaOutputPackage + "</entitySchemaOutputPackage>\n") +
                (StringUtils.isBlank(entitySchemaNameTemplate)
                        ? ""
                        : "                    <entitySchemaNameTemplate>" + entitySchemaNameTemplate + "</entitySchemaNameTemplate>\n") +
                (StringUtils.isBlank(idGenerator)
                        ? ""
                        : "                    <idGenerator>" + idGenerator + "</idGenerator>\n") +
                (StringUtils.isBlank(idGenerator4ValueObject)
                        ? ""
                        : "                    <idGenerator4ValueObject>" + idGenerator4ValueObject + "</idGenerator4ValueObject>\n") +
                (StringUtils.isBlank(hashMethod4ValueObject)
                        ? ""
                        : "                    <hashMethod4ValueObject>" + hashMethod4ValueObject + "</hashMethod4ValueObject>\n") +
                (StringUtils.equalsIgnoreCase("EAGER", fetchType)
                        ? ""
                        : "                    <fetchType>" + fetchType + "</fetchType>\n") +
                (StringUtils.equalsIgnoreCase("value", enumValueField)
                        ? ""
                        : "                    <enumValueField>" + enumValueField + "</enumValueField>\n") +
                (StringUtils.equalsIgnoreCase("name", enumNameField)
                        ? ""
                        : "                    <enumNameField>" + enumNameField + "</enumNameField>\n") +
                (enumUnmatchedThrowException
                        ? ""
                        : "                    <enumUnmatchedThrowException>" + enumUnmatchedThrowException + "</enumUnmatchedThrowException>\n") +
                (StringUtils.equalsIgnoreCase("java.time", datePackage4Java)
                        ? ""
                        : "                    <datePackage4Java>" + datePackage4Java + "</datePackage4Java>\n") +
                (typeRemapping.isEmpty()
                        ? ""
                        : "                    <typeRemapping>" + stringfyTypeRemapping() + "</typeRemapping>\n") +
                (!generateDefault
                        ? ""
                        : "                    <generateDefault>" + generateDefault + "</generateDefault>\n") +
                (!generateDbType
                        ? ""
                        : "                    <generateDbType>" + generateDbType + "</generateDbType>\n") +
                (!generateSchema
                        ? ""
                        : "                    <generateSchema>" + generateSchema + "</generateSchema>\n") +
                (!generateAggregate
                        ? ""
                        : "                    <generateAggregate>" + generateAggregate + "</generateAggregate>\n") +
                (!generateParent
                        ? ""
                        : "                    <generateParent>" + generateParent + "</generateParent>\n") +
                (StringUtils.isBlank(repositoryNameTemplate)
                        ? ""
                        : "                    <repositoryNameTemplate>" + repositoryNameTemplate + "</repositoryNameTemplate>\n") +
                "                    <repositorySupportQuerydsl>" + repositorySupportQuerydsl + "</repositorySupportQuerydsl>\n" +
                (StringUtils.isBlank(aggregateRootAnnotation)
                        ? ""
                        : "                    <aggregateRootAnnotation>" + aggregateRootAnnotation + "</aggregateRootAnnotation>\n") +
                (StringUtils.isBlank(aggregateNameTemplate)
                        ? ""
                        : "                    <aggregateNameTemplate>" + aggregateNameTemplate + "</aggregateNameTemplate>\n") +
                "                </configuration>";
    }

    protected String stringfyTypeRemapping() {
        if (typeRemapping == null || typeRemapping.isEmpty()) {
            return "";
        }
        String result = "";
        for (Map.Entry<String, String> kv :
                typeRemapping.entrySet()) {
            result += "<" + kv.getKey() + ">" + kv.getValue() + "</" + kv.getKey() + ">";
        }
        return result;
    }
}
