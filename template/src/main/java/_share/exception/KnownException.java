package ${basePackage}._share.exception;

import ${basePackage}._share.CodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务中断
 * @author cap4j-gen-arch
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class KnownException extends RuntimeException {

    public enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private Integer code;

    private String msg;

    private String level;

    public KnownException(CodeEnum codeEnum) {
        this(codeEnum.getCode(), codeEnum.getName());
    }

    public KnownException(CodeEnum codeEnum, String level){
        this(codeEnum.getCode(), codeEnum.getName(), level);
    }

    public KnownException(CodeEnum codeEnum, String level, Throwable Throwable){
        this(codeEnum.getCode(), codeEnum.getName(), level, Throwable);
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

    public KnownException(Integer code, String msg, String level, Throwable Throwable) {
        super(msg, Throwable);
        this.code = code;
        this.msg = msg;
        this.level = level;
    }


    public static KnownException systemError() {
        return new KnownException(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getName(), Level.ERROR.toString());
    }

    public static KnownException systemError(Throwable Throwable) {
        return new KnownException(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getName(), Level.ERROR.toString(), Throwable);
    }

    public static KnownException illegalArgument() {
        return new KnownException(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getName(), Level.ERROR.toString());
    }

    public static KnownException illegalArgument(String argumentName) {
        return new KnownException(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getName(), Level.ERROR.toString(), new IllegalArgumentException(argumentName));
    }
}
