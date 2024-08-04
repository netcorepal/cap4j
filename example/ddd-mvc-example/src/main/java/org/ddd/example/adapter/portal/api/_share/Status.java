package org.ddd.example.adapter.portal.api._share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author <template/>
 * @date
 */
@Data
@Schema(description = "接口返回状态")
public class Status {
    @Schema(description="业务状态码")
    private Integer retCode;
    @Schema(description="业务状态信息")
    private String msg;

    public Status() {
    }

    public Status(Integer retCode, String msg) {
        this.retCode = retCode;
        this.msg = msg;
    }
}
