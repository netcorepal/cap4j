package org.netcorepal.cap4j.ddd.application.query;

import org.netcorepal.cap4j.ddd.application.RequestHandler;

/**
 * 查询接口
 *
 * @author binking338
 * @date
 *
 * @param <PARAM> 查询参数
 * @param <RESULT> 查询结果
 */
public interface Query<PARAM, RESULT> extends RequestHandler<PARAM, RESULT> {
    @Override
    RESULT exec(PARAM param);
}
