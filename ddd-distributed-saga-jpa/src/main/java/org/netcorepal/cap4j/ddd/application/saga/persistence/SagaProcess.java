package org.netcorepal.cap4j.ddd.application.saga.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.alibaba.fastjson.serializer.SerializerFeature.IgnoreNonFieldGetter;
import static com.alibaba.fastjson.serializer.SerializerFeature.SkipTransientField;

/**
 * SAGA事务-子环节
 * <p>
 * 本文件由[cap4j-ddd-codegen-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 *
 * @author cap4j-ddd-codegen
 * @date 2024/10/14
 */
@Aggregate(aggregate = "saga", name = "SagaProcess", root = false, type = Aggregate.TYPE_ENTITY, relevant = {"Saga"}, description = "SAGA事务-子环节")
@Entity
@Table(name = "`__saga_process`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class SagaProcess {

    // 【行为方法开始】

    @Transient
    private Object sagaProcessResult;

    public void beginProcess(LocalDateTime now, RequestParam<?> param) {
        this.param = JSON.toJSONString(param, IgnoreNonFieldGetter, SkipTransientField);
        this.paramType = param.getClass().getName();
        this.processState = SagaProcessState.EXECUTING;
        this.lastTryTime = now;
    }

    public void endProcess(LocalDateTime now, Object result) {
        this.result = JSON.toJSONString(result, IgnoreNonFieldGetter, SkipTransientField);
        this.resultType = result.getClass().getName();
        this.processState = SagaProcessState.EXECUTED;
        this.lastTryTime = now;
    }

    public void occuredException(LocalDateTime now, Throwable ex){
        this.result = JSON.toJSONString(null);
        this.resultType = Object.class.getName();
        this.processState = SagaProcessState.EXCEPTION;
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        this.exception = sw.toString();
    }

    public Object getSagaProcessResult() {
        if (sagaProcessResult != null) {
            return sagaProcessResult;
        }
        if (TextUtils.isNotBlank(resultType)) {
            Class dataClass = null;
            try {
                dataClass = Class.forName(resultType);
            } catch (ClassNotFoundException e) {
                log.error("返回类型解析错误", e);
            }
            this.sagaProcessResult = JSON.parseObject(result, dataClass, Feature.SupportNonPublicField);
        }
        return this.sagaProcessResult;
    }

    // 【行为方法结束】

    @AllArgsConstructor
    public static enum SagaProcessState {
        /**
         * 初始状态
         */
        INIT(0, "init"),
        /**
         * 待确认结果
         */
        EXECUTING(-1, "executing"),
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

        public static SagaProcessState valueOf(Integer value) {
            for (SagaProcessState val : SagaProcessState.values()) {
                if (Objects.equals(val.value, value)) {
                    return val;
                }
            }
            return null;
        }

        public static class Converter implements AttributeConverter<SagaProcessState, Integer> {

            @Override
            public Integer convertToDatabaseColumn(SagaProcessState attribute) {
                return attribute.value;
            }

            @Override
            public SagaProcessState convertToEntityAttribute(Integer dbData) {
                return SagaProcessState.valueOf(dbData);
            }
        }
    }


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
    @Convert(converter = SagaProcessState.Converter.class)
    SagaProcessState processState;

    /**
     * datetime
     */
    @Column(name = "`create_at`", insertable = false, updatable = true)
    java.time.LocalDateTime createAt;

    /**
     * int
     */
    @Column(name = "`tried_times`")
    Integer triedTimes;

    /**
     * datetime
     */
    @Column(name = "`last_try_time`", insertable = false, updatable = true)
    java.time.LocalDateTime lastTryTime;

    // 【字段映射结束】本段落由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动
}


