package org.ddd.example._share.exception;

import org.ddd.example._share.CodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

/**
 * @author <template/>
 * @date
 */
@Data
@Slf4j
public class KnownException extends RuntimeException {

    private Integer code;

    private String msg;

    private String level;

    public KnownException(CodeEnum codeEnum) {
        this(codeEnum.getCode(), codeEnum.getValue());
    }

    public KnownException(CodeEnum codeEnum, String level){
        this(codeEnum.getCode(), codeEnum.getValue(), level);
    }

    public KnownException(CodeEnum codeEnum, String level, Throwable throwable){
        this(codeEnum.getCode(), codeEnum.getValue(), level, throwable);
    }

    public KnownException(String msg) {
        this(CodeEnum.FAIL.getCode(), msg);
    }

    public KnownException(Integer code, String msg) {
        this(code, msg, Level.DEBUG.toString());
    }

    public KnownException(Integer code, String msg, String level) {
        this(code, msg, level, null);
    }

    public KnownException(Integer code, String msg, String level, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
        this.msg = msg;
        this.level = level;
    }


    public static KnownException systemError() {
        return new KnownException(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getValue(), Level.ERROR.toString());
    }

    public static KnownException systemError(Throwable throwable) {
        return new KnownException(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getValue(), Level.ERROR.toString(), throwable);
    }

    public static KnownException illegalArgument() {
        return new KnownException(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getValue(), Level.ERROR.toString());
    }

    public static KnownException illegalArgument(String argumentName) {
        return new KnownException(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getValue(), Level.ERROR.toString(), new IllegalArgumentException(argumentName));
    }
}
