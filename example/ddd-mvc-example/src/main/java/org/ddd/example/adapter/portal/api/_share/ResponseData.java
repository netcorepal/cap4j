package org.ddd.example.adapter.portal.api._share;

import org.ddd.example._share.CodeEnum;
import org.ddd.example._share.exception.ErrorException;
import org.ddd.example._share.exception.KnownException;
import org.ddd.example._share.exception.WarnException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * @author <template/>
 * @date
 */
@Data
@Schema(description = "接口响应格式")
public class ResponseData<T> {
    @Schema(description="状态码")
    private Status status;
    @Schema(description="结果集")
    private T data;
    @Schema(description="服务器时间")
    private long currentTime;

    private ResponseData() {
    }

    private ResponseData(T data) {
        this.status = new Status(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getValue());
        this.currentTime = System.currentTimeMillis();
        this.data = data;
    }

    private ResponseData(Integer code, String msg, T data) {
        this.status = new Status(code, msg);
        if (code == null) {
            this.status.setRetCode(CodeEnum.FAIL.getCode());
        }

        if (msg == null) {
            this.status.setMsg("");
        }

        this.data = data;
        this.currentTime = System.currentTimeMillis();
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
        return new ResponseData(codeEnum.getCode(), codeEnum.getValue(), null);
    }

    public static <T> ResponseData<T> fail(KnownException knownException) {
        return new ResponseData(knownException.getCode(), knownException.getMsg(), null);
    }

    public static <T> ResponseData<T> fail(Integer code, String msg) {
        code = code == null ? CodeEnum.FAIL.getCode() : code;
        return new ResponseData(code, msg, null);
    }

    public static <T> ResponseData<T> illegalArgument() {
        return new ResponseData(CodeEnum.PARAM_INVALIDATE.getCode(), CodeEnum.PARAM_INVALIDATE.getValue(), null);
    }

    public static <T> ResponseData<T> systemError() {
        return new ResponseData(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getValue(), null);
    }

    @JsonIgnore
    public boolean isSuccess() {
        Integer retCode = status.getRetCode();
        return Objects.equals(CodeEnum.SUCCESS.getCode(), retCode);
    }

    public void throwKnownException(){
        throw new KnownException(status.getRetCode(), status.getMsg());
    }

    public void throwWarnException(){
        throw new WarnException(status.getRetCode(), status.getMsg());
    }

    public void throwErrorException(){
        throw new ErrorException(status.getRetCode(), status.getMsg());
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

