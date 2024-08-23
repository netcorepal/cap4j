package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.Map;

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
    String archTemplate = "";

    /**
     * 代码模板配置文件编码，默认UFT-8
     *
     * @parameter expression="${archTemplateEncoding}"
     */
    @Parameter(property = "archTemplateEncoding", defaultValue = "UTF-8")
    String archTemplateEncoding = "UTF-8";

    /**
     * 基础包路径
     *
     * @parameter expression="${basePackage}"
     */
    @Parameter(property = "basePackage", defaultValue = "")
    String basePackage = "";

    /**
     * 是否多模块项目
     *
     * @parameter expression="${multiModule}"
     */
    @Parameter(property = "multiModule", defaultValue = "false")
    Boolean multiModule = false;
    /**
     * adapter模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Adapter}"
     */
    @Parameter(property = "moduleNameSuffix4Adapter", defaultValue = "-adapter")
    String moduleNameSuffix4Adapter = "-adapter";
    /**
     * domain模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Domain}"
     */
    @Parameter(property = "moduleNameSuffix4Domain", defaultValue = "-domain")
    String moduleNameSuffix4Domain = "-domain";
    /**
     * application模块名称后缀
     *
     * @parameter expression="${moduleNameSuffix4Application}"
     */
    @Parameter(property = "moduleNameSuffix4Application", defaultValue = "-application")
    String moduleNameSuffix4Application = "-application";

    /**
     * 数据库连接地址
     *
     * @parameter expression="${connectionString}"
     */
    @Parameter(property = "connectionString")
    String connectionString = "";
    /**
     * 数据库连接用户
     *
     * @parameter expression="${user}"
     */
    @Parameter(property = "user")
    String user = "";
    /**
     * 数据库连接密码
     *
     * @parameter expression="${pwd}"
     */
    @Parameter(property = "pwd")
    String pwd = "";
    /**
     * 数据库过滤
     *
     * @parameter expression="${schema}"
     */
    @Parameter(property = "schema")
    String schema = "";
    /**
     * 数据表过滤
     * 逗号','或分号';'分割，支持通配符'%'
     *
     * @parameter expression="${table}"
     */
    @Parameter(property = "table", defaultValue = "")
    String table = "";
    /**
     * 数据表忽略
     * 被忽略的表不生成实体
     *
     * @parameter expression="${ignoreTable}"
     */
    @Parameter(property = "ignoreTable", defaultValue = "")
    String ignoreTable = "";
    /**
     * 主键字段名 默认'id'
     *
     * @parameter expression="${idField}"
     */
    @Parameter(property = "idField", defaultValue = "id")
    String idField = "id";
    /**
     * 乐观锁字段 默认'version'
     *
     * @parameter expression="${versionField}"
     */
    @Parameter(property = "versionField", defaultValue = "version")
    String versionField = "version";
    /**
     * 软删字段 默认'deleted'
     *
     * @parameter expression="${deletedField}"
     */
    @Parameter(property = "deletedField", defaultValue = "deleted")
    String deletedField = "deleted";
    /**
     * 只读字段
     * 逗号','或分号';'分割，不会通过ORM更新到数据库
     *
     * @parameter expression="${readonlyFields}"
     */
    @Parameter(property = "readonlyFields", defaultValue = "")
    String readonlyFields = "";
    /**
     * 忽略字段
     * 逗号','或分号';'分割，不会通过ORM绑定到实体
     *
     * @parameter expression="${ignoreFields}"
     */
    @Parameter(property = "ignoreFields", defaultValue = "")
    String ignoreFields = "";

    /**
     * 实体基类
     *
     * @parameter expression="${entityBaseClass}"
     */
    @Parameter(property = "entityBaseClass", defaultValue = "")
    String entityBaseClass = "";
    /**
     * 实体辅助类输出模式，绝对路径或相对路径，abs|ref
     *
     * @parameter expression="${entityMetaInfoClassOutputMode}"
     */
    @Parameter(property = "entityMetaInfoClassOutputMode", defaultValue = "")
    String entityMetaInfoClassOutputMode = "abs";
    /**
     * 实体辅助类输出包
     *
     * @parameter expression="${entityMetaInfoClassOutputPackage}"
     */
    @Parameter(property = "entityMetaInfoClassOutputPackage", defaultValue = "domain._share.meta")
    String entityMetaInfoClassOutputPackage = "domain._share.meta";

    /**
     * 关联实体加载模式 LAZY | EAGER
     *
     * @parameter expression="${fetchType}"
     */
    @Parameter(property = "fetchType", defaultValue = "EAGER")
    String fetchType = "EAGER";
    /**
     * 关联实体加载模式 SUBSELECT | JOIN | SELECT
     *
     * @parameter expression="${fetchMode}"
     */
    @Parameter(property = "fetchMode", defaultValue = "SUBSELECT")
    String fetchMode = "SUBSELECT";
    /**
     * 主键生成器 默认自增策略
     *
     * @parameter expression="${idGenerator}"
     */
    @Parameter(property = "idGenerator", defaultValue = "")
    String idGenerator = "";
    /**
     * 枚举类型【值】字段名称
     *
     * @parameter expression="${enumValueField}"
     */
    @Parameter(property = "enumValueField", defaultValue = "value")
    String enumValueField = "value";
    /**
     * 枚举类型【名】字段名称
     *
     * @parameter expression="${enumNameField}"
     */
    @Parameter(property = "enumNameField", defaultValue = "name")
    String enumNameField = "name";
    /**
     * 枚举值转换不匹配时，是否抛出异常
     *
     * @parameter expression="${enumUnmatchedThrowException}"
     */
    @Parameter(property = "enumUnmatchedThrowException", defaultValue = "true")
    Boolean enumUnmatchedThrowException = true;

    /**
     * 日期类型映射使用的包，java.util | java.time，默认java.util
     *
     * @parameter expression="${datePackage}"
     */
    @Parameter(property = "datePackage4Java", defaultValue = "java.util")
    String datePackage4Java = "java.util";
    /**
     * 自定义数据库字段【类型】到【代码类型】映射
     *
     * @parameter expression="${typeRemapping}"
     */
    @Parameter(property = "typeRemapping", defaultValue = "")
    Map<String, String> typeRemapping = new HashMap<>();

    /**
     * 实体字段是否生成默认值，来源数据库默认值
     *
     * @parameter expression="${generateDefault}"
     */
    @Parameter(property = "generateDefault", defaultValue = "false")
    Boolean generateDefault = false;
    /**
     * 实体字段注释是否包含生成数据库字段类型
     *
     * @parameter expression="${generateDbType}"
     */
    @Parameter(property = "generateDbType", defaultValue = "false")
    Boolean generateDbType = false;
    /**
     * 是否生成Schema类，辅助Jpa查询
     *
     * @parameter expression="${generateSchema}"
     */
    @Parameter(property = "generateSchema", defaultValue = "false")
    Boolean generateSchema = false;

    /**
     * 是否生成EntitBuilder类
     *
     * @parameter expression="${generateBuild}"
     */
    @Parameter(property = "generateBuild", defaultValue = "false")
    Boolean generateBuild = false;

    /**
     * 聚合唯一标识类型
     *
     * @parameter expression="${aggregateIdentityClass}"
     */
    @Parameter(property = "aggregateIdentityClass", defaultValue = "Long")
    String aggregateIdentityClass = "Long";

    /**
     * 聚合根注解
     *
     * @parameter expression="${aggregateRootAnnotation}"
     */
    @Parameter(property = "aggregateRootAnnotation", defaultValue = "")
    String aggregateRootAnnotation = "";

    /**
     * 聚合仓储基类型
     *
     * @parameter expression="${aggregateRepositoryBaseClass}"
     */
    @Parameter(property = "aggregateRepositoryBaseClass", defaultValue = "")
    String aggregateRepositoryBaseClass = "";

    /**
     * 聚合仓储自定义代码
     *
     * @parameter expression="${aggregateRepositoryCustomerCode}"
     */
    @Parameter(property = "aggregateRepositoryCustomerCode", defaultValue = "")
    String aggregateRepositoryCustomerCode = "";

    /**
     * 跳过生成仓储的聚合根
     * 逗号','或分号';'分割
     *
     * @parameter expression="${ignoreAggregateRoots}"
     */
    @Parameter(property = "ignoreAggregateRoots", defaultValue = "")
    String ignoreAggregateRoots = "";
}
