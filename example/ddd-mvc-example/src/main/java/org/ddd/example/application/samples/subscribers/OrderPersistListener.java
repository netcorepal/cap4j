package org.ddd.example.application.samples.subscribers;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.ddd.domain.repo.AbstractJpaPersistListener;
import org.ddd.example.domain.aggregates.samples.Order;
import org.springframework.stereotype.Service;

/**
 * @author binking338
 * @date 2024/4/14
 */
@Slf4j
@Service
public class OrderPersistListener extends AbstractJpaPersistListener<Order> {
    @Override
    public void onChange(Order order) {
        log.info("存储" + JSON.toJSONString(order));
    }

    @Override
    public void onCreate(Order order) {
        log.info("新增" + JSON.toJSONString(order));
    }

    @Override
    public void onUpdate(Order order) {

    }

    @Override
    public void onDelete(Order order) {

    }

    @Override
    public Class<Order> forEntityClass() {
        return Order.class;
    }
}
