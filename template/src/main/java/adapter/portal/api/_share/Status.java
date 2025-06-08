package ${basePackage}.adapter.portal.api._share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author cap4j-ddd-codegen
 */
@Data
@Schema(description = "接口返回状态")
public class Status {
    @Schema(description="业务状态码")
    private Integer code;
    @Schema(description="业务状态信息")
    private String msg;

    public Status() {
    }

    public Status(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
