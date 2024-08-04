package org.ddd.application.command;

/**
 * 无参无返回命令
 * @author <template/>
 * @date
 */
public abstract class CommandNoneParamAndResult implements Command<Void, Void>{
    @Override
    public Void exec(Void param) {
        exec();
        return null;
    }

    public abstract void exec();
}
