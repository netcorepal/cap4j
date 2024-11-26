package org.netcorepal.cap4j.ddd.archinfo.model.elements.pubsub;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.ClassRef;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

/**
 * 订阅
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class SubscriberElement implements Element, ClassRef {
    String classRef;
    String name;
    String description;
    String eventRef;

    @Override
    public String getType() {
        return TYPE_SUBSCRIBER;
    }
}
