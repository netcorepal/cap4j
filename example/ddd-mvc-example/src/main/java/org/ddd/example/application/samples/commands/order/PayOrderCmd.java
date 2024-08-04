package org.ddd.example.application.samples.commands.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ddd.application.command.Command;
import org.ddd.domain.repo.AggregateRepository;
import org.ddd.domain.repo.UnitOfWork;
import org.ddd.example.domain.aggregates.samples.Order;
import org.ddd.example.domain.aggregates.samples.events.BillPayedDomainEvent;
import org.ddd.example._share.exception.KnownException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


/**
 * 命令描述
 *
 * @date 2024/4/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderCmd {
    private Long id;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<PayOrderCmd, Boolean> {
        private final AggregateRepository<Order, Long> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Boolean exec(PayOrderCmd cmd) {
            Order order = repo.findById(cmd.getId())
                    .orElseThrow(()-> new KnownException("不存在"));
            order.pay();
            unitOfWork.persist(order);
            unitOfWork.save();
            return true;
        }

        @EventListener(BillPayedDomainEvent.class)
        public void listenOn(BillPayedDomainEvent event){
            PayOrderCmd cmd =  PayOrderCmd.builder()
                    .id(event.getBill().getOrderId())
                    .build();
            exec(cmd);
        }
    }
}