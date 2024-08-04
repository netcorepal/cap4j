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
import org.ddd.domain.event.RocketMqDomainEventSupervisor;
import org.ddd.example.domain.aggregates.samples.Order;
import org.ddd.example.domain.aggregates.samples.OrderItem;
import org.ddd.example.domain.aggregates.samples.enums.OrderStatus;
import org.ddd.example.domain.aggregates.samples.events.OrderPlacedDomainEvent;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 命令描述
 *
 * @date 2023/12/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderCmd {
    @NotBlank(message = "不为空")
    private String name;
    private Integer amount;
    private String owner;
    private List<PlaceOrderItem> orderItems;

    @Data
    public static class PlaceOrderItem{
        private String name;
        private Integer price;
        private Integer num;
    }

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<PlaceOrderCmd, Long> {
        private final Repository<Order> repo;
        private final UnitOfWork unitOfWork;

        @Override
        public Long exec(PlaceOrderCmd cmd) {
            Order order = Order.builder()
                    .name(cmd.name)
                    .amount(cmd.amount)
                    .owner(cmd.owner)
                    .orderItems(cmd.orderItems.stream().map(i -> OrderItem.builder()
                            .name(i.name)
                            .price(i.price)
                            .num(i.num)
                            .build()).collect(Collectors.toList()))
                    .build();
            order.place();
            unitOfWork.persist(order);
            unitOfWork.save();
            return order.getId();
        }
    }
}