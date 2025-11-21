package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.campus.util.RabbitMqProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Redis配置：序列化、键空间通知、消息监听
 */
@Configuration
public class RedisConfig {
    private static final Logger logger = Logger.getLogger(RedisConfig.class.getName());

    @Autowired
    private RabbitMqProducer rabbitMqProducer;

    /**
     * 配置RedisTemplate（解决序列化乱码问题）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 字符串序列化器（Key）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // JSON序列化器（Value）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 配置Key、Value、HashKey、HashValue的序列化器
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置Redis键空间通知（监听事件过期）
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.setErrorHandler(throwable -> {
            logger.severe("Redis消息监听容器错误: " + throwable.getMessage());
        });

        // 监听过期事件通道：__keyevent@0__:expired（数据库0）
        MessageListenerAdapter messageListener = new MessageListenerAdapter();
        messageListener.setDelegate(new RedisKeyExpirationListener(rabbitMqProducer));
        messageListener.setDefaultListenerMethod("handleMessage");
        
        // 设置消息序列化器
        messageListener.setSerializer(RedisSerializer.string());

        // 注册过期事件监听器
        container.addMessageListener(
                messageListener,
                new org.springframework.data.redis.listener.PatternTopic("__keyevent@0__:expired")
        );

        return container;
    }

    /**
     * Redis键过期监听器
     */
    @Component
    public static class RedisKeyExpirationListener {
        private final RabbitMqProducer rabbitMqProducer;
        private static final Logger logger = Logger.getLogger(RedisKeyExpirationListener.class.getName());

        public RedisKeyExpirationListener(RabbitMqProducer rabbitMqProducer) {
            this.rabbitMqProducer = rabbitMqProducer;
        }

        public void handleMessage(String expiredKey) {
            try {
                logger.info("接收到Redis键过期事件: " + expiredKey);
                
                // 处理事件详情Key过期
                if (expiredKey.startsWith(EventConstant.REDIS_KEY_EVENT_INFO)) {
                    String eventId = expiredKey.replace(EventConstant.REDIS_KEY_EVENT_INFO, "");
                    logger.info("处理事件过期: " + eventId);
                    
                    // 发送过期结算消息到MQ
                    boolean success = rabbitMqProducer.sendSettlementMsg(
                            eventId, 
                            EventConstant.SETTLE_STATUS_FAILED_TIMEOUT
                    );
                    
                    if (success) {
                        logger.info("事件过期结算消息发送成功: " + eventId);
                    } else {
                        logger.warning("事件过期结算消息发送失败: " + eventId);
                    }
                }
                // 可以添加其他类型的过期键处理
                else if (expiredKey.startsWith(RedisConstant.REDIS_KEY_USER_LOCATION_TEMP)) {
                    // 处理临时位置缓存过期等
                    logger.fine("处理临时位置缓存过期: " + expiredKey);
                }
            } catch (Exception e) {
                logger.severe("处理Redis键过期事件失败: " + e.getMessage());
            }
        }
    }
}