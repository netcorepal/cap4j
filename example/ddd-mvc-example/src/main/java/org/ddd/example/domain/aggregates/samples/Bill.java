package org.ddd.example.domain.aggregates.samples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ddd.domain.event.RocketMqDomainEventSupervisor;
import org.ddd.example.domain.aggregates.samples.events.BillPayedDomainEvent;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 账单
 *
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 */
@Entity
@Table(name = "`bill`")
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "update `bill` set `db_deleted` = 1 where id = ? and `version` = ? ")
@Where(clause = "`db_deleted` = 0")

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
/* @AggregateRoot */
public class Bill {

    // 【行为方法开始】

    public boolean pay(){

        if(this.payed || this.closed){
            return false;
        }
        this.payed = true;
        RocketMqDomainEventSupervisor.Instance.attach(BillPayedDomainEvent.builder().bill(this).build());
        return true;
    }

    public boolean revoke(){
        if(this.payed || this.closed){
            return false;
        }
        this.closed = true;
        return true;
    }

    // 【行为方法结束】



    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;


    /**
     * bigint(20)
     */
    @Column(name = "`order_id`")
    private Long orderId;

    /**
     * 账单名称
     * varchar(100)
     */
    @Column(name = "`name`")
    private String name;

    /**
     * 支付人
     * varchar(100)
     */
    @Column(name = "`owner`")
    private String owner;

    /**
     * 账单金额
     * int(11)
     */
    @Column(name = "`amount`")
    private Integer amount;

    /**
     * 是否支付
     * bit(1)
     */
    @Column(name = "`payed`")
    private Boolean payed;

    /**
     * 是否关闭
     * bit(1)
     */
    @Column(name = "`closed`")
    private Boolean closed;

    /**
     * 数据版本（支持乐观锁）
     */
    @Version
    @Column(name = "`version`")
    private Integer version;

    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动
}

