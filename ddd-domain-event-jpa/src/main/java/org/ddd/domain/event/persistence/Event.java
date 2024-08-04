package org.ddd.domain.event.persistence;


import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ddd.domain.event.annotation.DomainEvent;
import org.ddd.share.DomainException;
import org.ddd.share.annotation.Retry;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author <template/>
 * @date
 */
@Entity
@Table(name = "`__event`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class Event {

    public static final String F_EVENT_UUID = "eventUuid";
    public static final String F_SVC_NAME = "svcName";
    public static final String F_EVENT_TYPE = "eventType";
    public static final String F_DATA = "data";
    public static final String F_DATA_TYPE = "dataType";
    public static final String F_CREATE_AT = "createAt";
    public static final String F_EXPIRE_AT = "expireAt";
    public static final String F_EVENT_STATE = "eventState";
    public static final String F_TRY_TIMES = "tryTimes";
    public static final String F_TRIED_TIMES = "triedTimes";
    public static final String F_LAST_TRY_TIME = "lastTryTime";
    public static final String F_NEXT_TRY_TIME = "nextTryTime";

    public void init(Object payload, String svcName, LocalDateTime now, Duration expireAfter, int retryTimes) {
        this.eventUuid = UUID.randomUUID().toString();
        this.svcName = svcName;
        this.createAt = now;
        this.expireAt = now.plusSeconds((int) expireAfter.getSeconds());
        this.eventState = EventState.INIT;
        this.tryTimes = retryTimes;
        this.triedTimes = 0;
        this.lastTryTime = LocalDateTime.of(1, 1, 1, 0, 0, 0);
        this.loadPayload(payload);
    }

    @Transient
    private Object payload = null;

    public Object getPayload() {
        if (this.payload != null) {
            return this.payload;
        }
        if (StringUtils.isNotBlank(dataType)) {
            Class dataClass = null;
            try {
                dataClass = Class.forName(dataType);
            } catch (ClassNotFoundException e) {
                log.error("事件类型解析错误", e);
            }
            this.payload = JSON.parseObject(data, dataClass);
        }
        return this.payload;
    }

    public boolean isDelivering(LocalDateTime now) {
        return EventState.DELIVERING.equals(this.eventState)
                && now.isBefore(this.nextTryTime);
    }

    public boolean holdState4Delivery(LocalDateTime now) {
        // 超过重试次数
        if (this.triedTimes >= this.tryTimes) {
            this.eventState = EventState.FAILED;
            return false;
        }
        // 事件过期
        if (now.isAfter(this.expireAt)) {
            this.eventState = EventState.EXPIRED;
            return false;
        }
        // 初始状态或者确认中状态
        if (!EventState.INIT.equals(this.eventState)
                && !EventState.DELIVERING.equals(this.eventState)) {
            return false;
        }
        // 未到下次重试时间
        if (this.nextTryTime != null && this.nextTryTime.isAfter(now)) {
            return false;
        }
        this.eventState = EventState.DELIVERING;
        this.lastTryTime = now;
        this.nextTryTime = calculateNextTryTime(now);
        this.triedTimes++;
        return true;
    }

    public void confirmDelivered(LocalDateTime now) {
        this.eventState = EventState.DELIVERED;
    }

    public void cancel(LocalDateTime now) {
        this.eventState = EventState.CANCEL;
    }

    private void loadPayload(Object payload) {
        if(payload == null){
            throw new DomainException("事件体不能为null");
        }
        this.payload = payload;
        this.data = JSON.toJSONString(payload);
        this.dataType = payload.getClass().getName();
        DomainEvent domainEvent = payload == null
                ? null
                : payload.getClass().getAnnotation(DomainEvent.class);
        if (domainEvent != null) {
            this.eventType = domainEvent.value();
        }
        Retry retry = payload == null
                ? null
                : payload.getClass().getAnnotation(Retry.class);
        if (retry != null) {
            this.tryTimes = retry.retryTimes();
            this.expireAt = this.createAt.plusMinutes(retry.expireAfter());
        }
    }

    private LocalDateTime calculateNextTryTime(LocalDateTime now) {
        Retry retry = getPayload() == null
                ? null
                : getPayload().getClass().getAnnotation(Retry.class);
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
        return JSON.toJSONString(this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    /**
     * 事件uuid
     * varchar(64)
     */
    @Column(name = "`event_uuid`")
    private String eventUuid;

    /**
     * 服务
     * varchar
     */
    @Column(name = "`svc_name`")
    private String svcName;

    /**
     * 事件类型
     * varchar(100)
     */
    @Column(name = "`event_type`")
    private String eventType;

    /**
     * 事件数据
     * varchar(1000)
     */
    @Column(name = "`data`")
    private String data;

    /**
     * 事件数据类型
     * varchar(200)
     */
    @Column(name = "`data_type`")
    private String dataType;

    /**
     * 创建时间
     * datetime
     */
    @Column(name = "`create_at`")
    private LocalDateTime createAt;

    /**
     * 过期时间
     * datetime
     */
    @Column(name = "`expire_at`")
    private LocalDateTime expireAt;

    /**
     * 分发状态
     * int
     */
    @Column(name = "`event_state`")
    @Convert(converter = EventState.Converter.class)
    private EventState eventState;

    /**
     * 尝试次数
     * int
     */
    @Column(name = "`try_times`")
    private Integer tryTimes;

    /**
     * 已尝试次数
     * int
     */
    @Column(name = "`tried_times`")
    private Integer triedTimes;

    /**
     * 上次尝试时间
     * datetime
     */
    @Column(name = "`last_try_time`")
    private LocalDateTime lastTryTime;

    /**
     * 下次尝试时间
     * datetime
     */
    @Column(name = "`next_try_time`")
    private LocalDateTime nextTryTime;

    /**
     * 乐观锁
     * int
     */
    @Version
    @Column(name = "`version`")
    private Integer version;

    /**
     * 创建时间
     * datetime
     */
    @Column(name = "`db_created_at`", insertable = false, updatable = false)
    private LocalDateTime dbCreatedAt;

    /**
     * 更新时间
     * datetime
     */
    @Column(name = "`db_updated_at`", insertable = false, updatable = false)
    private Date dbUpdatedAt;

    @AllArgsConstructor
    public static enum EventState {
        /**
         * 初始状态
         */
        INIT(0, "init"),
        /**
         * 待确认发送结果
         */
        DELIVERING(-1, "delivering"),
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
        FAILED(-4, "failed"),
        /**
         * 已发送
         */
        DELIVERED(1, "delivered");
        @Getter
        private final Integer value;
        @Getter
        private final String name;

        public static EventState valueOf(Integer value) {
            for (EventState val : EventState.values()) {
                if (Objects.equals(val.value, value)) {
                    return val;
                }
            }
            return null;
        }

        public static class Converter implements AttributeConverter<EventState, Integer> {

            @Override
            public Integer convertToDatabaseColumn(EventState attribute) {
                return attribute.value;
            }

            @Override
            public EventState convertToEntityAttribute(Integer dbData) {
                return EventState.valueOf(dbData);
            }
        }
    }
}

