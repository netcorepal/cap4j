package org.ddd.application.command;

/**
 * 无参数命令
 * @author <template/>
 * @date
 */
public abstract class CommandNoneResult<PARAM> implements Command<PARAM, Void> {
    @Override
    public abstract Void exec(PARAM param);
}
