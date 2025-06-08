package ${basePackage}.domain.aggregates${package}.factory;

import ${basePackage}.domain.aggregates${package}.${Entity};
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregateFactory;
import org.netcorepal.cap4j.ddd.domain.aggregate.AggregatePayload;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.springframework.stereotype.Service;

/**
 * ${Entity}聚合工厂
 *
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Aggregate(aggregate = "${Entity}", name = "${Entity}Factory", type = Aggregate.TYPE_FACTORY, description = "")
@Service
public class ${Entity}Factory implements AggregateFactory<${Entity}Factory.Payload, ${Entity}> {

    @Override
    public ${Entity} create(Payload payload) {

        return ${Entity}.builder()

        .build();
    }

    /**
     * ${Entity}工厂负载
     */
    @Aggregate(aggregate = "${Entity}", name = "${Entity}Payload", type = Aggregate.TYPE_FACTORY_PAYLOAD, description = "")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload implements AggregatePayload<${Entity}> {
        String name;

   }
}
