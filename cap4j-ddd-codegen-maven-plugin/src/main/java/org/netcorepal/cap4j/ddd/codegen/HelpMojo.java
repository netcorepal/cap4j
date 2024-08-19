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
        getLog().info("    gen-ddd:gen-entity 生成聚合实体");
        getLog().info("    gen-ddd:gen-repository 生成聚合根仓储");
        getLog().info("    gen-ddd:help 帮助");
        getLog().info("");
        getLog().info("");
        getLog().info("配置");
        getLog().info("--------------------------------------------------");
        getLog().info("\n" +
                "                <configuration>\n" +
                "                    <!-- 数据库链接 --><connectionString>\n" +
                "                    <![CDATA[jdbc:mysql://localhost:3306/information_schema?serverTimezone=Hongkong&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull]]></connectionString>\n" +
                "                    <!-- 数据库账户 -->\n" +
                "                    <user>root</user>\n" +
                "                    <!-- 数据库密码 -->\n" +
                "                    <pwd></pwd>\n" +
                "                    <!-- 数据库DB -->\n" +
                "                    <schema>test</schema>\n" +
                "                    <!-- 数据库表过滤，支持通配符，支持匹配任一过滤条件（多条件逗号\",\"或分号\";\"分割） -->\n" +
                "                    <table>tb1_%,tb2%</table>\n" +
                "                    <!-- 数据库表忽略，支持通配符，支持匹配任一过滤条件（多条件逗号\",\"或分号\";\"分割） -->\n" +
                "                    <ignoreTable>tb1_%,tb2%</ignoreTable>\n" +
                "                    <!-- 主键字段名 默认 id -->\n" +
                "                    <idField>id</idField>\n" +
                "                    <!-- 主键生成器 org.hibernate.id.IdentifierGenerator 默认 数据库自增 -->\n" +
                "                    <idGenerator></idGenerator>\n" +
                "                    <!-- 设置软删字段 -->\n" +
                "                    <deletedField>deleted</deletedField>\n" +
                "                    <!-- 设置乐观锁字段 -->\n" +
                "                    <versionField>version</versionField>\n" +
                "                    <!-- 定义只读字段，逗号\",\"或分号\";\"分割，不会通过ORM更新到数据库 -->\n" +
                "                    <readonlyFields>create_at,update_at</readonlyFields>\n" +
                "                    <!-- 标记忽略字段，逗号\",\"或分号\";\"分割，不会通过ORM绑定到实体 -->\n" +
                "                    <ignoreFields></ignoreFields>\n" +
                "                    <!-- 设置枚举【值】字段 -->\n" +
                "                    <enumValueField>code</enumValueField>\n" +
                "                    <!-- 设置枚举【名】字段 -->\n" +
                "                    <enumNameField>name</enumNameField>\n" +
                "                    <!-- 数据库字段类型 到 代码类型 映射 -->\n" +
                "                    <!-- varchar、text、mediumtext、longtext、char、timestamp、datetime、date、time、int、bigint、smallint、bit、tinyint、float、double、decimal -->\n" +
                "                    <typeRemapping>\n" +
                "                        <varchar>String</varchar>\n" +
                "                    </typeRemapping>\n" +
                "                    <!-- 日期类型映射使用的包 java.util | java.time -->\n" +
                "                    <datePackage4Java>java.time</datePackage4Java>\n" +
                "                    <!-- 是否生成默认值 -->\n" +
                "                    <generateDefault>false</generateDefault>\n" +
                "                    <!-- 是否生成Schema -->\n" +
                "                    <generateSchema>false</generateSchema>\n" +
                "                    <!-- 是否生成数据库类型注释 -->\n" +
                "                    <generateDbType>true</generateDbType>\n" +
                "                    <!-- 关联实体加载模式 LAZY | EAGER -->\n" +
                "                    <fetchType>LAZY</fetchType>\n" +
                "                    <!-- 关联实体查询模式 SUBSELECT | JOIN | SELECT -->\n" +
                "                    <fetchMode>SUBSELECT</fetchMode>\n" +
                "                    <!-- 是否生成EntityBuilder -->\n" +
                "                    <generateBuild>false</generateBuild>\n" +
                "                    <!-- 是否多模块项目 -->\n" +
                "                    <multiModule>true</multiModule>\n" +
                "                    <!-- 实体辅助类输出包 -->\n" +
                        "            <entityMetaInfoClassOutputPackage></entityMetaInfoClassOutputPackage>\n" +
                "                    <!-- 实体辅助类输出模式，绝对路径或相对路径，abs|ref -->\n" +
                "                    <entityMetaInfoClassOutputMode>abs</entityMetaInfoClassOutputMode>\n" +
                "                    <!-- 项目包根路径 -->\n" +
                "                    <basePackage>org.netcorepal.cap4j.ddd.example</basePackage>\n" +
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
