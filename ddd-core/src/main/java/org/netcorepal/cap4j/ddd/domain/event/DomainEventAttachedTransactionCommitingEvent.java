package org.netcorepal.cap4j.ddd.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 领域事件所在事务正在提交事件
 *
 * @author binking338
 * @date 2024/8/28
 */
public class DomainEventAttachedTransactionCommitingEvent extends ApplicationEvent {
    @Getter
    List<EventRecord> events;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public DomainEventAttachedTransactionCommitingEvent(Object source, List<EventRecord> events) {
        super(source);
        this.events = events;
    }
}