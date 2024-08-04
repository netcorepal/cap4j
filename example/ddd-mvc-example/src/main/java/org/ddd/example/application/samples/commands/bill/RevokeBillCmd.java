package org.ddd.example.application.samples.commands.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ddd.application.command.Command;
import org.ddd.domain.repo.Repository;
import org.ddd.domain.repo.UnitOfWork;
import org.ddd.example.domain.aggregates.samples.Bill;
import org.ddd.example.domain.aggregates.samples.events.OrderClosedDomainEvent;
import org.ddd.example.domain.meta.schemas.BillSchema;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


/**
 * 命令描述
 *
 * @date 2024/4/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeBillCmd {
    Long id;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<RevokeBillCmd, Boolean> {
        private final Repository<Bill> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Boolean exec(RevokeBillCmd cmd) {
            Bill bill = repo.getBy(BillSchema.specify(b -> b.id().eq(cmd.getId())))
                    .orElse(null);
            bill.revoke();
            unitOfWork.persist(bill);
            unitOfWork.save();
            return true;
        }

        @EventListener(OrderClosedDomainEvent.class)
        public void listenOn(OrderClosedDomainEvent event){
            Bill bill = repo.getBy(BillSchema.specify(b -> b.orderId().eq(event.getOrder().getId())))
                    .orElse(null);
            if(bill == null){
                return;
            }
            exec(RevokeBillCmd.builder()
                    .id(bill.getId())
                    .build());
        }
    }
}