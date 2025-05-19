# cap4j

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.netcorepal/cap4j)](https://central.sonatype.com/artifact/io.github.netcorepal/cap4j)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/netcorepal/cap4j/blob/main/LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/netcorepal/cap4j)

本项目是 [CAP](https://github.com/dotnetcore/CAP) 项目的 Java 实现超集，基于
[整洁架构](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)、
Mediator中介者模式、
[Outbox发件箱](https://www.kamilgrzybek.com/blog/posts/the-outbox-pattern)模式、
[CQS命令查询分离](https://martinfowler.com/bliki/CommandQuerySeparation.html)模式
以及[UoW](https://learn.microsoft.com/en-us/archive/msdn-magazine/2009/june/the-unit-of-work-pattern-and-persistence-ignorance)模式
等理念，cap4j期望解决如何基于`领域模型` 方便地 `实现领域驱动设计`的问题。

如果对以上架构理念有充分了解，那么cap4j的使用将会非常顺手。另一方面，通过cap4j来构建你的服务，你将学会一种实现领域驱动设计的完整落地方法。

## 快速开始

### 脚手架搭建
为了方便框架应用与理解，cap4j配备了代码生成插件`cap4j-ddd-codegen`，基于该插件，我们可以非常方便地生成初始项目脚手架、实体映射代码和基于JPA的聚合仓储代码。

#### **第一步**：新建一个空的maven项目
> 定好maven坐标三要素：`groupId`、`artifactId`、`version`

#### **第二步**：修改pom.xml
> 在pom.xml中添加`cap4j-ddd-codegen-maven-plugin`插件。
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.netcorepal</groupId>
    <artifactId>cap4j-ddd-mvc-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>cap4j-ddd-mvc-example</name>
    <dependencies>
        <dependency>
            <groupId>io.github.netcorepal</groupId>
            <artifactId>cap4j-ddd-codegen-maven-plugin</artifactId>
            <version>1.0.0-alpha-5</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>io.github.netcorepal</groupId>
                <artifactId>cap4j-ddd-codegen-maven-plugin</artifactId>
                <version>1.0.0-alpha-5</version>
                <configuration>
                    <basePackage>org.netcorepal.cap4j.ddd.example</basePackage>
                    <archTemplate>https://raw.githubusercontent.com/netcorepal/cap4j/main/cap4j-ddd-codegen-template-nested.json</archTemplate>
                    <multiModule>false</multiModule>
                    <connectionString>
                        <![CDATA[jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia%2FShanghai&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull]]>
                    </connectionString>
                    <user>root</user>
                    <pwd>123456</pwd>
                    <schema>test</schema>
                    <table></table>
                    <ignoreTable>\_\_%</ignoreTable>
                    <ignoreFields>db_%</ignoreFields>
                    <versionField>version</versionField>
                    <deletedField>db_deleted</deletedField>
                    <readonlyFields>db_created_at,db_updated_at</readonlyFields>
                    <fetchType>EAGER</fetchType>
                    <idGenerator>org.netcorepal.cap4j.ddd.domain.distributed.SnowflakeIdentifierGenerator</idGenerator>
                    <enumNameField>name</enumNameField>
                    <enumValueField>code</enumValueField>
                    <generateDefault>false</generateDefault>
                    <generateDbType>true</generateDbType>
                    <generateSchema>true</generateSchema>
                    <generateParent>false</generateParent>
                    <generateAggregate>true</generateAggregate>
                    <repositorySupportQuerydsl>true</repositorySupportQuerydsl>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
通常，`cap4j-ddd-codegen`插件只需要我们根据团队或项目的实际情况调整以下配置项即可使用。
> `<basePackage>`_项目基础包名，一般为com.yourcompany.project_`</basePackage>` 
> 
> `<connectionString>`_数据库链接_`</connectionString>`
> 
> `<user>`_数据库账号_`</user>`
> 
> `<pwd>`_数据库密码_`</pwd>`
> 
> `<schema>`_数据库名称_`</schema>` 


#### **第三步**：执行插件命令，生成项目脚手架
> 插件配置项`archTemplate`是`gen-arch`命令生成脚手架目录与项目基础代码的配置文件地址。开放自定义方便大家根据自己团队需求进行定制化。格式说明后续补充，不过格式很简单，按示例中的配置自己应该就能看懂并应用。有兴趣更详细了解的参考源码[GenArchMojo](cap4j-ddd-codegen-maven-plugin/src/main/java/org/netcorepal/cap4j/ddd/codegen/GenArchMojo.java)

```shell
mvn cap4j-ddd-codegen:gen-arch
```
如果没有意外，`cap4j-ddd-codegen`插件将根据配置文件[cap4j-ddd-codegen-template.json](https://raw.githubusercontent.com/netcorepal/cap4j/main/cap4j-ddd-codegen-template.json)完成项目结构初始化！

#### 项目结构介绍
基于基础包路径配置，
```xml
<basePackage>org.netcorepal.cap4j.ddd.example</basePackage> 
```
`cap4j-ddd-codegen`插件在maven项目源码目录`src/main/java/org/netcorepal/cap4j/ddd/example`下将会生成4个`package`。
> - `_share`       公共代码
> - `adapter`      适配层(Interface Adapter)
> - `application`  应用层(Application Business Rules)
> - `domain`       领域层(Enterpprise Business Rules)

以上代码分层完全遵循[整洁架构](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)对于代码分层组织的观点。
![整洁架构](https://blog.cleancoder.com/uncle-bob/images/2012-08-13-the-clean-architecture/CleanArchitecture.jpg)

更详细分层结构介绍，移步[项目分层结构介绍](doc/00_项目分层结构介绍.md)。

### 编码最佳实践
1. [领域层编码指南](doc/01_领域层编码指南.md)
2. [应用层编码指南](doc/02_应用层编码指南.md)
3. [适配层编码指南](doc/03_适配层编码指南.md)

### have a nice trip!
