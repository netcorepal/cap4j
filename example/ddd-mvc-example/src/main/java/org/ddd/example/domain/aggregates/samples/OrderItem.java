package org.ddd.example.domain.aggregates.samples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 订单项
 *
 * 本文件由[gen-ddd-maven-plugin]生成
 * 警告：请勿手工修改该文件的字段声明，重新生成会覆盖字段声明
 */
@Entity
@Table(name = "`order_item`")
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "update `order_item` set `db_deleted` = 1 where id = ? ")
@Where(clause = "`db_deleted` = 0")

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class OrderItem {

    // 【行为方法开始】



    // 【行为方法结束】



    // 【字段映射开始】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;


    /**
     * 订单项名称
     * varchar(100)
     */
    @Column(name = "`name`")
    private String name;

    /**
     * 单价
     * int(11)
     */
    @Column(name = "`price`")
    private Integer price;

    /**
     * 数量
     * int(11)
     */
    @Column(name = "`num`")
    private Integer num;

    // 【字段映射结束】本段落由[gen-ddd-maven-plugin]维护，请不要手工改动
}

