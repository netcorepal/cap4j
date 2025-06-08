package ${basePackage}.application.distributed.sagas${package};

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.application.saga.SagaParam;

/**
 * ${Saga}事务请求参数
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${Saga}Request implements SagaParam<${ReturnType}> {
    String param;

}