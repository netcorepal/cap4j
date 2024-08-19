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
     * 模板文件地址
     *
     * @parameter expression="${archTemplate}"
     */
    @Parameter(property = "archTemplate", defaultValue = "")
    String archTemplate = "";

    /**
     * 基础包路径
     *
     * @parameter expression="${basePackage}"
     */
    @Parameter(property = "basePackage", defaultValue = "")
    String basePackage = "";

    /**
     * 是否多项目
     *
     * @parameter expression="${multiModule}"
     */
    @Parameter(property = "multiModule", defaultValue = "false")
    Boolean multiModule = false;
    /**
     * adapter模块名称
     *
     * @parameter expression="${moduleNameSuffix4Adapter}"
     */
    @Parameter(property = "moduleNameSuffix4Adapter", defaultValue = "-adapter")
    String moduleNameSuffix4Adapter = "-adapter";
    /**
     * domain模块名称
     *
     * @parameter expression="${moduleNameSuffix4Domain}"
     */
    @Parameter(property = "moduleNameSuffix4Domain", defaultValue = "-domain")
    String moduleNameSuffix4Domain = "-domain";
    /**
     * application模块名称
     *
     * @parameter expression="${moduleNameSuffix4Application}"
     */
    @Parameter(property = "moduleNameSuffix4Application", defaultValue = "-application")
    String moduleNameSuffix4Application = "-application";

    /**
     * @parameter expression="${connectionString}"
     */
    @Parameter(property = "connectionString")
    String connectionString = "";
    /**
     * @parameter expression="${user}"
     */
    @Parameter(property = "user")
    String user = "";
    /**
     * @parameter expression="${pwd}"
     */
    @Parameter(property = "pwd")
    String pwd = "";
    /**
     * @parameter expression="${schema}"
     */
    @Parameter(property = "schema")
    String schema = "";
    /**
     * @parameter expression="${table}"
     */
    @Parameter(property = "table", defaultValue = "%")
    String table = "";
    /**
     * @parameter expression="${ignoreTable}"
     */
    @Parameter(property = "ignoreTable", defaultValue = "")
    String ignoreTable = "";
    /**
     * 主键字段名 默认 id
     *
     * @parameter expression="${idField}"
     */
    @Parameter(property = "idField", defaultValue = "id")
    String idField = "id";
    /**
     * 乐观锁字段
     *
     * @parameter expression="${versionField}"
     */
    @Parameter(property = "versionField", defaultValue = "version")
    String versionField = "version";
    /**
     * 软删字段
     *
     * @parameter expression="${deletedField}"
     */
    @Parameter(property = "deletedField", defaultValue = "deleted")
    String deletedField = "deleted";
    /**
     * 标记只读字段，逗号","或分号";"分割，不会通过ORM更新到数据库
     *
     * @parameter expression="${readonlyFields}"
     */
    @Parameter(property = "readonlyFields", defaultValue = "")
    String readonlyFields = "";
    /**
     * 标记忽略字段，逗号","或分号";"分割，不会通过ORM绑定到实体
     *
     * @parameter expression="${ignoreFields}"
     */
    @Parameter(property = "ignoreFields", defaultValue = "")
    String ignoreFields = "";

    /**
     * 实体基础类
     *
     * @parameter expression="${entityBaseClass}"
     */
    @Parameter(property = "entityBaseClass", defaultValue = "")
    String entityBaseClass = "";
    /**
     * 实体辅助类输出包
     *
     * @parameter expression="${entityMetaInfoClassOutputPackage}"
     */
    @Parameter(property = "entityMetaInfoClassOutputPackage", defaultValue = "domain._share.meta")
    String entityMetaInfoClassOutputPackage = "domain._share.meta";
    /**
     * 实体辅助类输出模式，绝对路径或相对路径，abs|ref
     *
     * @parameter expression="${entityMetaInfoClassOutputMode}"
     */
    @Parameter(property = "entityMetaInfoClassOutputMode", defaultValue = "")
    String entityMetaInfoClassOutputMode = "abs";

    /**
     * 主键生成器 默认自增策略
     *
     * @parameter expression="${idGenerator}"
     */
    @Parameter(property = "idGenerator", defaultValue = "")
    String idGenerator = "";
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
     * 枚举【值】字段配置
     *
     * @parameter expression="${enumValueField}"
     */
    @Parameter(property = "enumValueField", defaultValue = "value")
    String enumValueField = "value";
    /**
     * 枚举【名】字段配置
     *
     * @parameter expression="${enumNameField}"
     */
    @Parameter(property = "enumNameField", defaultValue = "name")
    String enumNameField = "name";
    /**
     * 枚举不匹配时，是否抛出异常
     *
     * @parameter expression="${enumUnmatchedThrowException}"
     */
    @Parameter(property = "enumUnmatchedThrowException", defaultValue = "true")
    Boolean enumUnmatchedThrowException = true;

    /**
     * 日期类型映射使用的包 默认java.util
     * java.util | java.time
     *
     * @parameter expression="${datePackage}"
     */
    @Parameter(property = "datePackage4Java", defaultValue = "java.util")
    String datePackage4Java = "java.util";
    /**
     * 数据库字段类型 到 代码类型 映射
     *
     * @parameter expression="${typeRemapping}"
     */
    @Parameter(property = "typeRemapping", defaultValue = "")
    Map<String, String> typeRemapping = new HashMap<>();

    /**
     * 生成默认值，来源数据库默认值
     *
     * @parameter expression="${generateDefault}"
     */
    @Parameter(property = "generateDefault", defaultValue = "false")
    Boolean generateDefault = false;
    /**
     * 生成数据库字段类型到字段注释中
     *
     * @parameter expression="${generateDbType}"
     */
    @Parameter(property = "generateDbType", defaultValue = "false")
    Boolean generateDbType = false;
    /**
     * 生成Schema类
     *
     * @parameter expression="${generateSchema}"
     */
    @Parameter(property = "generateSchema", defaultValue = "false")
    Boolean generateSchema = false;

    /**
     * 生成EntitBuilder类
     *
     * @parameter expression="${generateBuild}"
     */
    @Parameter(property = "generateBuild", defaultValue = "false")
    Boolean generateBuild = false;

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
     * 聚合唯一标识类型
     *
     * @parameter expression="${aggregateIdentityClass}"
     */
    @Parameter(property = "aggregateIdentityClass", defaultValue = "Long")
    String aggregateIdentityClass = "Long";

    /**
     * 聚合仓储自定义代码
     *
     * @parameter expression="${aggregateRepositoryCustomerCode}"
     */
    @Parameter(property = "aggregateRepositoryCustomerCode", defaultValue = "")
    String aggregateRepositoryCustomerCode = "";

    /**
     * 忽略聚合根，逗号分割
     *
     * @parameter expression="${ignoreAggregateRoots}"
     */
    @Parameter(property = "ignoreAggregateRoots", defaultValue = "")
    String ignoreAggregateRoots = "";
}
