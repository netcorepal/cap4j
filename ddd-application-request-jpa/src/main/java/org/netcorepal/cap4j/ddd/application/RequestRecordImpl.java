package org.netcorepal.cap4j.ddd.application;

import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.persistence.Request;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 请求记录
 *
 * @author binking338
 * @date 2025/5/16
 */
@Slf4j
public class RequestRecordImpl implements RequestRecord {
    private Request request;

    public RequestRecordImpl() {
    }

    public void resume(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return this.request;
    }

    public String toString() {
        return this.request.toString();
    }

    @Override
    public void init(RequestParam<?> requestParam, String svcName, String requestType, LocalDateTime scheduleAt, Duration expireAfter, int retryTimes) {
        request = Request.builder().build();
        request.init(requestParam, svcName, requestType, scheduleAt, expireAfter, retryTimes);
    }

    @Override
    public String getId() {
        return request.getRequestUuid();
    }

    @Override
    public RequestParam<?> getParam() {
        return request.getRequestParam();
    }

    @Override
    public <R> R getResult() {
        return (R) request.getRequestResult();
    }

    @Override
    public LocalDateTime getScheduleTime() {
        return this.request.getLastTryTime();
    }

    @Override
    public LocalDateTime getNextTryTime() {
        return this.request.getNextTryTime();
    }

    @Override
    public boolean isValid() {
        return this.request.isValid();
    }

    @Override
    public boolean isInvalid() {
        return this.request.isInvalid();
    }

    @Override
    public boolean isExecuting() {
        return this.request.isExecuting();
    }

    @Override
    public boolean isExecuted() {
        return this.request.isExecuted();
    }

    @Override
    public boolean beginRequest(LocalDateTime now) {
        return this.request.beginRequest(now);
    }

    @Override
    public boolean cancelRequest(LocalDateTime now) {
        return this.request.cancelRequest(now);
    }

    @Override
    public void endRequest(LocalDateTime now, Object result) {
        this.request.endRequest(now, result);
    }

    @Override
    public void occuredException(LocalDateTime now, Throwable throwable) {
        this.request.occuredException(now, throwable);
    }
}
