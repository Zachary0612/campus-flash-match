package com.campus.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.campus.constant.EventConstant;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类：交换机、队列、绑定、消息转换器
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 结算交换机：Direct类型
     */
    @Bean
    public DirectExchange settlementExchange() {
        // 持久化交换机，避免重启后丢失
        return ExchangeBuilder.directExchange(EventConstant.MQ_EXCHANGE_SETTLEMENT)
                .durable(true)
                .withArgument("alternate-exchange", EventConstant.MQ_EXCHANGE_DEAD_LETTER)
                .build();
    }

    /**
     * 通知交换机：Topic类型，支持多规则路由
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(EventConstant.MQ_EXCHANGE_NOTIFICATION)
                .durable(true)
                .build();
    }

    /**
     * 死信交换机：处理失败消息
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(EventConstant.MQ_EXCHANGE_DEAD_LETTER)
                .durable(true)
                .build();
    }

    /**
     * 结算队列：持久化，带死信配置
     */
    @Bean
    public Queue settlementQueue() {
        Map<String, Object> args = new HashMap<>();
        // 死信交换机
        args.put("x-dead-letter-exchange", EventConstant.MQ_EXCHANGE_DEAD_LETTER);
        // 死信路由键
        args.put("x-dead-letter-routing-key", EventConstant.MQ_ROUTING_KEY_DEAD_LETTER);
        // 消息TTL：30分钟
        args.put("x-message-ttl", 30 * 60 * 1000);
        // 最大队列长度
        args.put("x-max-length", 10000);
        // 持久化队列，避免重启后消息丢失
        return QueueBuilder.durable(EventConstant.MQ_QUEUE_EVENT_SETTLEMENT)
                .withArguments(args)
                .build();
    }

    /**
     * 通知队列：用户通知
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(EventConstant.MQ_QUEUE_NOTIFICATION)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(EventConstant.MQ_QUEUE_DEAD_LETTER)
                .build();
    }

    /**
     * 绑定：结算交换机和结算队列
     */
    @Bean
    public Binding settlementBinding(DirectExchange settlementExchange, Queue settlementQueue) {
        return BindingBuilder.bind(settlementQueue)
                .to(settlementExchange)
                .with(EventConstant.MQ_ROUTING_KEY_EVENT_SETTLEMENT);
    }

    /**
     * 绑定：通知交换机和通知队列
     */
    @Bean
    public Binding notificationBinding(TopicExchange notificationExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(EventConstant.MQ_ROUTING_KEY_NOTIFICATION + ".#");
    }

    /**
     * 绑定：死信交换机和死信队列
     */
    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(EventConstant.MQ_ROUTING_KEY_DEAD_LETTER);
    }

    /**
     * 消息转换器：使用JSON序列化
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate：消息确认机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 开启发布确认模式
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息已确认
                System.out.println("消息发送成功，ID: " + (correlationData != null ? correlationData.getId() : "null"));
            } else {
                // 消息发送失败
                System.out.println("消息发送失败，原因: " + cause);
            }
        });
        
        // 开启返回确认模式
        template.setMandatory(true);
        template.setReturnsCallback(returnedMessage -> {
            System.out.println("消息投递失败: " + returnedMessage.getMessage());
            System.out.println("交换机: " + returnedMessage.getExchange());
            System.out.println("路由键: " + returnedMessage.getRoutingKey());
            System.out.println("回复码: " + returnedMessage.getReplyCode());
            System.out.println("回复文本: " + returnedMessage.getReplyText());
        });
        
        return template;
    }

    /**
     * 配置监听器容器工厂：手动确认模式
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        
        // 手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 消费者数量
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        // 预取数量
        factory.setPrefetchCount(10);
        
        return factory;
    }
}