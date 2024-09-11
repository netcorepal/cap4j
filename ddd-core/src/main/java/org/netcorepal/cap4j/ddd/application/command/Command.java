package org.netcorepal.cap4j.ddd.application.command;

import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * 命令接口
 *
 * @param <PARAM>
 * @param <RESULT>
 * @author binking338
 * @date
 */
public interface Command<PARAM extends RequestParam<RESULT>, RESULT> extends RequestHandler<PARAM, RESULT> {
    @Override
    RESULT exec(PARAM param);
}
