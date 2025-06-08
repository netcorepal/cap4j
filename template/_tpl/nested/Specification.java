package ${basePackage}.domain.aggregates${package}.specs;

import ${basePackage}.domain.aggregates${package}.${Entity};
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;
import org.springframework.stereotype.Service;

/**
 * ${Entity}规格约束
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Aggregate(aggregate = "${Aggregate}", name = "${Entity}Specification", type = Aggregate.TYPE_SPECIFICATION, description = "")
@Service
public class ${Entity}Specification implements Specification<${Entity}> {
    @Override
    public Result specify(${Entity} entity) {
        return Result.pass();
    }
}
