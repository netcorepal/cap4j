package org.netcorepal.cap4j.ddd.archinfo.model.elements.pubsub;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.ClassRef;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

import java.util.List;

/**
 * 集成事件
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class IntegrationEventElement implements Element, ClassRef {
    String classRef;
    String name;
    String description;
    List<String> subscribersRef;

    @Override
    public String getType() {
        return TYPE_INTEGRATION_EVENT;
    }
}