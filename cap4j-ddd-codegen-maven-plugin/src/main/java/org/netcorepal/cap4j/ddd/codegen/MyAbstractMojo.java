package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基Mojo
 *
 * @author binking338
 * @date 2024/8/18
 */
public abstract class MyAbstractMojo extends AbstractMojo {

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
     * 主键字段名 默认'id'
     *
     * @parameter expression="${idField}"
     */
    @Parameter(property = "idField", defaultValue = "id")
    public String idField = "id";
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
     * 实体类附加导入包
     *
     * @parameter expression="${entityClassExtraImports}"
     */
    @Parameter(property = "entityClassExtraImports", defaultValue = "")
    public List<String> entityClassExtraImports = new ArrayList<>();

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
                "javax.persistence.*"
        );
        List<String> imports = new ArrayList<>(importList);
        imports.addAll(entityClassExtraImports);
        return imports.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 实体辅助类输出模式，绝对路径或相对路径，abs|ref
     *
     * @parameter expression="${entityMetaInfoClassOutputMode}"
     */
    @Parameter(property = "entityMetaInfoClassOutputMode", defaultValue = "")
    public String entityMetaInfoClassOutputMode = "abs";

    /**
     * 实体辅助类输出包
     *
     * @parameter expression="${entityMetaInfoClassOutputPackage}"
     */
    @Parameter(property = "entityMetaInfoClassOutputPackage", defaultValue = "domain._share.meta")
    public String entityMetaInfoClassOutputPackage = "domain._share.meta";

    /**
     * 关联实体加载模式 LAZY | EAGER
     *
     * @parameter expression="${fetchType}"
     */
    @Parameter(property = "fetchType", defaultValue = "EAGER")
    public String fetchType = "EAGER";
    /**
     * 关联实体加载模式 SUBSELECT | JOIN | SELECT
     *
     * @parameter expression="${fetchMode}"
     */
    @Parameter(property = "fetchMode", defaultValue = "SUBSELECT")
    public String fetchMode = "SUBSELECT";

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
    @Parameter(property = "datePackage4Java", defaultValue = "java.util")
    public String datePackage4Java = "java.util";
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
     * 是否生成EntitBuilder类
     *
     * @parameter expression="${generateBuild}"
     */
    @Parameter(property = "generateBuild", defaultValue = "false")
    public Boolean generateBuild = false;

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

    /**
     * 聚合仓储基类型
     *
     * @parameter expression="${aggregateRepositoryBaseClass}"
     */
    @Parameter(property = "aggregateRepositoryBaseClass", defaultValue = "")
    public String aggregateRepositoryBaseClass = "";

    public String getAggregateRepositoryBaseClass() {
        if (StringUtils.isBlank(aggregateRepositoryBaseClass)) {
            // 默认聚合仓储基类
            aggregateRepositoryBaseClass = "org.netcorepal.cap4j.ddd.domain.repo.AggregateRepository<${EntityType}, ${IdentityType}>";
        }
        return aggregateRepositoryBaseClass;
    }

    /**
     * 聚合仓储自定义代码
     *
     * @parameter expression="${aggregateRepositoryCustomerCode}"
     */
    @Parameter(property = "aggregateRepositoryCustomerCode", defaultValue = "")
    public String aggregateRepositoryCustomerCode = "";

    public String getAggregateRepositoryCustomerCode() {
        if (StringUtils.isBlank(aggregateRepositoryCustomerCode)) {
            aggregateRepositoryCustomerCode =
                    "@org.springframework.stereotype.Component\n" +
                            "    @org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate(aggregate = \"${Aggregate}\", name = \"${EntityType}\", type = \"repository\", description = \"\")\n" +
                            "    public static class ${EntityType}JpaRepositoryAdapter extends org.netcorepal.cap4j.ddd.domain.repo.AbstractJpaRepository<${EntityType}, ${IdentityType}>\n" +
                            "    {\n" +
                            "        public ${EntityType}JpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<${EntityType}> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<${EntityType}, ${IdentityType}> jpaRepository) {\n" +
                            "            super(jpaSpecificationExecutor, jpaRepository);\n" +
                            "        }\n" +
                            "    }" +
                            "";
        }
        return aggregateRepositoryCustomerCode;
    }

    /**
     * 跳过生成仓储的聚合根
     * 逗号','或分号';'分割
     *
     * @parameter expression="${ignoreAggregateRoots}"
     */
    @Parameter(property = "ignoreAggregateRoots", defaultValue = "")
    public String ignoreAggregateRoots = "";
}
