package org.netcorepal.cap4j.ddd.application.saga;

import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * SagaHandler 基类
 *
 * @author binking338
 * @date 2024/10/12
 */
public interface SagaHandler<REQUEST extends SagaParam<RESPONSE>, RESPONSE> extends RequestHandler<REQUEST, RESPONSE> {

    /**
     * 执行流程子环节
     *
     * @param subCode
     * @param request
     * @param <SUB_REQUEST>
     * @param <SUB_RESPONSE>
     * @return
     */
    default <SUB_REQUEST extends RequestParam<SUB_RESPONSE>, SUB_RESPONSE> SUB_RESPONSE execProcess(String subCode, SUB_REQUEST request) {
        return SagaProcessSupervisor.getInstance().sendProcess(subCode, request);
    }
}
