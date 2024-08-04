package org.ddd.example.domain.aggregates.samples;

import org.ddd.example.domain.aggregates.samples.enums.OrderStatus;
import org.ddd.example.domain.aggregates.samples.events.OrderClosedDomainEvent;
import org.ddd.example.domain.aggregates.samples.events.BillPayedDomainEvent;
import org.ddd.example.domain.aggregates.samples.events.OrderPlacedDomainEvent;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ddd.domain.event.RocketMqDomainEventSupervisor;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 订单
 * <p>
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 */
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
/* @AggregateRoot */
public class Order {

    // 【行为方法开始】

    public boolean place(){
        if (finished!=null) {
            return false;
        }
        if (closed!=null) {
            return false;
        }
        this.finished = false;
        this.closed = false;
        this.status = OrderStatus.INIT;
        RocketMqDomainEventSupervisor.Instance.attach(OrderPlacedDomainEvent.builder().order(this).build());
        return true;
    }

    public boolean pay() {
        if (finished) {
            return false;
        }
        if (closed) {
            return false;
        }
        this.finished = true;
        this.status = OrderStatus.FINISH;
        return true;
    }

    public boolean close() {
        if (finished) {
            return false;
        }
        if (closed) {
            return false;
        }
        this.closed = false;
        this.status = OrderStatus.CLOSE;
        RocketMqDomainEventSupervisor.Instance.attach(OrderClosedDomainEvent.builder().order(this).build());
        return true;
    }

    // 【行为方法结束】


    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动

    @Id
    @GeneratedValue(generator = "org.ddd.example.adapter.domain.MyIdGeneratorConfig")
    @GenericGenerator(name = "org.ddd.example.adapter.domain.MyIdGeneratorConfig", strategy = "org.ddd.example.adapter.domain.MyIdGeneratorConfig")
    @Column(name = "`id`")
    private Long id;


    /**
     * 订单金额
     * int(11)
     */
    @Column(name = "`amount`")
    private Integer amount;

    /**
     * 订单标题
     * varchar(100)
     */
    @Column(name = "`name`")
    private String name;

    /**
     * 下单人
     * varchar(100)
     */
    @Column(name = "`owner`")
    private String owner;

    /**
     * 订单状态
     * 0:INIT:待支付;-1:CLOSE:已关闭;1:FINISH:已完成
     * int(11)
     */
    @Convert(converter = org.ddd.example.domain.aggregates.samples.enums.OrderStatus.Converter.class)
    @Column(name = "`status`")
    private org.ddd.example.domain.aggregates.samples.enums.OrderStatus status;

    /**
     * 是否完成
     * bit(1)
     */
    @Column(name = "`finished`")
    private Boolean finished;

    /**
     * 是否关闭
     * bit(1)
     */
    @Column(name = "`closed`")
    private Boolean closed;

    /**
     * datetime
     */
    @Column(name = "`update_at`")
    private java.util.Date updateAt;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true) @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "`order_id`", nullable = false)
    private java.util.List<org.ddd.example.domain.aggregates.samples.OrderItem> orderItems;

    /**
     * 数据版本（支持乐观锁）
     */
    @Version
    @Column(name = "`version`")
    private Integer version;

    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动
}

