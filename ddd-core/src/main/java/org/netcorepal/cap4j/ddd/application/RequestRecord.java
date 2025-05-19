package org.netcorepal.cap4j.ddd.application;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Request记录
 *
 * @author binking338
 * @date 2025/5/15
 */
public interface RequestRecord {
    /**
     * 初始化Request
     *
     * @param requestParam
     * @param svcName
     * @param requestType
     * @param scheduleAt
     * @param expireAfter
     * @param retryTimes
     */
    void init(RequestParam<?> requestParam, String svcName, String requestType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes);

    /**
     * 获取Request ID
     *
     * @return
     */
    String getId();

    /**
     * 获取Request流程执行参数
     *
     * @return
     */
    RequestParam<?> getParam();

    /**
     * 获取Request流程执行结果
     *
     * @return
     */
    <R> R getResult();

    /**
     * 获取计划执行时间
     * @return
     */
    LocalDateTime getScheduleTime();

    /**
     * 获取下次重试时间
     * @return
     */
    LocalDateTime getNextTryTime();

    /**
     * Request流程是否有效（初始或执行中等待确认结果）
     * @return
     */
    boolean isValid();

    /**
     * Request流程是否失效（未执行完成）
     * @return
     */
    boolean isInvalid();
    /**
     * Request流程是否正在执行
     * @return
     */
    boolean isExecuting();
    /**
     * Request流程是否已完成
     * @return
     */
    boolean isExecuted();

    /**
     * Request流程开始执行
     * @param now
     * @return
     */
    boolean beginRequest(LocalDateTime now);

    /**
     * Request流程取消执行
     * @param now
     * @return
     */
    boolean cancelRequest(LocalDateTime now);

    /**
     * Request流程执行完成
     * @param now
     * @param result
     * @return
     */
    void endRequest(LocalDateTime now, Object result);

    /**
     * Request流程发生异常
     * @param now
     * @param throwable
     * @return
     */
    void occuredException(LocalDateTime now, Throwable throwable);
}
