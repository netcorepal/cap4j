package org.netcorepal.cap4j.ddd.application.saga.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * SAGA事务(存档)
 * <p>
 * 本文件由[cap4j-ddd-codegen-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 *
 * @author cap4j-ddd-codegen
 * @date 2024/10/14
 */
@Aggregate(aggregate = "archived_saga", name = "ArchivedSaga", root = true, type = Aggregate.TYPE_ENTITY, description = "SAGA事务(存档)")
@Entity
@Table(name = "`__archived_saga`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class ArchivedSaga {

    // 【行为方法开始】

    public void archiveFrom(Saga saga){
        this.id = saga.id;
        this.sagaUuid = saga.sagaUuid;
        this.svcName = saga.svcName;
        this.sagaType = saga.sagaType;
        this.param = saga.param;
        this.paramType = saga.paramType;
        this.result = saga.result;
        this.resultType = saga.resultType;
        this.exception = saga.exception;
        this.expireAt = saga.expireAt;
        this.createAt = saga.createAt;
        this.sagaState = saga.sagaState;
        this.nextTryTime = saga.nextTryTime;
        this.triedTimes = saga.triedTimes;
        this.tryTimes = saga.tryTimes;
        this.version = saga.version;
        this.sagaProcesses = saga.getSagaProcesses().stream().map(p -> ArchivedSagaProcess.builder()
                .processCode(p.processCode)
                .param(p.param)
                .paramType(p.paramType)
                .result(p.result)
                .resultType(p.resultType)
                .exception(p.exception)
                .processState(p.processState)
                .createAt(p.createAt)
                .triedTimes(p.triedTimes)
                .lastTryTime(p.lastTryTime)
                .build()
        ).collect(Collectors.toList());
    }


    // 【行为方法结束】

    // 【字段映射开始】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "`saga_id`", nullable = false)
    private java.util.List<ArchivedSagaProcess> sagaProcesses;

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
    @Column(name = "`saga_uuid`")
    String sagaUuid;

    /**
     * varchar(255)
     */
    @Column(name = "`svc_name`")
    String svcName;

    /**
     * varchar(255)
     */
    @Column(name = "`saga_type`")
    String sagaType;

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
    LocalDateTime expireAt;

    /**
     * datetime
     */
    @Column(name = "`create_at`")
    LocalDateTime createAt;

    /**
     * int
     */
    @Column(name = "`saga_state`")
    @Convert(converter = Saga.SagaState.Converter.class)
    Saga.SagaState sagaState;

    /**
     * datetime
     */
    @Column(name = "`last_try_time`")
    LocalDateTime lastTryTime;

    /**
     * datetime
     */
    @Column(name = "`next_try_time`")
    LocalDateTime nextTryTime;

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


