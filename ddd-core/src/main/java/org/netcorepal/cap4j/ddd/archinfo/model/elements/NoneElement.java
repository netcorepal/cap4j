package org.netcorepal.cap4j.ddd.archinfo.model.elements;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

/**
 * 空元素
 *
 * @author binking338
 * @date 2024/11/25
 */
@Data
public class NoneElement implements Element {

    @Override
    public String getType() {
        return TYPE_NONE;
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
