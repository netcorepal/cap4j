package ${basePackage}.application.distributed.clients${package};

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestParam;

/**
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
public class ${Client} {

    /**
     * ${Client}防腐端请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request implements RequestParam<Response> {
        Long id;
    }

    /**
     * ${Client}防腐端响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        Long id;
    }
}
