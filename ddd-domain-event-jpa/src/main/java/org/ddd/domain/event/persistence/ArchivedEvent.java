package org.ddd.domain.event.persistence;


import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ddd.domain.event.annotation.DomainEvent;
import org.ddd.share.annotation.Retry;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * @author <template/>
 * @date
 */
@Entity
@Table(name = "`__archived_event`")
@DynamicInsert
@DynamicUpdate

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Slf4j
public class ArchivedEvent {
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
    @Convert(converter = Event.EventState.Converter.class)
    private Event.EventState eventState;

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

}

