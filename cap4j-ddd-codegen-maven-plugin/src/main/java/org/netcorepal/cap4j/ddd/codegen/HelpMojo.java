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
        getLog().info("    cap4j-ddd-codegen:gen-arch         生成初始代码、包结构（支持自定义脚手架配置）");
        getLog().info("    cap4j-ddd-codegen:gen-design       生成设计元素（支持自定义脚手架配置）");
        getLog().info("    cap4j-ddd-codegen:gen-entity       生成聚合实体");
        getLog().info("    cap4j-ddd-codegen:gen-repository   生成聚合根仓储");
        getLog().info("    cap4j-ddd-codegen:help             帮助");
        getLog().info("");
        getLog().info("");
        getLog().info("pom.xml插件配置");
        getLog().info("--------------------------------------------------");
        getLog().info("\n" +
                "                <configuration>\n" +
                "                    <!-- 基础包路径 -->\n" +
                "                    <basePackage>org.netcorepal.cap4j.ddd.example</basePackage>\n" +
                "                    <!-- [gen-arch]代码模板配置文件地址 -->\n" +
                "                    <archTemplate>https://raw.githubusercontent.com/netcorepal/cap4j/main/cap4j-ddd-codegen-template.json</archTemplate>\n" +
                "                    <!-- [gen-arch]代码模板配置文件编码 -->\n" +
                "                    <archTemplateEncoding>UTF-8</archTemplateEncoding>\n" +                "                    <!-- [gen-arch]代码模板配置文件编码 -->\n" +
                "                    <!-- [gen-arch]生成代码文件编码，默认UFT-8 -->\n" +
                "                    <outputEncoding>UTF-8</outputEncoding>\n" +
                "                    <!-- [gen-entity][gen-repository]是否多模块项目 -->\n" +
                "                    <multiModule>false</multiModule>\n" +
                "                    <!-- [gen-entity][gen-repository]adapter模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Adapter>-adapter</moduleNameSuffix4Adapter>\n" +
                "                    <!-- [gen-entity][gen-repository]domain模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Domain>-domain</moduleNameSuffix4Domain>\n" +
                "                    <!-- [gen-entity][gen-repository]application模块名称后缀 -->\n" +
                "                    <moduleNameSuffix4Application>-application</moduleNameSuffix4Application>\n" +
                "                    <!-- [gen-entity]数据库链接地址 -->\n" +
                "                    <connectionString>\n" +
                "                        <![CDATA[jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull]]>\n" +
                "                    </connectionString>\n" +
                "                    <!-- [gen-entity]数据库连接用户 -->\n" +
                "                    <user>root</user>\n" +
                "                    <!-- [gen-entity]数据库连接密码 -->\n" +
                "                    <pwd>123456</pwd>\n" +
                "                    <!-- [gen-entity]数据库过滤 -->\n" +
                "                    <schema>test</schema>\n" +
                "                    <!-- [gen-entity]数据表过滤 -->\n" +
                "                    <!-- 逗号','或分号';'分割，支持通配符'%' -->\n" +
                "                    <table></table>\n" +
                "                    <!-- [gen-entity]数据表忽略，被忽略的表不生成实体 -->\n" +
                "                    <ignoreTable></ignoreTable>\n" +
                "                    <!-- [gen-entity]主键字段名 默认'id' -->\n" +
                "                    <idField>id</idField>\n" +
                "                    <!-- [gen-entity]乐观锁字段 默认'version' -->\n" +
                "                    <versionField>version</versionField>\n" +
                "                    <!-- [gen-entity]软删字段 默认'deleted' -->\n" +
                "                    <deletedField>db_deleted</deletedField>\n" +
                "                    <!-- [gen-entity]只读字段 -->\n" +
                "                    <!-- 逗号','或分号';'分割，不会通过ORM更新到数据库 -->\n" +
                "                    <readonlyFields>db_created_at,db_updated_at</readonlyFields>\n" +
                "                    <!-- 忽略字段 -->\n" +
                "                    <!-- [gen-entity]逗号','或分号';'分割，不会通过ORM绑定到实体 -->\n" +
                "                    <ignoreFields></ignoreFields>\n" +
                "                    <!-- [gen-entity]实体基类 -->\n" +
                "                    <entityBaseClass></entityBaseClass>\n" +
                "                    <!-- [gen-entity]实体辅助类输出模式，绝对路径或相对路径，abs | ref -->\n" +
                "                    <entitySchemaOutputMode>abs</entitySchemaOutputMode>\n" +
                "                    <!-- [gen-entity]实体辅助类输出包 -->\n" +
                "                    <entitySchemaOutputPackage>domain._share.meta</entitySchemaOutputPackage>\n" +
                "                    <!-- [gen-entity]关联实体加载模式 LAZY | EAGER -->\n" +
                "                    <fetchType>EAGER</fetchType>\n" +
                "                    <!-- [gen-entity]主键生成器 默认自增策略 -->\n" +
                "                    <idGenerator></idGenerator>\n" +
                "                    <!-- [gen-entity]值对象主键生成器 默认md5哈希 -->\n" +
                "                    <idGenerator4ValueObject></idGenerator4ValueObject>\n" +
                "                    <!-- [gen-entity]枚举类型【值】字段名称 默认'value' -->\n" +
                "                    <enumValueField>code</enumValueField>\n" +
                "                    <!-- [gen-entity]枚举类型【名】字段名称 默认'name' -->\n" +
                "                    <enumNameField>name</enumNameField>\n" +
                "                    <!-- [gen-entity]枚举值转换不匹配时，是否抛出异常 -->\n" +
                "                    <enumUnmatchedThrowException>true</enumUnmatchedThrowException>\n" +
                "                    <!-- [gen-entity]日期类型映射使用的包，java.util | java.time，默认java.util -->\n" +
                "                    <datePackage4Java>java.time</datePackage4Java>\n" +
                "                    <!-- [gen-entity]自定义数据库字段【类型】到【代码类型】映射 -->\n" +
                "                    <typeRemapping></typeRemapping>\n" +
                "                    <!-- [gen-entity]实体字段是否生成默认值，来源数据库默认值 -->\n" +
                "                    <generateDefault>false</generateDefault>\n" +
                "                    <!-- [gen-entity]实体字段注释是否包含生成数据库字段类型 -->\n" +
                "                    <generateDbType>true</generateDbType>\n" +
                "                    <!-- [gen-entity]是否生成Schema类，辅助Jpa查询 -->\n" +
                "                    <generateSchema>true</generateSchema>\n" +
                "                    <!-- [gen-entity]是否生成关联父实体字段 -->\n" +
                "                    <generateParent>false</generateParent>\n" +
                "                    <!-- [gen-entity]聚合根注解 -->\n" +
                "                    <aggregateRootAnnotation></aggregateRootAnnotation>\n" +
                "                    <!-- [gen-repository]聚合仓储基类型 -->\n" +
                "                    <aggregateRepositoryBaseClass></aggregateRepositoryBaseClass>\n" +
                "                    <!-- [gen-design]应用架构设计，通常通过命令行参数 -Ddesign= 传入-->\n" +
                "                    <design></design>\n" +
                "                    <!-- [gen-design]应用架构设计文件，每行一个LiteralDesign-->\n" +
                "                    <designFile></designFile>\n" +
                "                </configuration>");
        getLog().info("");
        getLog().info("");
        getLog().info("mvn cap4j-ddd-codegen:gen-entity");
        getLog().info("支持如下【表注解】:");
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
        getLog().info("@DomainEvent={domain_event_name1}[:{description}][|{domain_event_name2}[:{description}]];");
        getLog().info("@DE={domain_event_name1}[:{description}][|{domain_event_name2}[:{description}]];");
        getLog().info("@Event={domain_event_name1}[:{description}][|{domain_event_name2}[:{description}]];");
        getLog().info("@Evt={domain_event_name1}[:{description}][|{domain_event_name2}[:{description}]];");
        getLog().info("功能：标注该表对应聚合内的领域事件列表,多个领域事件使用','分割");
        getLog().info("--------------------------------------------------");
        getLog().info("@Factory;");
        getLog().info("@Fac;");
        getLog().info("功能：标注该表对应聚合需生成聚合工厂");
        getLog().info("--------------------------------------------------");
        getLog().info("@Specification;");
        getLog().info("@Spec;");
        getLog().info("@Spe;");
        getLog().info("功能：标注该表对应聚合需生成实体规格约束");
        getLog().info("--------------------------------------------------");
        getLog().info("");
        getLog().info("");
        getLog().info("mvn cap4j-ddd-codegen:gen-entity");
        getLog().info("支持如下【列注解】:");
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
        getLog().info("mvn cap4j-ddd-codegen:gen-design -Ddesign=literalDesign1;[literalDesign2;][...][literalDesignN;]");
        getLog().info("mvn cap4j-ddd-codegen:gen-design -DdesignFile=/path/to/designFile");
        getLog().info("literalDesignN; 支持如下设计元素:");
        getLog().info("(可使用转义符'\\'对 ';' ':' 进行转义)");
        getLog().info("--------------------------------------------------");
        getLog().info("cmd:{命令名称}[:命令说明]");
        getLog().info("功能：定义【应用层】设计元素【命令】，CQS中的C");
        getLog().info("--------------------------------------------------");
        getLog().info("qry:{查询名称}[:查询说明]");
        getLog().info("功能：定义【应用层】设计元素【查询】，CQS中的Q");
        getLog().info("--------------------------------------------------");
        getLog().info("cli:{防腐端名称}[:防腐端说明]");
        getLog().info("功能：定义【应用层】设计元素【防腐端】，作为外部服务接口防腐层，ACL");
        getLog().info("--------------------------------------------------");
        getLog().info("ie:{集成事件名称}[:mq-topic[:集成事件说明]]");
        getLog().info("功能：定义【应用层】设计元素【集成事件】，Integration Event");
        getLog().info("--------------------------------------------------");
        getLog().info("ies:{集成事件名称}[:mq-topic[:mq-consumer[:集成事件说明]]]");
        getLog().info("ieh:{集成事件名称}[:mq-topic[:mq-consumer[:集成事件说明]]]");
        getLog().info("功能：定义【应用层】设计元素【集成事件订阅】，Integration Event Subscriber");
        getLog().info("--------------------------------------------------");
        getLog().info("de:{所在聚合根名称}:{领域事件名称}[:领域事件说明]");
        getLog().info("功能：定义【领域层】设计元素【领域事件】，并自动生成【领域事件订阅】，Domain Event & Domain Event Subscriber");
        getLog().info("--------------------------------------------------");
        getLog().info("fac:{所在聚合根名称}[:聚合工厂说明]");
        getLog().info("功能：定义【领域层】设计元素【领域模型工厂/聚合工厂】，Domain Model Factory / Aggregate Factory");
        getLog().info("--------------------------------------------------");
        getLog().info("spe:{所在聚合根名称}[:实体规格约束说明]");
        getLog().info("功能：定义【领域层】设计元素【实体规格约束】，Specification");
        getLog().info("--------------------------------------------------");
        getLog().info("svc:{领域服务名称}[:领域服务说明]");
        getLog().info("功能：定义【领域层】设计元素【领域服务】，慎用该元素，警惕模型贫血化，Domain Service");
        getLog().info("--------------------------------------------------");
    }
}
