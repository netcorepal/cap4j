package org.netcorepal.cap4j.ddd.application.command;

import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * 无返回命令
 *
 * @author binking338
 * @date
 */
public abstract class NoneResultCommandParam<PARAM extends RequestParam<Void>> implements Command<PARAM, Void> {
    @Override
    public abstract Void exec(PARAM param);
}
