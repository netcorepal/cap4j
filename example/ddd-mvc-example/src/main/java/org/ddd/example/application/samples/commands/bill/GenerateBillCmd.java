package org.ddd.example.application.samples.commands.bill;

import com.alibaba.fastjson.JSON;
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
import org.ddd.example.domain.aggregates.samples.events.OrderPlacedDomainEvent;
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
public class GenerateBillCmd {
    Long orderId;
    String name;
    String owner;
    Integer amount;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<GenerateBillCmd, Long> {
        private final Repository<Bill> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Long exec(GenerateBillCmd cmd) {
            Bill bill = repo.getBy(BillSchema.specify(b->b.orderId().eq(cmd.orderId)))
                    .orElse(null);
            if(bill!=null){
                return bill.getId();
            }
            bill = Bill.builder()
                    .orderId(cmd.orderId)
                    .name(cmd.name)
                    .owner(cmd.owner)
                    .amount(cmd.amount)
                    .build();
            unitOfWork.persist(bill);
            unitOfWork.save();
            return bill.getId();
        }

        @EventListener(OrderPlacedDomainEvent.class)
        public void listenOn(OrderPlacedDomainEvent event){
            log.info("领域事件消费：" + JSON.toJSON(event));
            GenerateBillCmd cmd = GenerateBillCmd.builder()
                    .orderId(event.getOrder().getId())
                    .amount(event.getOrder().getAmount())
                    .name(event.getOrder().getName())
                    .owner(event.getOrder().getOwner())
                    .build();
            exec(cmd);
        }
    }
}