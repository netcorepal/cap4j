package ${basePackage}.application.subscribers.integration;

import ${basePackage}.application.distributed.events${subPackage}.${IntegrationEvent};
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * ${IntegrationEvent}集成事件订阅
 * ${CommentEscaped}
 */
@Service
@RequiredArgsConstructor
public class ${IntegrationEvent}Subscriber {

    @EventListener(${IntegrationEvent}.class)
    public void on(${IntegrationEvent} event) {

    }

}
