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

/**
 * SAGA事务-子环节(存档)
 * <p>
 * 本文件由[cap4j-ddd-codegen-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 *
 * @author cap4j-ddd-codegen
 * @date 2024/10/14
 */
@Aggregate(aggregate = "archived_saga", name = "ArchivedSagaProcess", root = false, type = Aggregate.TYPE_ENTITY, relevant = {"ArchivedSaga"}, description = "SAGA事务-子环节(存档)")
@Entity
@Table(name = "`__archived_saga_process`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class ArchivedSagaProcess {

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
     * varchar(255)
     */
    @Column(name = "`process_code`")
    String processCode;

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
     * int
     */
    @Column(name = "`process_state`")
    @Convert(converter = SagaProcess.SagaProcessState.Converter.class)
    SagaProcess.SagaProcessState processState;

    /**
     * datetime
     */
    @Column(name = "`create_at`", insertable = false, updatable = true)
    LocalDateTime createAt;

    /**
     * int
     */
    @Column(name = "`tried_times`")
    Integer triedTimes;

    /**
     * datetime
     */
    @Column(name = "`last_try_time`", insertable = false, updatable = true)
    LocalDateTime lastTryTime;

    // 【字段映射结束】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动
}


