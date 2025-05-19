package org.netcorepal.cap4j.ddd.application.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.netcorepal.cap4j.ddd.share.annotation.Retry;
import org.netcorepal.cap4j.ddd.share.misc.TextUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.alibaba.fastjson.serializer.SerializerFeature.IgnoreNonFieldGetter;
import static com.alibaba.fastjson.serializer.SerializerFeature.SkipTransientField;

/**
 * 请求记录
 * <p>
 * 本文件由[cap4j-ddd-codegen-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 *
 * @author cap4j-ddd-codegen
 * @date 2024/10/14
 */
@Aggregate(aggregate = "request", name = "Request", root = true, type = Aggregate.TYPE_ENTITY, description = "请求记录")
@Entity
@Table(name = "`__request`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class Request {

    public static final String F_REQUEST_UUID = "requestUuid";
    public static final String F_SVC_NAME = "svcName";
    public static final String F_REQUEST_TYPE = "requestType";
    public static final String F_PARAM = "param";
    public static final String F_PARAM_TYPE = "paramType";
    public static final String F_RESULT = "result";
    public static final String F_RESULT_TYPE = "resultType";
    public static final String F_EXCEPTION = "exception";
    public static final String F_CREATE_AT = "createAt";
    public static final String F_EXPIRE_AT = "expireAt";
    public static final String F_REQUEST_STATE = "requestState";
    public static final String F_TRY_TIMES = "tryTimes";
    public static final String F_TRIED_TIMES = "triedTimes";
    public static final String F_LAST_TRY_TIME = "lastTryTime";
    public static final String F_NEXT_TRY_TIME = "nextTryTime";

    // 【行为方法开始】

    public void init(RequestParam<?> requestParam, String svcName, String requestType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes) {
        this.requestUuid = UUID.randomUUID().toString();
        this.svcName = svcName;
        this.requestType = requestType;
        this.createAt = scheduleAt;
        this.expireAt = scheduleAt.plusSeconds((int) expireAfter.getSeconds());
        this.requestState = RequestState.INIT;
        this.tryTimes = retryTimes;
        this.triedTimes = 0;
        this.lastTryTime = scheduleAt;
        this.nextTryTime = calculateNextTryTime(scheduleAt);
        this.loadRequestParam(requestParam);
        this.result = "";
        this.resultType = "";
    }

    @Transient
    @JSONField(serialize = false)
    private RequestParam<?> requestParam = null;
    @Transient
    @JSONField(serialize = false)
    private Object requestResult = null;

    void loadRequestParam(RequestParam<?> requestParam) {
        if (requestParam == null) {
            throw new DomainException("Request参数不能为null");
        }
        this.requestParam = requestParam;
        this.param = JSON.toJSONString(requestParam, IgnoreNonFieldGetter, SkipTransientField);
        this.paramType = requestParam.getClass().getName();
        Retry retry = requestParam == null
                ? null
                : requestParam.getClass().getAnnotation(Retry.class);
        if (retry != null) {
            this.tryTimes = retry.retryTimes();
            this.expireAt = this.createAt.plusMinutes(retry.expireAfter());
        }
    }

    void loadRequestResult(Object result) {
        if (result == null) {
            throw new DomainException("Request返回不能为null");
        }
        this.requestResult = result;
        this.result = JSON.toJSONString(result, IgnoreNonFieldGetter, SkipTransientField);
        this.resultType = result.getClass().getName();
    }

    public RequestParam<?> getRequestParam() {
        if (this.requestParam != null) {
            return this.requestParam;
        }
        if (TextUtils.isNotBlank(paramType)) {
            Class<?> dataClass = null;
            try {
                dataClass = Class.forName(paramType);
            } catch (ClassNotFoundException e) {
                log.error("参数类型解析错误", e);
            }
            this.requestParam = (RequestParam<?>) JSON.parseObject(param, dataClass, Feature.SupportNonPublicField);
        }
        return this.requestParam;
    }

    public Object getRequestResult() {
        if (this.requestResult != null) {
            return this.requestResult;
        }

        if (TextUtils.isNotBlank(resultType)) {
            Class<?> dataClass = null;
            try {
                dataClass = Class.forName(resultType);
            } catch (ClassNotFoundException e) {
                log.error("返回类型解析错误", e);
            }
            this.requestResult = JSON.parseObject(result, dataClass, Feature.SupportNonPublicField);
        }
        return this.requestResult;
    }

    public boolean isValid() {
        return RequestState.INIT.equals(this.requestState)
                || RequestState.EXECUTING.equals(this.requestState)
                || RequestState.EXCEPTION.equals(this.requestState);
    }

    public boolean isInvalid() {
        return RequestState.CANCEL.equals(this.requestState)
                || RequestState.EXPIRED.equals(this.requestState)
                || RequestState.EXHAUSTED.equals(this.requestState);
    }

    public boolean isExecuting() {
        return RequestState.EXECUTING.equals(this.requestState);
    }

    public boolean isExecuted() {
        return RequestState.EXECUTED.equals(this.requestState);
    }

    public boolean beginRequest(LocalDateTime now) {
        // 超过重试次数
        if (this.triedTimes >= this.tryTimes) {
            this.requestState = RequestState.EXHAUSTED;
            return false;
        }
        // 事件过期
        if (now.isAfter(this.expireAt)) {
            this.requestState = RequestState.EXPIRED;
            return false;
        }
        // 初始状态或者确认中或者异常
        if (!isValid()) {
            return false;
        }
        // 未到下次重试时间
        if ((this.lastTryTime == null || !this.lastTryTime.isEqual(now)) && this.nextTryTime != null && this.nextTryTime.isAfter(now)) {
            return false;
        }
        this.requestState = RequestState.EXECUTING;
        this.lastTryTime = now;
        this.triedTimes++;
        this.nextTryTime = calculateNextTryTime(now);
        return true;
    }

    public void endRequest(LocalDateTime now, Object result) {
        this.requestState = RequestState.EXECUTED;
        loadRequestResult(result);
    }

    public boolean cancelRequest(LocalDateTime now) {
        if (isExecuted() || isInvalid()) {
            return false;
        }
        this.requestState = RequestState.CANCEL;
        return true;
    }

    public void occuredException(LocalDateTime now, Throwable ex) {
        if (isExecuted()) {
            return;
        }
        this.requestState = RequestState.EXCEPTION;
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        this.exception = sw.toString();
    }

    private LocalDateTime calculateNextTryTime(LocalDateTime now) {
        Retry retry = getRequestParam() == null
                ? null
                : getRequestParam().getClass().getAnnotation(Retry.class);
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
    public static enum RequestState {
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

        public static RequestState valueOf(Integer value) {
            for (RequestState val : RequestState.values()) {
                if (Objects.equals(val.value, value)) {
                    return val;
                }
            }
            return null;
        }

        public static class Converter implements AttributeConverter<RequestState, Integer> {
            @Override
            public Integer convertToDatabaseColumn(RequestState attribute) {
                return attribute.getValue();
            }

            @Override
            public RequestState convertToEntityAttribute(Integer dbData) {
                return RequestState.valueOf(dbData);
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
    @Convert(converter = RequestState.Converter.class)
    RequestState requestState;

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
