package org.ddd.example.domain.aggregates.samples.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ddd.domain.event.annotation.DomainEvent;
import org.ddd.example.domain.aggregates.samples.Order;
import org.ddd.share.annotation.Retry;

/**
 * @author binking338
 * @date 2023/12/1
 */
@DomainEvent(
        value = "",
        subscriber = DomainEvent.NONE_SUBSCRIBER,
        persist = true)
@Retry(retryIntervals = {2})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedDomainEvent {
    Order order;
}
