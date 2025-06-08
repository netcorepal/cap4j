package ${basePackage}.application.subscribers.domain;

import ${basePackage}.domain.aggregates${package}.events.${DomainEvent};
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * ${Entity}.${DomainEvent}领域事件订阅
 * ${CommentEscaped}
 */
@Service
@RequiredArgsConstructor
public class ${DomainEvent}Subscriber {

    @EventListener(${DomainEvent}.class)
    public void on(${DomainEvent} event) {

    }

}
