package ${basePackage}.adapter.portal.api._share;

import ${basePackage}._share.CodeEnum;
import ${basePackage}._share.exception.ErrorException;
import ${basePackage}._share.exception.KnownException;
import ${basePackage}._share.exception.WarnException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * @author cap4j-ddd-codegen
 */
@Data
@Schema(description = "接口响应格式")
public class ResponseData<T> {
    @Schema(description="状态码")
    private Status status;
    @Schema(description="结果集")
    private T data;
    @Schema(description="服务器时间")
    private long timestamp;

    private ResponseData() {
    }

    private ResponseData(T data) {
        this.status = new Status(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getName());
        this.timestamp = System.currentTimeMillis();
        this.data = data;
    }

    private ResponseData(Integer code, String msg, T data) {
        this.status = new Status(code, msg);
        if (code == null) {
            this.status.setCode(CodeEnum.FAIL.getCode());
        }

        if (msg == null) {
            this.status.setMsg("");
        }

        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData(data);
    }

    public static <T> ResponseData<T> success(String msg, T data) {
        return new ResponseData(CodeEnum.SUCCESS.getCode(), msg, data);
    }

    public static <T> ResponseData<T> fail(String msg) {
        return new ResponseData(CodeEnum.FAIL.getCode(), msg, null);
    }

    public static <T> ResponseData<T> fail(CodeEnum codeEnum) {
        return new ResponseData(codeEnum.getCode(), codeEnum.getName(), null);
    }

    public static <T> ResponseData<T> fail(KnownException knownException) {
        return new ResponseData(knownException.getCode(), knownException.getMsg(), null);
    }

    public static <T> ResponseData<T> fail(Integer code, String msg) {
        code = code == null ? CodeEnum.FAIL.getCode() : code;
        return new ResponseData(code, msg, null);
    }

    public static <T> ResponseData<T> illegalArgument() {
        return new ResponseData(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getName(), null);
    }

    public static <T> ResponseData<T> systemError() {
        return new ResponseData(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getName(), null);
    }

    @JsonIgnore
    public boolean isSuccess() {
        Integer retCode = status.getCode();
        return Objects.equals(CodeEnum.SUCCESS.getCode(), retCode);
    }

    public void throwKnownException(){
        throw new KnownException(status.getCode(), status.getMsg());
    }

    public void throwWarnException(){
        throw new WarnException(status.getCode(), status.getMsg());
    }

    public void throwErrorException(){
        throw new ErrorException(status.getCode(), status.getMsg());
    }

    public void tryThrowKnownException(){
        if(!isSuccess()){
            throwKnownException();
        }
    }

    public void tryThrowWarnException(){
        if(!isSuccess()){
            throwWarnException();
        }
    }

    public void tryThrowErrorException(){
        if(!isSuccess()){
            throwErrorException();
        }
    }
}

