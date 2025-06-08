package ${basePackage}.domain.aggregates${package};

import org.netcorepal.cap4j.ddd.domain.aggregate.Aggregate;
import ${basePackage}.domain.aggregates${package}.factory.${Entity}Factory;

/**
 * ${Entity}聚合封装
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
public class ${aggregateNameTemplate} extends Aggregate.Default<${Entity}> {
    public Agg${Entity}() {
        super(null);
    }

    public ${aggregateNameTemplate}(${Entity}Factory.Payload payload){
        super(payload);
    }

    public static class Id extends org.netcorepal.cap4j.ddd.domain.aggregate.Id.Default<Agg${Entity}, ${IdentityType}> {
        public Id(${IdentityType} key) {
            super(key);
        }
    }

    /**
     * 获取聚合ID
     * @return
     */
    public Id getId() {
        return new Id(root.getId());
    }


}
