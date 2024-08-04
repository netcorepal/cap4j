package org.ddd.application.command;

/**
 * 无参数命令
 * @author <template/>
 * @date
 */
public abstract class CommandNoneParam<RESULT> implements Command<Void, RESULT> {
    @Override
    public RESULT exec(Void aVoid) {
        return exec();
    }

    public abstract RESULT exec();
}
