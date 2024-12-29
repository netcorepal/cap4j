package org.netcorepal.cap4j.ddd.application.saga.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.saga.SagaParam;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.annotation.Retry;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.alibaba.fastjson.serializer.SerializerFeature.IgnoreNonFieldGetter;
import static com.alibaba.fastjson.serializer.SerializerFeature.SkipTransientField;

/**
 * SAGA事务
 * <p>
 * 本文件由[cap4j-ddd-codegen-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 *
 * @author cap4j-ddd-codegen
 * @date 2024/10/14
 */
@Aggregate(aggregate = "saga", name = "Saga", root = true, type = Aggregate.TYPE_ENTITY, description = "SAGA事务")
@Entity
@Table(name = "`__saga`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class Saga {

    public static final String F_SAGA_UUID = "sagaUuid";
    public static final String F_SVC_NAME = "svcName";
    public static final String F_SAGA_TYPE = "sagaType";
    public static final String F_PARAM = "param";
    public static final String F_PARAM_TYPE = "paramType";
    public static final String F_RESULT = "result";
    public static final String F_RESULT_TYPE = "resultType";
    public static final String F_EXCEPTION = "exception";
    public static final String F_CREATE_AT = "createAt";
    public static final String F_EXPIRE_AT = "expireAt";
    public static final String F_SAGA_STATE = "sagaState";
    public static final String F_TRY_TIMES = "tryTimes";
    public static final String F_TRIED_TIMES = "triedTimes";
    public static final String F_LAST_TRY_TIME = "lastTryTime";
    public static final String F_NEXT_TRY_TIME = "nextTryTime";

    // 【行为方法开始】

    public void init(SagaParam<?> sagaParam, String svcName, String sagaType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes) {
        this.sagaUuid = UUID.randomUUID().toString();
        this.svcName = svcName;
        this.sagaType = sagaType;
        this.createAt = scheduleAt;
        this.expireAt = scheduleAt.plusSeconds((int) expireAfter.getSeconds());
        this.sagaState = SagaState.INIT;
        this.tryTimes = retryTimes;
        this.triedTimes = 1;
        this.lastTryTime = scheduleAt;
        this.nextTryTime = calculateNextTryTime(scheduleAt);
        this.loadSagaParam(sagaParam);
        this.result = "";
        this.resultType = "";
        this.sagaProcesses = new ArrayList<>();
    }

    @Transient
    @JSONField(serialize=false)
    private SagaParam<?> sagaParam = null;
    @Transient
    @JSONField(serialize=false)
    private Object sagaResult = null;

    void loadSagaParam(SagaParam<?> sagaParam) {
        if (sagaParam == null) {
            throw new DomainException("Saga参数不能为null");
        }
        this.sagaParam = sagaParam;
        this.param = JSON.toJSONString(sagaParam, IgnoreNonFieldGetter, SkipTransientField);
        this.paramType = sagaParam.getClass().getName();
        Retry retry = sagaParam == null
                ? null
                : sagaParam.getClass().getAnnotation(Retry.class);
        if (retry != null) {
            this.tryTimes = retry.retryTimes();
            this.expireAt = this.createAt.plusMinutes(retry.expireAfter());
        }
    }

    void loadSagaResult(Object result) {
        if (result == null) {
            throw new DomainException("Saga返回不能为null");
        }
        this.sagaResult = result;
        this.result = JSON.toJSONString(result, IgnoreNonFieldGetter, SkipTransientField);
        this.resultType = result.getClass().getName();
    }

    public SagaParam<?> getSagaParam() {
        if (this.sagaParam != null) {
            return this.sagaParam;
        }
        if (TextUtils.isNotBlank(paramType)) {
            Class<?> dataClass = null;
            try {
                dataClass = Class.forName(paramType);
            } catch (ClassNotFoundException e) {
                log.error("参数类型解析错误", e);
            }
            this.sagaParam = (SagaParam<?>) JSON.parseObject(param, dataClass, Feature.SupportNonPublicField);
        }
        return this.sagaParam;
    }

    public Object getSagaResult() {
        if (this.sagaResult != null) {
            return this.sagaResult;
        }

        if (TextUtils.isNotBlank(resultType)) {
            Class<?> dataClass = null;
            try {
                dataClass = Class.forName(resultType);
            } catch (ClassNotFoundException e) {
                log.error("返回类型解析错误", e);
            }
            this.sagaResult = JSON.parseObject(result, dataClass, Feature.SupportNonPublicField);
        }
        return this.sagaResult;
    }

    public SagaProcess getSagaProcess(String processCode) {
        if (this.sagaProcesses == null) {
            return null;
        }
        return this.sagaProcesses.stream().filter(p -> Objects.equals(p.processCode, processCode)).findFirst().orElse(null);
    }

    public void beginSagaProcess(LocalDateTime now, String processCode, RequestParam<?> param) {
        SagaProcess sagaProcess = getSagaProcess(processCode);
        if (sagaProcess == null) {
            sagaProcess = SagaProcess.builder()
                    .processCode(processCode)
                    .processState(SagaProcess.SagaProcessState.INIT)
                    .createAt(now)
                    .triedTimes(0)
                    .build();
            if(this.sagaProcesses == null){
                this.sagaProcesses = new ArrayList<>();
            }
            this.sagaProcesses.add(sagaProcess);
        }
        sagaProcess.beginProcess(now, param);
    }

    public void endSagaProcess(LocalDateTime now, String processCode, Object result) {
        SagaProcess sagaProcess = getSagaProcess(processCode);
        if (sagaProcess == null) {
            return;
        }
        sagaProcess.endProcess(now, result);
    }

    public boolean isValid() {
        return SagaState.INIT.equals(this.sagaState)
                || SagaState.EXECUTING.equals(this.sagaState)
                || SagaState.EXCEPTION.equals(this.sagaState);
    }

    public boolean isInvalid() {
        return SagaState.CANCEL.equals(this.sagaState)
                || SagaState.EXPIRED.equals(this.sagaState)
                || SagaState.EXHAUSTED.equals(this.sagaState);
    }

    public boolean isExecuted() {
        return SagaState.EXECUTED.equals(this.sagaState);
    }

    public boolean beginSaga(LocalDateTime now) {
        // 超过重试次数
        if (this.triedTimes >= this.tryTimes) {
            this.sagaState = SagaState.EXHAUSTED;
            return false;
        }
        // 事件过期
        if (now.isAfter(this.expireAt)) {
            this.sagaState = SagaState.EXPIRED;
            return false;
        }
        // 初始状态或者确认中或者异常
        if (!isValid()) {
            return false;
        }
        // 未到下次重试时间
        if (this.nextTryTime != null && this.nextTryTime.isAfter(now)) {
            return false;
        }
        this.sagaState = SagaState.EXECUTING;
        this.lastTryTime = now;
        this.nextTryTime = calculateNextTryTime(now);
        this.triedTimes++;
        return true;
    }

    public void endSaga(LocalDateTime now, Object result) {
        this.sagaState = SagaState.EXECUTED;
        loadSagaResult(result);
    }

    public boolean cancelSaga(LocalDateTime now) {
        if (isExecuted() || isInvalid()) {
            return false;
        }
        this.sagaState = SagaState.CANCEL;
        return true;
    }

    public void occuredException(LocalDateTime now, Throwable ex) {
        if (isExecuted()) {
            return;
        }
        this.sagaState = SagaState.EXCEPTION;
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        this.exception = sw.toString();
    }

    private LocalDateTime calculateNextTryTime(LocalDateTime now) {
        Retry retry = getSagaParam() == null
                ? null
                : getSagaParam().getClass().getAnnotation(Retry.class);
        if (Objects.isNull(retry) || retry.retryIntervals().length == 0) {
            if (this.triedTimes <= 10) {
                return now.plusMinutes(1);
            } else if (this.triedTimes <= 20) {
                return now.plusMinutes(5);
            } else {
                return now.plusMinutes(10);
            }
        }
        int index = this.triedTimes - 1;
        if (index >= retry.retryIntervals().length) {
            index = retry.retryIntervals().length - 1;
        } else if (index < 0) {
            index = 0;
        }
        return now.plusMinutes(retry.retryIntervals()[index]);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, IgnoreNonFieldGetter, SkipTransientField);
    }
    // 【行为方法结束】

    @AllArgsConstructor
    public static enum SagaState {
        /**
         * 初始状态
         */
        INIT(0, "init"),
        /**
         * 待确认结果
         */
        EXECUTING(-1, "executing"),
        /**
         * 业务主动取消
         */
        CANCEL(-2, "cancel"),
        /**
         * 过期
         */
        EXPIRED(-3, "expired"),
        /**
         * 用完重试次数
         */
        EXHAUSTED(-4, "exhausted"),
        /**
         * 发生异常
         */
        EXCEPTION(-9, "exception"),
        /**
         * 已发送
         */
        EXECUTED(1, "executed");
        @Getter
        private final Integer value;
        @Getter
        private final String name;

        public static SagaState valueOf(Integer value) {
            for (SagaState val : SagaState.values()) {
                if (Objects.equals(val.value, value)) {
                    return val;
                }
            }
            return null;
        }

        public static class Converter implements AttributeConverter<SagaState, Integer> {

            @Override
            public Integer convertToDatabaseColumn(SagaState attribute) {
                return attribute.value;
            }

            @Override
            public SagaState convertToEntityAttribute(Integer dbData) {
                return SagaState.valueOf(dbData);
            }
        }
    }

    // 【字段映射开始】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "`saga_id`", nullable = false)
    private java.util.List<SagaProcess> sagaProcesses;

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
    @Column(name = "`expire_at`", insertable = false, updatable = true)
    java.time.LocalDateTime expireAt;

    /**
     * datetime
     */
    @Column(name = "`create_at`", insertable = false, updatable = true)
    java.time.LocalDateTime createAt;

    /**
     * int
     */
    @Column(name = "`saga_state`")
    @Convert(converter = SagaState.Converter.class)
    SagaState sagaState;

    /**
     * datetime
     */
    @Column(name = "`last_try_time`", insertable = false, updatable = true)
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


