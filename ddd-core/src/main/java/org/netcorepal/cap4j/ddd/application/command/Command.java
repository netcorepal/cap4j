package org.netcorepal.cap4j.ddd.application.command;

/**
 * 命令接口
 *
 * @author binking338
 * @date
 */
public interface Command<PARAM, RESULT> {
    RESULT exec(PARAM param);
}
