package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author binking338
 * @date 2022-02-18
 */
@Mojo(name = "help")
public class HelpMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("");
        getLog().info("");
        getLog().info("plugins goals:");
        getLog().info("    gen-ddd:gen-arch         生成初始代码、包结构（支持自定义脚手架配置）");
        getLog().info("    gen-ddd:gen-entity       生成聚合实体");
        getLog().info("    gen-ddd:gen-repository   生成聚合根仓储");
        getLog().info("    gen-ddd:help             帮助");
        getLog().info("");
        getLog().info("");
        getLog().info("pom.xml插件配置");
        getLog().info("--------------------------------------------------");
        getLog().info("\n" +
                "                <configuration>\n" +
                "                    <!-- 代码模板配置文件地址 -->\n" +
                "                    <archTemplate>https://raw.githubusercontent.com/netcorepal/cap4j/main/cap4j-ddd-codegen-template.json</archTemplate>\n" +
                "                    <!-- 基础包路径 -->\n" +
                "                    <basePackage>org.netcorepal.cap4j.ddd.example</basePackage>\n" +
                "                    <!-- 是否多模块项目 -->\n" +
                "                    <multiModule>false</multiModule>\n" +
                "                    <!-- adapter模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Adapter>-adapter</moduleNameSuffix4Adapter>\n" +
                "                    <!-- domain模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Domain>-domain</moduleNameSuffix4Domain>\n" +
                "                    <!-- application模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Application>-application</moduleNameSuffix4Application>\n" +
                "                    <!-- 数据库链接地址 -->\n" +
                "                    <connectionString>\n" +
                "                        <![CDATA[jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull]]>\n" +
                "                    </connectionString>\n" +
                "                    <!-- 数据库连接用户 -->\n" +
                "                    <user>root</user>\n" +
                "                    <!-- 数据库连接密码 -->\n" +
                "                    <pwd>123456</pwd>\n" +
                "                    <!-- 数据库过滤 -->\n" +
                "                    <schema>test</schema>\n" +
                "                    <!-- 数据表过滤 -->\n" +
                "                    <!-- 逗号','或分号';'分割，支持通配符'%' -->\n" +
                "                    <table></table>\n" +
                "                    <!-- 数据表忽略 -->\n" +
                "                    <!-- 被忽略的表不生成实体 -->\n" +
                "                    <ignoreTable></ignoreTable>\n" +
                "                    <!-- 主键字段名 默认'id' -->\n" +
                "                    <idField>id</idField>\n" +
                "                    <!-- 乐观锁字段 默认'version' -->\n" +
                "                    <versionField>version</versionField>\n" +
                "                    <!-- 软删字段 默认'deleted' -->\n" +
                "                    <deletedField>db_deleted</deletedField>\n" +
                "                    <!-- 只读字段 -->\n" +
                "                    <!-- 逗号','或分号';'分割，不会通过ORM更新到数据库 -->\n" +
                "                    <readonlyFields>db_created_at,db_updated_at</readonlyFields>\n" +
                "                    <!-- 忽略字段 -->\n" +
                "                    <!-- 逗号','或分号';'分割，不会通过ORM绑定到实体 -->\n" +
                "                    <ignoreFields></ignoreFields>\n" +
                "                    <!-- 实体基类 -->\n" +
                "                    <entityBaseClass></entityBaseClass>\n" +
                "                    <!-- 实体辅助类输出模式，绝对路径或相对路径，abs | ref -->\n" +
                "                    <entityMetaInfoClassOutputMode>abs</entityMetaInfoClassOutputMode>\n" +
                "                    <!-- 实体辅助类输出包 -->\n" +
                "                    <entityMetaInfoClassOutputPackage>domain._share.meta</entityMetaInfoClassOutputPackage>\n" +
                "                    <!-- 关联实体加载模式 LAZY | EAGER -->\n" +
                "                    <fetchMode>SUBSELECT</fetchMode>\n" +
                "                    <!-- 关联实体加载模式 SUBSELECT | JOIN | SELECT -->\n" +
                "                    <fetchType>EAGER</fetchType>\n" +
                "                    <!-- 主键生成器 默认自增策略 -->\n" +
                "                    <idGenerator></idGenerator>\n" +
                "                    <!-- 枚举类型【值】字段名称 默认'value' -->\n" +
                "                    <enumValueField>code</enumValueField>\n" +
                "                    <!-- 枚举类型【名】字段名称 默认'name' -->\n" +
                "                    <enumNameField>name</enumNameField>\n" +
                "                    <!-- 枚举值转换不匹配时，是否抛出异常 -->\n" +
                "                    <enumUnmatchedThrowException>true</enumUnmatchedThrowException>\n" +
                "                    <!-- 日期类型映射使用的包，java.util | java.time，默认java.util -->\n" +
                "                    <datePackage4Java>java.time</datePackage4Java>\n" +
                "                    <!-- 自定义数据库字段【类型】到【代码类型】映射 -->\n" +
                "                    <typeRemapping></typeRemapping>\n" +
                "                    <!-- 实体字段是否生成默认值，来源数据库默认值 -->\n" +
                "                    <generateDefault>false</generateDefault>\n" +
                "                    <!-- 实体字段注释是否包含生成数据库字段类型 -->\n" +
                "                    <generateDbType>true</generateDbType>\n" +
                "                    <!-- 是否生成Schema类，辅助Jpa查询 -->\n" +
                "                    <generateSchema>true</generateSchema>\n" +
                "                    <!-- 是否生成EntitBuilder类 -->\n" +
                "                    <generateBuild>false</generateBuild>\n" +
                "                    <!-- 聚合唯一标识类型 -->\n" +
                "                    <aggregateIdentityClass>Long</aggregateIdentityClass>\n" +
                "                    <!-- 聚合根注解 -->\n" +
                "                    <aggregateRootAnnotation></aggregateRootAnnotation>\n" +
                "                    <!-- 聚合仓储基类型 -->\n" +
                "                    <aggregateRepositoryBaseClass></aggregateRepositoryBaseClass>\n" +
                "                    <!-- 聚合仓储自定义代码 -->\n" +
                "                    <aggregateRepositoryCustomerCode></aggregateRepositoryCustomerCode>\n" +
                "                    <!-- 忽略聚合根 -->\n" +
                "                    <!-- 逗号','或分号';'分割 -->\n" +
                "                    <ignoreAggregateRoots></ignoreAggregateRoots>\n" +
                "                </configuration>");
        getLog().info("");
        getLog().info("");
        getLog().info("gen-ddd:gen-entity 支持如下【表注解】:");
        getLog().info("--------------------------------------------------");
        getLog().info("[@AggregateRoot;]");
        getLog().info("[@Root;]");
        getLog().info("[@R;]");
        getLog().info("功能：标注该表存储的是聚合根实体，如未标记父级表则隐含认为该表存储聚合根实体");
        getLog().info("--------------------------------------------------");
        getLog().info("@Parent={parent_table};");
        getLog().info("@P={parent_table};");
        getLog().info("功能：标注该表存储的是实体，且其父级表为{parent_table}");
        getLog().info("--------------------------------------------------");
        getLog().info("@Count={One|Many};");
        getLog().info("@C={One|Many};");
        getLog().info("功能：标注该实体与聚合根的关系时OneToOne或OneToMany");
        getLog().info("--------------------------------------------------");
        getLog().info("@Lazy{=true|false};");
        getLog().info("@L{=true|false};");
        getLog().info("功能：标注该实体是否应用懒加载策略");
        getLog().info("--------------------------------------------------");
        getLog().info("@Relation[=ManyToMany];");
        getLog().info("@Rel[=ManyToMany];");
        getLog().info("功能：标注该表属于关联关系表（只可能是多对多关系），关联关系表不生成实体");
        getLog().info("--------------------------------------------------");
        getLog().info("@Module={module};");
        getLog().info("@M={module};");
        getLog().info("功能：标注该表所属模块{module}");
        getLog().info("--------------------------------------------------");
        getLog().info("@Aggregate={aggregate};");
        getLog().info("@A={aggregate};");
        getLog().info("功能：标注该表所属聚合{aggregate}");
        getLog().info("--------------------------------------------------");
        getLog().info("@Type={CustomerClassName};");
        getLog().info("@T={CustomerClassName};");
        getLog().info("功能：标注该表的实体类名称{CustomerClassName}");
        getLog().info("--------------------------------------------------");
        getLog().info("@Ignore;");
        getLog().info("@I;");
        getLog().info("功能：标注该表不需要生成实体");
        getLog().info("--------------------------------------------------");
        getLog().info("@IdGenerator[={generator}];");
        getLog().info("@IG[={generator}];");
        getLog().info("功能：标注主键生成策略。org.hibernate.id.IdentifierGenerator");
        getLog().info("--------------------------------------------------");
        getLog().info("");
        getLog().info("");
        getLog().info("gen-ddd:gen-entity 支持如下【列注解】:");
        getLog().info("--------------------------------------------------");
        getLog().info("@Reference[={table}];");
        getLog().info("@Ref[={table}];");
        getLog().info("功能：标注该字段属于关联外键，关联的表为{table}，如果字段命名规范符合({table}_id)，可省略注解值{table}。");
        getLog().info("--------------------------------------------------");
        getLog().info("@Relation={OneToOne|ManyToOne};");
        getLog().info("@Rel={OneToOne|ManyToOne};");
        getLog().info("功能：标注该字段的关联关系类型。如果标记了Reference，则该注解默认值为ManyToOne。");
        getLog().info("--------------------------------------------------");
        getLog().info("@Type={CustomerPropertyName};");
        getLog().info("@T={CustomerPropertyName};");
        getLog().info("功能：标注该字段的实体属性类型{CustomerPropertyName}");
        getLog().info("--------------------------------------------------");
        getLog().info("@Enum[=1:def1:说明1|2:def2:说明2];");
        getLog().info("@E[=1:def1:说明1|2:def2:说明2];");
        getLog().info("功能：标注该字段是枚举字段，请配合@Type一起使用");
        getLog().info("--------------------------------------------------");
        getLog().info("@Lazy{=true|false};");
        getLog().info("@L{=true|false};");
        getLog().info("功能：标注该关联目标应用懒加载策略");
        getLog().info("--------------------------------------------------");
        getLog().info("@Ignore;");
        getLog().info("@I;");
        getLog().info("功能：标注该字段不需要生成映射");
        getLog().info("--------------------------------------------------");
        getLog().info("@IgnoreInsert;");
        getLog().info("@II;");
        getLog().info("功能：标注该字段不需要插入");
        getLog().info("--------------------------------------------------");
        getLog().info("@IgnoreUpdate;");
        getLog().info("@IU;");
        getLog().info("功能：标注该字段不需要更新");
        getLog().info("--------------------------------------------------");
        getLog().info("@ReadOnly;");
        getLog().info("@RO;");
        getLog().info("功能：标注该字段不需要插入和更新");
        getLog().info("--------------------------------------------------");
        getLog().info("");
        getLog().info("");
    }
}
