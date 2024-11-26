package org.netcorepal.cap4j.ddd.archinfo.model.elements;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

/**
 * 元素引用
 *
 * @author binking338
 * @date 2024/11/25
 */
@Data
@Builder
public class ElementRef implements Element {
    String ref;
    String name;
    String description;

    @Override
    public String getType() {
        return TYPE_REF;
    }
}
