package org.ddd.example.application.samples.commands.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ddd.application.command.Command;
import org.ddd.domain.repo.Repository;
import org.ddd.domain.repo.UnitOfWork;
import org.ddd.example.domain.aggregates.samples.Order;
import org.ddd.example.domain.meta.schemas.OrderSchema;
import org.ddd.example._share.exception.KnownException;
import org.springframework.stereotype.Service;


/**
 * 命令描述
 *
 * @date 2023/12/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseOrderCmd {
    private Long id;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<CloseOrderCmd, Boolean> {
        private final Repository<Order> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Boolean exec(CloseOrderCmd cmd) {
            Order order = repo.getBy(OrderSchema.specify(o -> o.id().eq(cmd.getId())))
                    .orElseThrow(()-> new KnownException("不存在"));
            order.close();
            unitOfWork.persist(order);
            unitOfWork.save();
            return true;
        }
    }
}