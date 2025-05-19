package org.netcorepal.cap4j.ddd.application;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 请求管理器
 *
 * @author binking338
 * @date 2024/8/24
 */
public interface RequestSupervisor {

    /**
     * 获取请求管理器
     *
     * @return 请求管理器
     */
    static RequestSupervisor getInstance() {
        return RequestSupervisorSupport.instance;
    }

    /**
     * 执行请求
     *
     * @param request    请求参数
     * @param <REQUEST>  请求参数类型
     * @param <RESPONSE> 响应参数类型
     */
    <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE send(REQUEST request);

    /**
     * 异步执行请求
     *
     * @param request    请求参数
     * @param <REQUEST>  请求参数类型
     * @param <RESPONSE> 响应参数类型
     * @return 请求ID
     */
    default <REQUEST extends RequestParam<RESPONSE>, RESPONSE> String async(REQUEST request) {
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
    <REQUEST extends RequestParam<RESPONSE>, RESPONSE> String schedule(REQUEST request, LocalDateTime schedule);

    /**
     * 延迟执行请求
     *
     * @param request    请求参数
     * @param delay      延迟时间
     * @param <REQUEST>  请求参数类型
     * @param <RESPONSE> 响应参数类型
     * @return 请求ID
     */
    default <REQUEST extends RequestParam<RESPONSE>, RESPONSE> String delay(REQUEST request, Duration delay) {
        return schedule(request, LocalDateTime.now().plus(delay));
    }

    /**
     * 获取请求结果
     *
     * @param requestId 请求ID
     * @param <R>
     * @return 请求结果
     */
    <R> R result(String requestId);

    /**
     * 获取请求结果
     *
     * @param requestId    请求ID
     * @param requestClass 请求参数类型
     * @param <REQUEST>    请求参数类型
     * @param <RESPONSE>   响应参数类型
     * @return 请求结果
     */
    default <REQUEST extends RequestParam<RESPONSE>, RESPONSE> RESPONSE result(String requestId, Class<REQUEST> requestClass) {
        Object r = result(requestId);
        RESPONSE response = (RESPONSE) r;
        if (r != null && response == null) {
            throw new IllegalArgumentException("request response type mismatch");
        }
        return response;
    }
}
