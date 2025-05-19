package org.netcorepal.cap4j.ddd.application.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;

import javax.persistence.*;

/**
 * 归档请求实体
 *
 * @author binking338
 * @date 2025/5/16
 */
@Aggregate(aggregate = "archived_request", name = "ArchivedRequest", root = true, type = Aggregate.TYPE_ENTITY, description = "请求记录")
@Entity
@Table(name = "`__archived_request`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class ArchivedRequest {

    // 【行为方法开始】

    public void archiveFrom(Request request) {
        this.id = request.id;
        this.requestUuid = request.requestUuid;
        this.svcName = request.svcName;
        this.requestType = request.requestType;
        this.param = request.param;
        this.paramType = request.paramType;
        this.result = request.result;
        this.resultType = request.resultType;
        this.exception = request.exception;
        this.expireAt = request.expireAt;
        this.createAt = request.createAt;
        this.requestState = request.requestState;
        this.nextTryTime = request.nextTryTime;
        this.triedTimes = request.triedTimes;
        this.tryTimes = request.tryTimes;
        this.version = request.version;
    }

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
     * varchar(64)
     */
    @Column(name = "`request_uuid`")
    String requestUuid;

    /**
     * varchar(255)
     */
    @Column(name = "`svc_name`")
    String svcName;

    /**
     * varchar(255)
     */
    @Column(name = "`request_type`")
    String requestType;

    /**
     * text
     */
    @Column(name = "`param`")
    String param;

    /**
     * varchar(255)
     */
    @Column(name = "`param_type`")
    String paramType;

    /**
     * text
     */
    @Column(name = "`result`")
    String result;

    /**
     * varchar(255)
     */
    @Column(name = "`result_type`")
    String resultType;

    /**
     * text
     */
    @Column(name = "`exception`")
    String exception;

    /**
     * datetime
     */
    @Column(name = "`expire_at`")
    java.time.LocalDateTime expireAt;

    /**
     * datetime
     */
    @Column(name = "`create_at`")
    java.time.LocalDateTime createAt;

    /**
     * int
     */
    @Column(name = "`request_state`")
    @Convert(converter = Request.RequestState.Converter.class)
    Request.RequestState requestState;

    /**
     * datetime
     */
    @Column(name = "`last_try_time`")
    java.time.LocalDateTime lastTryTime;

    /**
     * datetime
     */
    @Column(name = "`next_try_time`")
    java.time.LocalDateTime nextTryTime;

    /**
     * int
     */
    @Column(name = "`tried_times`")
    Integer triedTimes;

    /**
     * int
     */
    @Column(name = "`try_times`")
    Integer tryTimes;

    /**
     * 数据版本（支持乐观锁）
     * int
     */
    @Version
    @Column(name = "`version`")
    Integer version;

    // 【字段映射结束】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动
}
