package org.netcorepal.cap4j.ddd.application.command;

/**
 * 无参数命令
 *
 * @author binking338
 * @date
 */
public abstract class CommandNoneResult<PARAM> implements Command<PARAM, Void> {
    @Override
    public abstract Void exec(PARAM param);
}
