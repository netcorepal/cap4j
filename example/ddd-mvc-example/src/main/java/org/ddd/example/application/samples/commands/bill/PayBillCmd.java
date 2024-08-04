package org.ddd.example.application.samples.commands.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ddd.application.command.Command;
import org.ddd.domain.repo.AggregateRepository;
import org.ddd.domain.repo.UnitOfWork;
import org.ddd.example.domain.aggregates.samples.Bill;
import org.ddd.example.domain.meta.schemas.BillSchema;
import org.ddd.example._share.exception.KnownException;
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
public class PayBillCmd {
    Long id;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<PayBillCmd, Boolean> {
        private final AggregateRepository<Bill, Long> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Boolean exec(PayBillCmd cmd) {
            Bill bill = repo.findOne(BillSchema.specify(b -> b.id().eq(cmd.getId())))
                    .orElseThrow(() -> new KnownException("账单不存在"));
            bill.pay();

            unitOfWork.persist(bill);
            unitOfWork.save();
            return true;
        }
    }
}