package org.netcorepal.cap4j.ddd.application;

/**
 * 请求接口
 *
 * @param <REQUEST>  请求参数
 * @param <RESPONSE> 返回结果
 * @author binking338
 * @date 2024/8/24
 */
public interface RequestHandler<REQUEST extends RequestParam<RESPONSE>, RESPONSE> {

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @return 返回结果
     */
    RESPONSE exec(REQUEST request);
}
