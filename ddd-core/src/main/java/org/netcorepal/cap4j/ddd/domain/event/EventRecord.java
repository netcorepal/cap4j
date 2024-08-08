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
     * @param now
     * @param expireAfter
     * @param retryTimes
     */
    void init(Object payload, String svcName, LocalDateTime now, Duration expireAfter, int retryTimes);

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
     * 开始发送事件
     * @param now
     * @return
     */
    boolean beginDelivery(LocalDateTime now);

    /**
     * 确认时间已发出
     * @param now
     */
    void confirmDelivered(LocalDateTime now);
}
