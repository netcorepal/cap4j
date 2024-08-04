package org.ddd.example._share.exception;

import org.ddd.example._share.CodeEnum;
import org.slf4j.event.Level;

/**
 * @author <template/>
 * @date
 */
public class ErrorException extends KnownException {

    public ErrorException(String msg) {
        super(CodeEnum.FAIL.getCode(), msg, Level.ERROR.toString());
    }

    public ErrorException(String msg, Throwable e) {
        super(CodeEnum.FAIL.getCode(), msg, Level.ERROR.toString(), e);
    }

    public ErrorException(CodeEnum codeEnum){
        super(codeEnum, Level.ERROR.toString());
    }

    public ErrorException(CodeEnum codeEnum, Throwable throwable){
        super(codeEnum, Level.ERROR.toString(), throwable);
    }

    public ErrorException(Integer code, String msg) {
        super(code, msg, Level.ERROR.toString());
    }

    public ErrorException(Integer code, String msg, Throwable throwable) {
        super(code, msg, Level.ERROR.toString(), throwable);
    }
}
