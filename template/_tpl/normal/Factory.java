package ${basePackage}.domain.aggregates${package}.factory;

import ${basePackage}.domain.aggregates${package}.${Entity};
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.springframework.stereotype.Service;

/**
 * ${Entity}聚合工厂
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Aggregate(aggregate = "${Aggregate}", name = "${Entity}Factory", type = Aggregate.TYPE_FACTORY, description = "")
@Service
public class ${Entity}Factory implements AggregateFactory<${Entity}Payload, ${Entity}> {

    @Override
    public ${Entity} create(${Entity}Payload payload) {

        return ${Entity}.builder()

        .build();
    }
}
