package org.ddd.example.domain.aggregates.samples.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ddd.domain.event.annotation.DomainEvent;
import org.ddd.example.domain.aggregates.samples.Bill;
import org.ddd.example.domain.aggregates.samples.Order;

/**
 * @author binking338
 * @date 2024/4/20
 */
@DomainEvent(
        value = "",
        subscriber = DomainEvent.NONE_SUBSCRIBER,
        persist = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPayedDomainEvent {
    Bill bill;
}
