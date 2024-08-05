package org.netcorepal.cap4j.ddd.application.query;

/**
 * 查询接口
 *
 * @author binking338
 * @date
 */
public interface Query<PARAM, RESULT> {
    RESULT exec(PARAM param);
}
