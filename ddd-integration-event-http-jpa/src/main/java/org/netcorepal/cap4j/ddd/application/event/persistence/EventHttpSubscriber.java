package org.netcorepal.cap4j.ddd.application.event.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;

/**
 * 集成事件HTTP订阅
 *
 * @author binking338
 * @date 2025/5/23
 */
@Aggregate(aggregate = "event_http_subscriber", name = "EventHttpSubscriber", root = true, type = Aggregate.TYPE_ENTITY, description = "集成事件订阅")
@Entity
@Table(name = "`__event_http_subscriber`")
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "update `__event_http_subscriber` set `db_deleted` = 1 where `id` = ? and `version` = ? ")
@Where(clause = "`db_deleted` = 0")

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class EventHttpSubscriber {
    public static final String F_ID = "id";
    public static final String F_EVENT = "event";
    public static final String F_SUBSCRIBER = "subscriber";
    public static final String F_CALLBACK_URL = "callbackUrl";

    // 【行为方法开始】

    // 【行为方法结束】

    // 【字段映射开始】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动

    /**
     * bigint
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    Long id;

    /**
     * 事件
     * varchar(255)
     */
    @Column(name = "`event`")
    String event;

    /**
     * 订阅者
     * varchar(255)
     */
    @Column(name = "`subscriber`")
    String subscriber;

    /**
     * 回调地址
     * varchar(1023)
     */
    @Column(name = "`callback_url`")
    String callbackUrl;

    /**
     * 数据版本（支持乐观锁）
     * int
     */
    @Version
    @Column(name = "`version`")
    Integer version;

    // 【字段映射结束】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动

}
