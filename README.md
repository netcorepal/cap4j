# cap4j

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.netcorepal/cap4j)](https://central.sonatype.com/artifact/io.github.netcorepal/cap4j)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/netcorepal/cap4j/blob/main/LICENSE)

本项目是 [CAP](https://github.com/dotnetcore/CAP) 项目的 Java 实现，基于[整洁架构](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)、领域模型、Outbox模式、CQS模式以及UoW等理念，cap4j期望解决如何实现领域驱动设计的问题。

如果对以上架构理念有充分了解，那么cap4j的使用将会非常顺手。另一方面，通过cap4j来构建你的服务，你将学会一种实现领域驱动设计的完整落地方法。

## 快速开始

### 脚手架搭建
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
    <build>
        <plugins>
            <plugin>
                <groupId>io.github.netcorepal</groupId>
                <artifactId>cap4j-ddd-codegen-maven-plugin</artifactId>
                <version>1.0.0-alpha-1</version>
                <configuration>
                    <archTemplate>https://raw.githubusercontent.com/netcorepal/cap4j/main/cap4j-ddd-codegen-template.json</archTemplate>
                    <basePackage>org.netcorepal.cap4j.ddd.example</basePackage>
                    <multiModule>false</multiModule>
                    <moduleNameSuffix4Adapter>-adapter</moduleNameSuffix4Adapter>
                    <moduleNameSuffix4Domain>-domain</moduleNameSuffix4Domain>
                    <moduleNameSuffix4Application>-application</moduleNameSuffix4Application>
                    <connectionString>
                        <![CDATA[jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull]]>
                    </connectionString>
                    <user>root</user>
                    <pwd>123456</pwd>
                    <schema>test</schema>
                    <table></table>
                    <ignoreTable></ignoreTable>
                    <idField>id</idField>
                    <versionField>version</versionField>
                    <deletedField>db_deleted</deletedField>
                    <readonlyFields>db_created_at,db_updated_at</readonlyFields>
                    <ignoreFields></ignoreFields>
                    <entityBaseClass></entityBaseClass>
                    <entityMetaInfoClassOutputMode>ref</entityMetaInfoClassOutputMode>
                    <entityMetaInfoClassOutputPackage>domain._share.meta</entityMetaInfoClassOutputPackage>
                    <fetchMode>SUBSELECT</fetchMode>
                    <fetchType>EAGER</fetchType>
                    <idGenerator>org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator</idGenerator>
                    <enumValueField>code</enumValueField>
                    <enumNameField>name</enumNameField>
                    <enumUnmatchedThrowException>true</enumUnmatchedThrowException>
                    <datePackage4Java>java.time</datePackage4Java>
                    <typeRemapping></typeRemapping>
                    <generateDefault>false</generateDefault>
                    <generateDbType>true</generateDbType>
                    <generateSchema>true</generateSchema>
                    <generateBuild>false</generateBuild>
                    <aggregateIdentityClass>Long</aggregateIdentityClass>
                    <aggregateRootAnnotation></aggregateRootAnnotation>
                    <aggregateRepositoryBaseClass></aggregateRepositoryBaseClass>
                    <aggregateRepositoryCustomerCode></aggregateRepositoryCustomerCode>
                    <ignoreAggregateRoots></ignoreAggregateRoots>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
通常我们只需要根据团队或项目的实际情况调整以下配置项即可。
> - `basePackage`: 项目基础包名，一般为com.yourcompany.project
> - `connectionString`: 数据库连接串
> - `user`: 数据库账号
> - `pwd`: 数据库密码
> - `schema`: 数据库名称


#### **第三步**：执行插件命令，生成项目脚手架
> `archTemplate`是脚手架目录与项目基础代码的配置文件地址。开放自定义方便大家根据自己团队需求进行定制化。格式说明后续再，不过格式很简单，按示例中的配置自己应该就能看懂并应用。有兴趣更详细了解的参考源码[GenArchMojo](cap4j-ddd-codegen-maven-plugin/src/main/java/org/netcorepal/cap4j/ddd/codegen/GenArchMojo.java)

```shell
mvn cap4j-ddd-codegen:gen-arch
```
如果没有意外，项目结构通过插件已初始化完毕！

### 目录结构介绍
#### 简介
```xml
<basePackage>org.netcorepal.cap4j.ddd.example</basePackage> 
```
基于基础包路径配置，在maven项目源码目录`src/main/java/org/netcorepal/cap4j/ddd/example`下有4个`package`。
> - _share       公共代码
> - adapter      适配层(Interface Adapter)
> - application  应用层(Application Business Rules)
> - domain       领域层(Enterpprise Business Rules)

以上代码分层完全遵循[整洁架构](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)对于代码分层组织的观点。
![整洁架构](https://blog.cleancoder.com/uncle-bob/images/2012-08-13-the-clean-architecture/CleanArchitecture.jpg)

#### 领域层
实现领域模型，聚合、实体、领域事件以及集成事件定义。
```text
└── org.netcorepal.cap4j.ddd.example
    └── domain
        ├── _share (领域层公共代码，仅供领域层引用)
        ├── aggregates (实体聚合声明)
        └── services (领域服务)
```

#### 应用层
实现CQS模式，将功能用例(UseCase)抽象成命令或查询。
```text
└── org.netcorepal.cap4j.ddd.example
    └── application
        ├── _share (应用层公共代码，仅供领域层引用)
        │   ├── clients (防腐层：包装三方服务调用接口)
        │   ├── enums (应用层枚举类型)
        │   └── events (声明三方服务集成事件)
        ├── commands (CQS的C：命令)
        ├── queries (CQS的Q：查询)
        └── subscribers (领域事件或集成事件的订阅处理逻辑)
```

#### 适配层
放置领域层（domain）、应用层（application）定义的接口实现。整洁架构中称为接口适配层（Interface Adapters）。
```text
└── org.netcorepal.cap4j.ddd.example
    └── adapter
        ├── _share (适配层公共代码，仅供适配层引用)
        │   └── configure
        │       └── ApolloConfig.java (配置中心)
        ├── application (应用层接口实现)
        │   ├── _share
        │   └── clients
        ├── domain (领域层接口实现)
        │   ├── _share
        │   │   └── configure
        │   │       └── MyDomainEventMessageInterceptor.java (集成事件消息拦截器)
        │   └── repositories (实现聚合仓储接口)
        ├── infra (基础设施适配器）
        │   ├── _share
        │   ├── jdbc (服务于应用层CQS的Q，jdbc查询工具类)
        │   │   └── NamedParameterJdbcTemplateDao.java
        │   └── mybatis (服务于应用层CQS的Q，mybatis集成） 
        │       ├── _share
        │       │   └── MyEnumTypeHandler.java
        │       └── mapper
        └── portal (端口)
            ├── api (SpringMVC相关代码)
            │   ├── TestController.java
            │   └── _share
            │       ├── ResponseData.java
            │       ├── Status.java
            │       └── configure
            │           ├── CommonExceptionHandler.java
            │           ├── MvcConfig.java
            │           └── SwaggerConfig.java
            ├── jobs (定时任务相关代码）
            │   └── _share
            │       └── configure
            │           └── XxlJobConfig.java
            └── queues (消息队列相关代码）
```

#### 公共代码
放置公共代码。
```text
└── org.netcorepal.cap4j.ddd.example
    └── _share
        ├── CodeEnum.java  (响应状态码枚举)
        ├── Constants.java (公共常量)
        └── exception (自定义业务异常)
            ├── ErrorException.java
            ├── KnownException.java
            └── WarnException.java
```

#### 项目目录树
```text
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── netcorepal
    │   │           └── cap4j
    │   │               └── ddd
    │   │                   └── example
    │   │                       ├── StartApplication.java
    │   │                       ├── _share
    │   │                       │   ├── CodeEnum.java
    │   │                       │   ├── Constants.java
    │   │                       │   └── exception
    │   │                       │       ├── ErrorException.java
    │   │                       │       ├── KnownException.java
    │   │                       │       └── WarnException.java
    │   │                       ├── adapter
    │   │                       │   ├── _share
    │   │                       │   │   └── configure
    │   │                       │   │       └── ApolloConfig.java
    │   │                       │   ├── application
    │   │                       │   │   ├── _share
    │   │                       │   │   └── clients
    │   │                       │   ├── domain
    │   │                       │   │   ├── _share
    │   │                       │   │   │   └── configure
    │   │                       │   │   │       └── MyDomainEventMessageInterceptor.java
    │   │                       │   │   └── repositories
    │   │                       │   ├── infra
    │   │                       │   │   ├── _share
    │   │                       │   │   ├── jdbc
    │   │                       │   │   │   └── NamedParameterJdbcTemplateDao.java
    │   │                       │   │   └── mybatis
    │   │                       │   │       ├── _share
    │   │                       │   │       │   └── MyEnumTypeHandler.java
    │   │                       │   │       └── mapper
    │   │                       │   └── portal
    │   │                       │       ├── api
    │   │                       │       │   ├── TestController.java
    │   │                       │       │   └── _share
    │   │                       │       │       ├── ResponseData.java
    │   │                       │       │       ├── Status.java
    │   │                       │       │       └── configure
    │   │                       │       │           ├── CommonExceptionHandler.java
    │   │                       │       │           ├── MvcConfig.java
    │   │                       │       │           └── SwaggerConfig.java
    │   │                       │       ├── jobs
    │   │                       │       │   └── _share
    │   │                       │       │       └── configure
    │   │                       │       │           └── XxlJobConfig.java
    │   │                       │       └── queues
    │   │                       ├── application
    │   │                       │   ├── _share
    │   │                       │   │   ├── clients
    │   │                       │   │   ├── enums
    │   │                       │   │   └── events
    │   │                       │   ├── commands
    │   │                       │   ├── queries
    │   │                       │   └── subscribers
    │   │                       └── domain
    │   │                           ├── _share
    │   │                           ├── aggregates
    │   │                           └── services
    │   └── resources
    │       ├── mapper
    │       ├── application.properties
    │       ├── ddl.sql
    │       └── logback.xml
    └── test
        └── java
            └── org
                └── netcorepal
                    └── cap4j
                        └── ddd
                            └── example
                                └── AppTest.java
```

### 编码最佳实践

#### 领域层
##### ORM代码生成
根据领域模型中的实体以及聚合关系，完成数据库表设计。

为了方便实体到数据库表映射的枯燥工作（ORM），我们设计了一套基于数据库注释的注解语法，并且这套语法非常简单。
通常情况下（比如都是单实体聚合的领域模型）我们不需要这些注解语法也可以让实体代码生成正常工作。

大部分情况下，我们也只需要熟悉一个表注解和两个列注解即可：
- 表注解 @P=_root_entity_table_;
- 列注解 @T=_JavaType_; @E=0:_ENUM_FIELD_:枚举字段注释;

```sql
CREATE TABLE `order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_no` varchar(100) NOT NULL DEFAULT '' COMMENT '订单编号',
  `order_status` int unsigned NOT NULL DEFAULT '0' COMMENT '订单状态@T=OrderStatus;@E=0:INIT:待支付|1:PAID:已支付|-1:CLOSED:已关闭;',
  `amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `version` bigint unsigned NOT NULL DEFAULT '0',
  `db_created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `db_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `db_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_db_created_at` (`db_created_at`),
  KEY `idx_db_updated_at` (`db_updated_at`)
) COMMENT='订单\n';

CREATE TABLE `order_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL DEFAULT '0' COMMENT '关联主订单',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '名称',
  `price` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `count` int NOT NULL DEFAULT '0' COMMENT '数量',
  `version` bigint unsigned NOT NULL DEFAULT '0',
  `db_created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `db_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `db_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_db_created_at` (`db_created_at`),
  KEY `idx_db_updated_at` (`db_updated_at`)
) COMMENT='订单项\n @P=order';
# 以上sql语句隐含了如下聚合关系：
# 订单order是一个聚合根，并且订单项order_item是order聚合的实体成员。
# 订单order_status将会映射成OrderStatus的Java类型，该OrderStatus是一个enum类型，有3个字段成员，INIT、PAID、CLOSED
```
默认情况下，所有数据库表都将会映射成一个Java实体类，该实体类将构成一个聚合，并且作为该聚合的聚合根。



> @P指示该表对应的Java实体类属于某个聚合内的实体成员。
> 
> @E负责生成OrderStatus枚举。
> 
> @T负责将Order实体的orderStatus字段映射成OrderStatus枚举
> 
> 如果想要对这套语法有个详细完整的了解，可以通过如下maven指令获取语法帮助。
> ```shell
> mvn io.github.netcorepal:cap4j-ddd-codegen-maven-plugin:1.0.0-alpha-1:help
> # or
> mvn cap4j-ddd-codegen:help
> ```
> 需要注意的是，当前cap4j仅支持基于MySQL数据库注释的注解解析。

先后执行
```shell
mvn cap4j-ddd-codegen:gen-entity
mvn cap4j-ddd-codegen:gen-repository
```
代码生成结果
```java
package org.netcorepal.cap4j.ddd.example.domain.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 订单
 *
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 */
/* @AggregateRoot */
@Entity
@Table(name = "`order`")
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "update `order` set `db_deleted` = 1 where id = ? and `version` = ? ")
@Where(clause = "`db_deleted` = 0")

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Order {

    // 【行为方法开始】



    // 【行为方法结束】



    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动

    @Id
    @GeneratedValue(generator = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator")
    @GenericGenerator(name = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator", strategy = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator")
    @Column(name = "`id`")
    Long id;


    /**
     * 订单编号
     * varchar(100)
     */
    @Column(name = "`order_no`")
    String orderNo;

    /**
     * 订单状态
     * 0:INIT:待支付;-1:CLOSED:已关闭;1:PAID:已支付
     * int unsigned
     */
    @Convert(converter = org.netcorepal.cap4j.ddd.example.domain.aggregates.enums.OrderStatus.Converter.class)
    @Column(name = "`order_status`")
    org.netcorepal.cap4j.ddd.example.domain.aggregates.enums.OrderStatus orderStatus;

    /**
     * 总金额
     * decimal(14,2)
     */
    @Column(name = "`amount`")
    java.math.BigDecimal amount;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true) @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "`order_id`", nullable = false)
    private java.util.List<org.netcorepal.cap4j.ddd.example.domain.aggregates.OrderItem> orderItems;

    /**
     * 数据版本（支持乐观锁）
     */
    @Version
    @Column(name = "`version`")
    Integer version;

    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动
}

```

```java
package org.netcorepal.cap4j.ddd.example.domain.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 订单项
 *  
 *
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 */
@Entity
@Table(name = "`order_item`")
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "update `order_item` set `db_deleted` = 1 where id = ? and `version` = ? ")
@Where(clause = "`db_deleted` = 0")

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class OrderItem {

    // 【行为方法开始】



    // 【行为方法结束】



    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动

    @Id
    @GeneratedValue(generator = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator")
    @GenericGenerator(name = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator", strategy = "org.netcorepal.cap4j.ddd.application.distributed.SnowflakeIdentifierGenerator")
    @Column(name = "`id`")
    Long id;


    /**
     * 名称
     * varchar(100)
     */
    @Column(name = "`name`")
    String name;

    /**
     * 单价
     * decimal(14,2)
     */
    @Column(name = "`price`")
    java.math.BigDecimal price;

    /**
     * 数量
     * int
     */
    @Column(name = "`count`")
    Integer count;

    /**
     * 数据版本（支持乐观锁）
     */
    @Version
    @Column(name = "`version`")
    Integer version;

    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动
}


```

```java
package org.netcorepal.cap4j.ddd.example.domain.aggregates.enums;

import lombok.Getter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件，重新生成会覆盖该文件
 */
public enum OrderStatus {

    /**
     * 待支付
     */
    INIT(0, "待支付"),
    /**
     * 已关闭
     */
    CLOSED(-1, "已关闭"),
    /**
     * 已支付
     */
    PAID(1, "已支付"),
;
    @Getter
    private int code;
    @Getter
    private String name;

    OrderStatus(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    private static Map<Integer, OrderStatus> enums = null;
    public static OrderStatus valueOf(Integer code) {
        if(enums == null) {
            enums = new HashMap<>();
            for (OrderStatus val : OrderStatus.values()) {
                enums.put(val.code, val);
            }
        }
        if(enums.containsKey(code)){
            return enums.get(code);
        }
        throw new RuntimeException("枚举类型OrderStatus枚举值转换异常，不存在的值" + code);
    }

    /**
     * JPA转换器
     */
    public static class Converter implements AttributeConverter<OrderStatus, Integer>{
        @Override
        public Integer convertToDatabaseColumn(OrderStatus  val) {
            return val.code;
        }

        @Override
        public OrderStatus convertToEntityAttribute(Integer code) {
            return OrderStatus.valueOf(code);
        }
    }
}


```

```java
package org.netcorepal.cap4j.ddd.example.adapter.domain.repositories;

import org.netcorepal.cap4j.ddd.example.domain.aggregates.Order;

/**
 * 本文件由[gen-ddd-maven-plugin]生成
 */
public interface OrderRepository extends org.netcorepal.cap4j.ddd.domain.repo.AggregateRepository<Order, Long> {
    // 【自定义代码开始】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动

    @org.springframework.stereotype.Component
    public static class OrderJpaRepositoryAdapter extends org.netcorepal.cap4j.ddd.domain.repo.AbstractJpaRepository<Order, Long>
    {
        public OrderJpaRepositoryAdapter(org.springframework.data.jpa.repository.JpaSpecificationExecutor<Order> jpaSpecificationExecutor, org.springframework.data.jpa.repository.JpaRepository<Order, Long> jpaRepository) {
            super(jpaSpecificationExecutor, jpaRepository);
        }
    }

    // 【自定义代码结束】本段落之外代码由[gen-ddd-maven-plugin]维护，请不要手工改动
}

```

##### UniOfWork模式
简单来说[UoW](https://learn.microsoft.com/en-us/archive/msdn-magazine/2009/june/the-unit-of-work-pattern-and-persistence-ignorance)实现了将当前线程上下文中所有实体的变更操作一并转化成对应的关系型数据库的持久化DML（insert、update、delete）的能力。缩短事务执行时间的同时，可以让我们将更多的精力放在业务逻辑实现和优化上。

UnitOfWork 常用接口
- `persist(Object entity)` 待持久化添加或更新
- `remove(Object entity)` 待持久化移除
- `save()` 以整体事务提交以上持久化变更

示例
```java
// 代码省略...
public class Order {

    // 【行为方法开始】

    /**
     * 下单初始化
     * @param items
     */
    public void init(List<OrderItem> items){
        this.orderNo = "order-" + System.currentTimeMillis();
        this.orderStatus = OrderStatus.INIT;
        BigDecimal amount = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf( i.getCount())))
                .reduce(BigDecimal.ZERO, (a,b) -> a.add(b));
        this.amount = amount;
        this.orderItems = items;
    }

    // 【行为方法结束】
    // 代码省略...
}
```

```java
package org.netcorepal.cap4j.ddd.example.application.commands;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.domain.repo.AggregateRepository;
import org.netcorepal.cap4j.ddd.domain.repo.UnitOfWork;
import org.netcorepal.cap4j.ddd.example.domain.aggregates.Order;
import org.netcorepal.cap4j.ddd.example.domain.aggregates.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 下单
 *
 * @date 2024/8/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderCmd {

    @Schema(description = "订单项列表")
    List<Item> orderItems;


    @Schema(description = "订单项")
    public static class Item{

        @Schema(description = "名称")
        String name;

        @Schema(description = "价格")
        BigDecimal price;

        @Schema(description = "数量")
        Integer count;
    }

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<PlaceOrderCmd, String> {
        private final AggregateRepository<Order, Long> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public String exec(PlaceOrderCmd cmd) {

            Order order = Order.builder().build();

            List<OrderItem> orderItems = cmd.orderItems.stream()
                    .map(i -> OrderItem.builder()
                            .name(i.name)
                            .price(i.price)
                            .count(i.count)
                            .build())
                    .collect(Collectors.toList());

            order.init(orderItems);

            unitOfWork.persist(order);
            unitOfWork.save();

            return order.getOrderNo();
        }
    }
}
```

##### 事件定义、订阅、发布
**创建发件箱表**

为了实现Outbox模式，cap4j需要在业务库中创建发件箱表。脚手架初始化后，`resources/ddl.sql`包含完整的发件箱表建表语句
```sql
-- Create syntax for TABLE '__event'
CREATE TABLE `__event` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `event_uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '事件uuid',
                           `svc_name` varchar(255) NOT NULL DEFAULT '' COMMENT '服务',
                           `event_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件类型',
                           `data` text COMMENT '事件数据',
                           `data_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件数据类型',
                           `exception` text COMMENT '事件发送异常',
                           `expire_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
                           `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `event_state` int(11) NOT NULL DEFAULT '0' COMMENT '分发状态',
                           `last_try_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次尝试时间',
                           `next_try_time` datetime NOT NULL DEFAULT '0001-01-01 00:00:00' COMMENT '下次尝试时间',
                           `tried_times` int(11) NOT NULL DEFAULT '0' COMMENT '已尝试次数',
                           `try_times` int(11) NOT NULL DEFAULT '0' COMMENT '尝试次数',
                           `version` int(11) NOT NULL DEFAULT '0',
                           `db_created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `db_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`
#   , `db_created_at`
                               ),
                           KEY `idx_db_created_at` (`db_created_at`),
                           KEY `idx_db_updated_at` (`db_updated_at`),
                           KEY `idx_event_uuid` (`event_uuid`),
                           KEY `idx_event_type` (`event_type`,`svc_name`),
                           KEY `idx_create_at` (`create_at`),
                           KEY `idx_expire_at` (`expire_at`),
                           KEY `idx_next_try_time` (`next_try_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件发件箱 support by cap4j\n@I;'
# partition by range(to_days(db_created_at))
# (partition p202201 values less than (to_days('2022-02-01')) ENGINE=InnoDB)
;
-- Create syntax for TABLE '__achrived_event'
CREATE TABLE `__achrived_event` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `event_uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '事件uuid',
                           `svc_name` varchar(255) NOT NULL DEFAULT '' COMMENT '服务',
                           `event_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件类型',
                           `data` text COMMENT '事件数据',
                           `data_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件数据类型',
                           `exception` text COMMENT '事件发送异常',
                           `expire_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
                           `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `event_state` int(11) NOT NULL DEFAULT '0' COMMENT '分发状态',
                           `last_try_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次尝试时间',
                           `next_try_time` datetime NOT NULL DEFAULT '0001-01-01 00:00:00' COMMENT '下次尝试时间',
                           `tried_times` int(11) NOT NULL DEFAULT '0' COMMENT '已尝试次数',
                           `try_times` int(11) NOT NULL DEFAULT '0' COMMENT '尝试次数',
                           `version` int(11) NOT NULL DEFAULT '0',
                           `db_created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `db_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`
#   , `db_created_at`
                               ),
                           KEY `idx_db_created_at` (`db_created_at`),
                           KEY `idx_db_updated_at` (`db_updated_at`),
                           KEY `idx_event_uuid` (`event_uuid`),
                           KEY `idx_event_type` (`event_type`,`svc_name`),
                           KEY `idx_create_at` (`create_at`),
                           KEY `idx_expire_at` (`expire_at`),
                           KEY `idx_next_try_time` (`next_try_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件发件箱存档 support by cap4j\n@I;'
# partition by range(to_days(db_created_at))
# (partition p202201 values less than (to_days('2022-02-01')) ENGINE=InnoDB)
;

CREATE TABLE `__locker` (
                            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                            `name` varchar(100) NOT NULL DEFAULT '' COMMENT '锁名称',
                            `pwd` varchar(100) NOT NULL DEFAULT '' COMMENT '锁密码',
                            `lock_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '锁获取时间',
                            `unlock_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '锁释放时间',
                            `version` bigint(20) unsigned NOT NULL DEFAULT '0',
                            `db_created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `db_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_db_created_at` (`db_created_at`),
                            KEY `idx_db_updated_at` (`db_updated_at`),
                            UNIQUE `uniq_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锁 support by cap4j\n@I;';

```

**领域事件定义**

通常领域事件都是因聚合内部属性状态变更发出，所以基于保障代码业务逻辑的内聚性，更合理的做法是领域事件一般定义在领域层（domain)。

通过[`DomainEvent`](ddd-core/src/main/java/org/netcorepal/cap4j/ddd/domain/event/annotation/DomainEvent.java)注解的类，cap4j将会识别成领域事件。
后续即可通过[`DefaultDomainEventSupervisor`](ddd-core/src/main/java/org/netcorepal/cap4j/ddd/domain/event/impl/DefaultDomainEventSupervisor.java).`instance`.`attach`方法来向当前线程上线文附加领域事件。
一旦 [`UnitOfWork`](ddd-core/src/main/java/org/netcorepal/cap4j/ddd/domain/repo/UnitOfWork.java).save() 顺利提交事务。则cap4j将会保障事件被提交到具体适配好的消息队列（比如当前cap4j实现的RocketMQ）中。

```java
package org.netcorepal.cap4j.ddd.example.domain.aggregates.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 下单领域事件
 *
 * @author bingking338
 */
@DomainEvent(
        persist = true
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedDomainEvent {
    /**
     * 订单号
     */
    String orderNo;
    /**
     * 订单金额
     */
    BigDecimal amount;
    /**
     * 下单时间
     */
    LocalDateTime orderTime;
}

```
> 注解属性详解
> - `value()` value字段非空，则事件会被识别为集成事件，意味着该事件将通过消息队列适配，通知到分布式系统中的其他服务进程。
> - `subscriber()` 集成事件订阅场景，必须定义该字段，通常该字段的值将会被适配的消息队列应用到消费分组配置中。
> - `persist()` 控制事件发布记录持久化。集成事件发布场景，该字段无意义。非集成事件发布场景（仅在本服务进程内部有订阅需求），可以通过`persist=true`控制事件进入发件箱表，并脱离事件发布上下文事务中。以避免订阅逻辑异常影响发布事务的完成。
> 
> 应用场景例子说明
> - `基于MQ发送方` DomainEvent(value="event-name-used-for-mq-topic")
> - `基于MQ订阅方` DomainEvent(subscriber="consumer-group")
> - `消费方与订阅方事务隔离` DomainEvent(persist=true)
> - `消费方与订阅方同一事务` DomainEvent


**领域事件发布**

通常应在实体行为中，发布领域事件。

接口[DomainEventSupervisor.java](ddd-core/src/main/java/org/netcorepal/cap4j/ddd/domain/event/DomainEventSupervisor.java)
> `即时发送` DefaultDomainEventSupervisor.instance.attach(Object eventPayload, Object entity)
> 
> `延时发送` DefaultDomainEventSupervisor.instance.attach(Object eventPayload, Object entity, Duration delay)
> 
> `定时发送` DefaultDomainEventSupervisor.instance.attach(Object eventPayload, Object entity, LocalDateTime schedule)

```java
import org.netcorepal.cap4j.ddd.domain.event.impl.DefaultDomainEventSupervisor;


// 代码省略...
public class Order {
    // 代码省略...
    public class Order {

        // 【行为方法开始】

        /**
         * 下单初始化
         * @param items
         */
        public void init(List<OrderItem> items){
            // 代码省略...
            DefaultDomainEventSupervisor.instance.attach(OrderPlacedDomainEvent.builder()
                    .orderNo(this.orderNo)
                    .amount(this.amount)
                    .orderTime(LocalDateTime.now())
                    .build(), this);
        }

        // 【行为方法结束】
        // 代码省略...
    }
}
```

**领域事件订阅**

领域事件订阅定义在应用层（application），通常放置在 subscribers 包中。

领域事件订阅支持Spring注解式声明订阅(监听)的方式。

```java

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class OrderPlacedDomainEventSubscriber{
    @EventListener(DeliveryReceivedDomainEvent.class)
    public void onEvent(DeliveryReceivedDomainEvent event){
        // 事件处理逻辑
    }
}
```


#### 应用层
##### IDEA代码模板
`${basePackage}.application.commands`中的类模板

模板名称：`Command`
```java

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

#parse("File Header.java")

/**
 * todo: 命令描述
 * 
 * @author binking338
 * @date ${DATE}
 */
@Data
@Builder
public class ${NAME} {
    
    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<${NAME}, ${ReturnType}>{
        private final AggregateRepository<${Entity}, Long> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public ${ReturnType} exec(${NAME} cmd) {
            
            return null;
        }
    }
}
```
`${basePackage}.application.queries`中的类模板

模板名称：`Query`
```java

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

#parse("File Header.java")

/**
 * todo: 查询描述
 *
 * @author binking338
 * @date ${DATE}
 */
@Data
@Builder
public class ${NAME} {
    
    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Query<${NAME}, ${NAME}Dto>{
        private final AggregateRepository<${Entity}, Long> repo;

        @Override
        public ${NAME}Dto exec(${NAME} param) {
            ${Entity} entity = repo.findOne(${Entity}Schema.specify(
                root -> root.id().eq(1)
            )).orElseThrow(() -> new KnownException("不存在"));
            
            return MapperUtil.map(entity, ${NAME}Dto.class);
        }
    }
    
    @Data
    public static class ${NAME}Dto{
        private Long id;
        
    }
}
```

模板名称：`QueryList`
```java

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

#parse("File Header.java")

/**
 * todo: 查询描述
 *
 * @author binking338
 * @date ${DATE}
 */
@Data
@Builder
public class ${NAME} {
    
    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements ListQuery<${NAME}, ${NAME}Dto>{
        private final AggregateRepository<${Entity}, Long> repo;

        @Override
        public List<${NAME}Dto> exec(${NAME} param) {
            List<${Entity}> list = repo.findAll(${Entity}Schema.specify(
                root -> root.id().gt(0)
            ));
            
            return MapperUtil.mapAsList(list, ${NAME}Dto.class);
        }
    }
    
    @Data
    public static class ${NAME}Dto{
        private Long id;
        
    }
}
```

模板名称：`QueryPage`
```java

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

#parse("File Header.java")

/**
 * todo: 查询描述
 *
 * @author binking338
 * @date ${DATE}
 */
@Data
@Builder
public class ${NAME} extends PageParam {
    
    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements PageQuery<${NAME}, ${NAME}Dto>{
        private final AggregateRepository<${Entity}, Long> repo;

        @Override
        public PageData<${NAME}Dto> exec(${NAME} param) {
            Page<${Entity}> page = repo.findAll(${Entity}Schema.specify(
                root -> root.id().gt(0)
            ), param.toSpringData());
            
            return PageData.fromSpringData(page, ${NAME}Dto.class);
        }
    }
    
    @Data
    public static class ${NAME}Dto{
        private Long id;
        
    }
}
```


#### 适配层
##### IDEA LiveTemplate
`acmd` 适配mvc透出命令
```java

@Autowired
$Cmd$.Handler $cmd$Handler;

@Data
@NoArgsConstructor
public static class $Cmd$Request {
    // todo: 添加参数

    @Schema(description = "参数说明")
    String param;

    public DeductWalletCmd toCommand() {
        return $Cmd$.builder()
                .param(param)
                .build();
    }
}
@Schema(description = "接口说明")
@PostMapping("/$cmd$")
public ResponseData<$ReturnType$> $cmd$(@RequestBody @Valid $Cmd$Request request) {
    $ReturnType$ result = $cmd$Handler.exec(request.toCommand());
    return ResponseData.success(result);
}
```
> Edit Template Variables 技巧
>
> cmd参数的Expression可以填入decapitalize(Cmd)

`aqry` 适配mvc透出查询详情
```java

@Autowired
$Qry$.Handler $qry$Handler;

@Schema(description = "接口说明")
@GetMapping("/$qry$")
public ResponseData<$Qry$.$Qry$Dto> $qry$(@Valid $Qry$ param) {
        $Qry$.$Qry$Dto result = $qry$Handler.exec(param);
        return ResponseData.success(result);
        }
```
`aqryl` 适配mvc透出查询列表
```java

@Autowired
$Qry$.Handler $qry$Handler;

@Schema(description = "接口说明")
@GetMapping("/$qry$")
public ResponseData<List<$Qry$.$Qry$Dto>> $qry$(@Valid $Qry$ param) {
        List<$Qry$.$Qry$Dto> result = $qry$Handler.exec(param);
        return ResponseData.success(result);
        }
```
`aqryp` 适配mvc透出查询分页列表
```java

@Autowired
$Qry$.Handler $qry$Handler;

@Schema(description = "接口说明")
@PostMapping("/$qry$")
public ResponseData<PageData<$Qry$.$Qry$Dto>> $qry$(@RequestBody @Valid $Qry$ param) {
        PageData<$Qry$.$Qry$Dto> result = $qry$Handler.exec(param);
        return ResponseData.success(result);
        }esponseData.success(result);
        }
```
### have a nice trip!