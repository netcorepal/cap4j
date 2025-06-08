package ${basePackage}.application.queries${package};

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestParam;


/**
 * ${Query}查询请求参数
 * ${CommentEscaped}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${Query}Request implements RequestParam<${Query}Response> {
    Long id;
}
