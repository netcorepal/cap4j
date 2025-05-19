package org.netcorepal.cap4j.ddd.application.saga;

import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.saga.persistence.Saga;
import org.netcorepal.cap4j.ddd.application.saga.persistence.SagaProcess;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Saga记录
 *
 * @author binking338
 * @date 2024/10/12
 */
@Slf4j
public class SagaRecordImpl implements SagaRecord {
    private Saga saga;

    public SagaRecordImpl() {
    }

    public void resume(Saga saga) {
        this.saga = saga;
    }

    public Saga getSaga() {
        return this.saga;
    }

    public String toString() {
        return this.saga.toString();
    }

    @Override
    public void init(SagaParam<?> sagaParam, String svcName, String sagaType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes) {
        saga = Saga.builder().build();
        saga.init(sagaParam, svcName, sagaType, scheduleAt, expireAfter, retryTimes);
    }

    @Override
    public String getId() {
        return this.saga.getSagaUuid();
    }

    @Override
    public SagaParam<?> getParam() {
        return this.saga.getSagaParam();
    }

    @Override
    public <R> R getResult() {
        return (R) this.saga.getSagaResult();
    }

    @Override
    public void beginSagaProcess(LocalDateTime now, String processCode, RequestParam<?> param) {
        this.saga.beginSagaProcess(now, processCode, param);
    }

    @Override
    public void endSagaProcess(LocalDateTime now, String processCode, Object result) {
        this.saga.endSagaProcess(now, processCode, result);
    }

    @Override
    public void sagaProcessOccuredException(LocalDateTime now, String processCode, Throwable throwable) {
        this.saga.getSagaProcess(processCode).occuredException(now, throwable);
    }

    @Override
    public boolean isSagaProcessExecuted(String processCode) {
        SagaProcess sagaProcess = this.saga.getSagaProcess(processCode);
        if (sagaProcess == null) {
            return false;
        }
        return sagaProcess.getProcessState() == SagaProcess.SagaProcessState.EXECUTED;
    }

    @Override
    public <R> R getSagaProcessResult(String processCode) {
        SagaProcess sagaProcess = this.saga.getSagaProcess(processCode);
        if (sagaProcess == null) {
            return null;
        }
        return (R) sagaProcess.getSagaProcessResult();
    }

    @Override
    public LocalDateTime getScheduleTime() {
        return this.saga.getLastTryTime();
    }

    @Override
    public LocalDateTime getNextTryTime() {
        return this.saga.getNextTryTime();
    }

    @Override
    public boolean isValid() {
        return this.saga.isValid();
    }

    @Override
    public boolean isInvalid() {
        return this.saga.isInvalid();
    }

    @Override
    public boolean isExecuting() {
        return this.saga.isExecuting();
    }

    @Override
    public boolean isExecuted() {
        return this.saga.isExecuted();
    }

    @Override
    public boolean beginSaga(LocalDateTime now) {
        return this.saga.beginSaga(now);
    }

    @Override
    public boolean cancelSaga(LocalDateTime now) {
        return this.saga.cancelSaga(now);
    }

    @Override
    public void endSaga(LocalDateTime now, Object result) {
        this.saga.endSaga(now, result);
    }

    @Override
    public void occuredException(LocalDateTime now, Throwable throwable) {
        this.saga.occuredException(now, throwable);
    }
}
