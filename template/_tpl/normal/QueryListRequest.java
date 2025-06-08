package ${basePackage}.application.queries${package};

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.application.query.ListQueryParam;


/**
 * ${Query}查询请求参数
 * ${CommentEscaped}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${Query}Request implements ListQueryParam<${Query}Response> {
    Long id;
}
