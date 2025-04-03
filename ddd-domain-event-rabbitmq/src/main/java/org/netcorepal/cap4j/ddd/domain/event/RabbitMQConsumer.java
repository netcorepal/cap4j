package org.netcorepal.cap4j.ddd.domain.event;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 构建消费者对象
 *
 * @author fujc2dev@126.com
 * @date 2025-02-21
 */
@Slf4j
public class RabbitMQConsumer {

    private final ConnectionFactory connectionFactory;
    private final SimpleMessageListenerContainer messageListenerContainer;

    @Getter
    Connection connection;
    @Getter
    Channel channel;

    Consumer consumer;

    String exchange;
    String routingKey;
    private String queue;

    /**
     * 默认使用direct路由模式
     */
    String exchangeType = "direct";
    boolean exchangeDurable = true;
    boolean exchangeAutoDelete = false;
    Map<String, Object> exchangeArguments = null;

    boolean queueDurable = true;
    boolean queueExclusive = false;
    boolean queueAutoDelete = false;
    Map<String, Object> queueArguments = null;


    public RabbitMQConsumer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
    }

    /**
     * 关闭
     */
    public void shutdown() {
        closeChannel(channel);
        closeConnection(connection);
    }

    /**
     * 启动
     */
    public void start() {
        try {
            this.connection = connectionFactory.createConnection();
            this.channel = connection.createChannel(false);
            channel.basicQos(1);
            channel.addShutdownListener((ShutdownSignalException cause) -> {
                if (cause != null) {
                    log.error("shutdown signal exception : {}", cause.getMessage());
                }
            });
            channel.basicConsume(queue, false, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 注册消息监听器
     *
     * @param consumer
     */
    public void configureConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    /**
     * 配置exchange
     *
     * @param type
     * @param durable
     * @param autoDelete
     * @param arguments
     */
    public void configureExchange(String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        this.exchangeType = type;
        this.exchangeDurable = durable;
        this.exchangeAutoDelete = autoDelete;
        this.exchangeArguments = arguments;
    }

    /**
     * 配置queue
     *
     * @param durable
     * @param exclusive
     * @param autoDelete
     * @param arguments
     */
    public void configureQueue(boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
        this.queueDurable = durable;
        this.queueExclusive = exclusive;
        this.queueAutoDelete = autoDelete;
        this.queueArguments = arguments;
    }

    public void subscribe(String exchange, String routingKey, String queue) {
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;

        Connection connection = connectionFactory.createConnection();
        // 创建交换机
        Channel channel = connection.createChannel(false);
        try {
            channel.exchangeDeclare(exchange, exchangeType, exchangeDurable, exchangeAutoDelete, exchangeArguments);
            channel.queueDeclare(queue, queueDurable, queueExclusive, queueAutoDelete, queueArguments);
            channel.queueBind(queue, exchange, routingKey);
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
