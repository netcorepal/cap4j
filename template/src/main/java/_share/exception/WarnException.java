package ${basePackage}._share.exception;

import ${basePackage}._share.CodeEnum;

/**
 * 告警
 * @author cap4j-gen-arch
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

    public WarnException(CodeEnum codeEnum, Throwable Throwable) {
        super(codeEnum, Level.WARN.toString(), Throwable);
    }

    public WarnException(Integer code, String msg) {
        super(code, msg, Level.WARN.toString());
    }

    public WarnException(Integer code, String msg, Throwable Throwable) {
        super(code, msg, Level.WARN.toString(), Throwable);
    }
}
