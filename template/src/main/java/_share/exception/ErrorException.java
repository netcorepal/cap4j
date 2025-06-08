package ${basePackage}._share.exception;

import ${basePackage}._share.CodeEnum;

/**
 * 错误
 * @author cap4j-ddd-codegen
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

    public ErrorException(CodeEnum codeEnum, Throwable Throwable){
        super(codeEnum, Level.ERROR.toString(), Throwable);
    }

    public ErrorException(Integer code, String msg) {
        super(code, msg, Level.ERROR.toString());
    }

    public ErrorException(Integer code, String msg, Throwable Throwable) {
        super(code, msg, Level.ERROR.toString(), Throwable);
    }
}
