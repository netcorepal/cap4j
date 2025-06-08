package ${basePackage}.application.distributed.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;

/**
 * ${IntegrationEvent}集成事件
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@IntegrationEvent(value = ${MQ_TOPIC}, subscriber = ${MQ_CONSUMER})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ${IntegrationEvent} {
    private Long id;
}
