package org.netcorepal.cap4j.ddd.application.command;

import org.netcorepal.cap4j.ddd.application.RequestHandler;

/**
 * 命令接口
 *
 * @author binking338
 * @date
 *
 * @param <PARAM>
 * @param <RESULT>
 */
public interface Command<PARAM, RESULT> extends RequestHandler<PARAM, RESULT> {
    @Override
    RESULT exec(PARAM param);
}
