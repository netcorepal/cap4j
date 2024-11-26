package org.netcorepal.cap4j.ddd.archinfo.model.elements.aggreagate;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.ClassRef;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

/**
 * 实体规约
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class SpecificationElement implements Element, ClassRef {
    String classRef;
    String name;
    String description;

    @Override
    public String getType() {
        return TYPE_SPECIFICATION;
    }
}
