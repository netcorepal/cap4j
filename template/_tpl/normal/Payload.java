package ${basePackage}.domain.aggregates${package}.factory;

import ${basePackage}.domain.aggregates${package}.${Entity};
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;

/**
 * ${Entity}工厂负载
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Aggregate(aggregate = "${Aggregate}", name = "${Entity}Payload", type = Aggregate.TYPE_FACTORY_PAYLOAD, description = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${Entity}Payload implements AggregatePayload<${Entity}> {
    String name;

}
