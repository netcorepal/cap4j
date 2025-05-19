package org.netcorepal.cap4j.ddd.application.saga;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Saga控制器
 *
 * @author binking338
 * @date 2024/10/12
 */
public interface SagaSupervisor {

    /**
     * 获取请求管理器
     *
     * @return 请求管理器
     */
    static SagaSupervisor getInstance() {
        return SagaSupervisorSupport.instance;
    }

    /**
     * 执行Saga流程
     *
     * @param request   请求参数
     * @param <REQUEST> 请求参数类型
     */
    <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request);

    /**
     * 异步执行Saga流程
     *
     * @param request
     * @param <REQUEST>
     * @param <RESPONSE> 响应参数类型
     * @return Saga ID
     */
    default <REQUEST extends SagaParam<RESPONSE>, RESPONSE> String async(REQUEST request) {
        return schedule(request, LocalDateTime.now());
    }

    /**
     * 延迟执行请求
     *
     * @param request    请求参数
     * @param schedule   计划时间
     * @param <REQUEST>  请求参数类型
     * @param <RESPONSE> 响应参数类型
     * @return 请求ID
     */
    <REQUEST extends SagaParam<RESPONSE>, RESPONSE> String schedule(REQUEST request, LocalDateTime schedule);

    /**
     * 延迟执行请求
     *
     * @param request    请求参数
     * @param delay      延迟时间
     * @param <REQUEST>  请求参数类型
     * @param <RESPONSE> 响应参数类型
     * @return 请求ID
     */
    default <REQUEST extends SagaParam<RESPONSE>, RESPONSE> String delay(REQUEST request, Duration delay) {
        return schedule(request, LocalDateTime.now().plus(delay));
    }

    /**
     * 获取Saga结果
     *
     * @param id  Saga ID
     * @param <R>
     * @return 请求结果
     */
    <R> R result(String id);

    /**
     * 获取Saga结果
     *
     * @param requestId    请求ID
     * @param requestClass 请求参数类型
     * @param <REQUEST>    请求参数类型
     * @param <RESPONSE>   响应参数类型
     * @return 请求结果
     */
    default <REQUEST extends SagaParam<RESPONSE>, RESPONSE> RESPONSE result(String requestId, Class<REQUEST> requestClass) {
        Object r = result(requestId);
        RESPONSE response = (RESPONSE) r;
        if (r != null && response == null) {
            throw new IllegalArgumentException("request response type mismatch");
        }
        return response;
    }
}
