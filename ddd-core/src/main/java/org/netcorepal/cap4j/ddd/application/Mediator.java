package org.netcorepal.cap4j.ddd.application;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 中介者
 *
 * @author binking338
 * @date 2024/8/24
 */
public interface Mediator {
    static Mediator getInstance()
    {
        return MediatorConfiguration.instance;
    }

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @param <REQUEST> 请求参数类型
     */
    <REQUEST> Object request(REQUEST request);

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @param resultClass 返回结果类型
     * @param <REQUEST> 请求参数类型
     * @param <RESPONSE> 返回结果类型
     */
    <REQUEST, RESPONSE> RESPONSE request(REQUEST request, Class<RESPONSE> resultClass);

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @param paramClass 请求参数类型
     * @param resultClass 返回结果类型
     * @return 请求结果
     * @param <REQUEST> 请求参数类型
     * @param <RESPONSE> 返回结果类型
     */
    <REQUEST, RESPONSE> RESPONSE request(REQUEST request, Class<REQUEST> paramClass, Class<RESPONSE> resultClass);

    /**
     * 通知事件
     *
     * @param eventPayload 事件消息体
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload);

    /**
     * 延迟通知事件
     * @param eventPayload 事件消息体
     * @param delay 延迟时长
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Duration delay);

    /**
     * 定时通知事件
     * @param eventPayload 事件消息体
     * @param schedule 定时时间
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, LocalDateTime schedule);

    /**
     * 通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity);

    /**
     * 延迟通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param delay 延迟时长
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity, Duration delay);

    /**
     * 定时通知事件
     * @param eventPayload 事件消息体
     * @param entity 事件关联实体
     * @param schedule 定时时间
     * @param <EVENT> 事件消息类型
     */
    <EVENT> void notify(EVENT eventPayload, Object entity, LocalDateTime schedule);

}
