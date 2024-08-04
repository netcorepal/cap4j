package org.ddd.example.application.samples.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ddd.application.query.Query;
import org.ddd.domain.repo.AggregateRepository;
import org.ddd.example.domain.aggregates.samples.Order;
import org.ddd.example._share.exception.KnownException;
import org.springframework.stereotype.Service;


/**
 * 查询描述
 *
 * @date 2023/12/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderQry {
    private Long id;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Query<GetOrderQry, GetOrderQryDto> {
        private final AggregateRepository<Order,Long> repo;

        @Override
        public GetOrderQryDto exec(GetOrderQry param) {
            Order entity = repo.findById(param.getId())
                    .orElseThrow(() -> new KnownException("不存在"));
            entity = repo.findById(param.getId())
                    .orElseThrow(() -> new KnownException("不存在"));
            entity = repo.findById(param.getId())
                    .orElseThrow(() -> new KnownException("不存在"));

            return GetOrderQryDto.builder()
                    .id(entity.getId())
                    .amount(entity.getAmount())
                    .name(entity.getName())
                    .build();
        }
    }

    @Data
    @Builder
    public static class GetOrderQryDto {
        private Long id;
        private Integer amount;
        private String name;
    }
}