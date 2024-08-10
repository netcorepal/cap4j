package org.netcorepal.cap4j.ddd.domain.event;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 事件记录
 *
 * @author binking338
 * @date 2023/9/9
 */
public interface EventRecord {
    /**
     * 初始化事件
     * @param payload
     * @param svcName
     * @param scheduleAt
     * @param expireAfter
     * @param retryTimes
     */
    void init(Object payload, String svcName, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes);

    /**
     * 获取事件ID
     * @return
     */
    String getId();

    /**
     * 获取事件主题
     * @return
     */
    String getEventTopic();

    /**
     * 获取事件消息体
     * @return
     */
    Object getPayload();

    /**
     * 获取计划发送事件
     * @return
     */
    LocalDateTime getScheduleTime();

    /**
     * 获取下次重试时间
     * @return
     */
    LocalDateTime getNextTryTime();

    /**
     * 是否发送中
     * @return
     */
    boolean isDelivering();
    /**
     * 是否已发送
     * @return
     */
    boolean isDelivered();

    /**
     * 开始发送事件
     * @param now
     * @return
     */
    boolean beginDelivery(LocalDateTime now);

    /**
     * 取消发送
     * @param now
     * @return
     */
    boolean cancelDelivery(LocalDateTime now);

    /**
     * 发生异常
     * @param now
     * @param throwable
     * @return
     */
    void occuredException(LocalDateTime now, Throwable throwable);

    /**
     * 确认时间已发出
     * @param now
     */
    void confirmedDelivery(LocalDateTime now);
}
