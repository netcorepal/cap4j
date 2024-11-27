package org.netcorepal.cap4j.ddd.archinfo.model.elements.reqres;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.ClassRef;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

/**
 * 命令
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class CommandElement implements Element, ClassRef {
    String classRef;
    String requestClassRef;
    String responseClassRef;
    String name;
    String description;

    @Override
    public String getType() {
        return TYPE_COMMAND;
    }
}
