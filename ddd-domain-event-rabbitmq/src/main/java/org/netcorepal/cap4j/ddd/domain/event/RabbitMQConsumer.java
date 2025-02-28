package org.netcorepal.cap4j.ddd.domain.event;

import com.rabbitmq.client.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 构建消费者对象
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
public class RabbitMQConsumer {

    private final ConnectionFactory connectionFactory;
    private final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

    private Connection connection;
    boolean durable = true;
    boolean autoDelete = false;
    boolean exclusive = false;
    private String queueName;

    public RabbitMQConsumer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * 关闭
     */
    public void shutdown() {
        // do nothing
    }

    /**
     * 启动
     */
    public void start() {
        // do nothing
    }

    public void registerMessageListener(Consumer<byte[]> consumer) {
        try {
            Connection connection = connectionFactory.createConnection();
            Channel channel = connection.createChannel(false);
            channel.basicQos(1);
            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        consumer.accept(body);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    }
                }
            });
            channel.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException cause) {
                    log.error("shutdown signal exception : {}", cause.getMessage());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    String exchange = "cap4j.ddd.event";
    String routing_key = "cap4j.ddd.routing";
    /**
     * 默认使用direct路由模式
     */
    String type = "direct";

    public void subscribe(String queue) {
        // TODO 重连？
        connection = connectionFactory.createConnection();
        // 创建交换机
        Channel channel = connection.createChannel(false);
        try {
            channel.exchangeDeclare(exchange, type, durable, autoDelete, null);
            channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            channel.queueBind(queue, exchange, routing_key);
            container.setQueueNames(queue);
            this.queueName = queue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeChannel(channel);
            closeConnection(connection);
        }
    }

    private static void closeChannel(Channel channel) {
        try {
            try {
                channel.close();
            } catch (IOException e) {
                log.error("close connection exception：{}", e.getMessage());
            }
            log.info("rabbitmq close channel");
        } catch (TimeoutException e) {
            log.info("close channel time out,{} ", e.getMessage());
        }
    }

    private static void closeConnection(Connection connection) {
        connection.close();
        log.info("rabbitmq close connection");
    }
}
