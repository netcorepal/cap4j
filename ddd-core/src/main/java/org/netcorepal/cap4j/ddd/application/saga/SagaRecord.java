package org.netcorepal.cap4j.ddd.application.saga;

import org.netcorepal.cap4j.ddd.application.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Saga记录
 *
 * @author binking338
 * @date 2024/10/12
 */
public interface SagaRecord {
    /**
     * 初始化Saga
     * @param sagaParam
     * @param svcName
     * @param sagaType
     * @param scheduleAt
     * @param expireAfter
     * @param retryTimes
     */
    void init(SagaParam<?> sagaParam, String svcName, String sagaType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes);

    /**
     * 获取Saga ID
     * @return
     */
    String getId();

    /**
     * 获取Saga类型
     * @return
     */
    String getType();

    /**
     * 获取Saga流程执行参数
     * @return
     */
    SagaParam<?> getParam();

    /**
     * 获取Saga流程执行结果
     * @return
     */
    <R> R getResult();

    /**
     * Saga流程子环节开始执行
     *
     * @param now
     * @param processCode
     * @param param
     */
    void beginSagaProcess(LocalDateTime now, String processCode, RequestParam<?> param);

    /**
     * Saga流程子环节执行完成
     *
     * @param now
     * @param processCode
     * @param result
     */
    void endSagaProcess(LocalDateTime now, String processCode, Object result);

    /**
     * 获取Saga流程子环节发生异常
     *
     * @param now
     * @param processCode
     * @param throwable
     */
    void sagaProcessOccuredException(LocalDateTime now, String processCode, Throwable throwable);

    /**
     * 获取Saga流程子环节是否已执行
     *
     * @param processCode
     * @return
     */
    boolean isSagaProcessExecuted(String processCode);

    /**
     * 获取Saga流程子环节执行结果
     *
     * @param processCode
     * @return
     */
    <R> R getSagaProcessResult(String processCode);

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
     * Saga流程是否有效（初始或执行中等待确认结果）
     * @return
     */
    boolean isValid();

    /**
     * Saga流程是否失效（未执行完成）
     * @return
     */
    boolean isInvalid();

    /**
     * Saga流程是否正在执行
     * @return
     */
    boolean isExecuting();
    /**
     * Saga流程是否已完成
     * @return
     */
    boolean isExecuted();

    /**
     * Saga流程开始执行
     * @param now
     * @return
     */
    boolean beginSaga(LocalDateTime now);

    /**
     * Saga流程取消执行
     * @param now
     * @return
     */
    boolean cancelSaga(LocalDateTime now);

    /**
     * Saga流程执行完成
     * @param now
     * @param result
     * @return
     */
    void endSaga(LocalDateTime now, Object result);

    /**
     * Saga流程发生异常
     * @param now
     * @param throwable
     * @return
     */
    void occuredException(LocalDateTime now, Throwable throwable);
}
