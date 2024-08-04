package org.ddd.example._share.exception;

import org.ddd.example._share.CodeEnum;
import org.slf4j.event.Level;

/**
 * @author <template/>
 * @date
 */
public class WarnException extends KnownException {

    public WarnException(String msg) {
        super(CodeEnum.FAIL.getCode(), msg, Level.WARN.toString());
    }

    public WarnException(String msg, Throwable e) {
        super(CodeEnum.FAIL.getCode(), msg, Level.WARN.toString(), e);
    }

    public WarnException(CodeEnum codeEnum) {
        super(codeEnum, Level.WARN.toString());
    }

    public WarnException(CodeEnum codeEnum, Throwable throwable) {
        super(codeEnum, Level.WARN.toString(), throwable);
    }

    public WarnException(Integer code, String msg) {
        super(code, msg, Level.WARN.toString());
    }

    public WarnException(Integer code, String msg, Throwable throwable) {
        super(code, msg, Level.WARN.toString(), throwable);
    }
}
