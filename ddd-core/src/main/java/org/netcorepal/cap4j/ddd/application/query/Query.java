package org.netcorepal.cap4j.ddd.application.query;

import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * 查询接口
 *
 * @author binking338
 * @date
 *
 * @param <PARAM> 查询参数
 * @param <RESULT> 查询结果
 */
public interface Query<PARAM extends RequestParam<RESULT>, RESULT> extends RequestHandler<PARAM, RESULT> {
    @Override
    RESULT exec(PARAM param);
}
