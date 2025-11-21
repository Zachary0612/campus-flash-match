package com.campus.util;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.campus.dto.SettlementMsgDTO;
import com.campus.constant.EventConstant;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * RabbitMQ消息生产者
 */
@Component
public class RabbitMqProducer {
    private static final Logger logger = Logger.getLogger(RabbitMqProducer.class.getName());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送事件结算消息
     * @param eventId 事件ID
     * @param status 结算状态
     * @return 是否发送成功
     */
    public boolean sendSettlementMsg(String eventId, String status) {
        try {
            SettlementMsgDTO msg = new SettlementMsgDTO();
            msg.setEventId(eventId);
            msg.setStatus(status);
            return sendSettlementMsg(msg);
        } catch (Exception e) {
            logger.severe("发送结算消息失败 [eventId=" + eventId + ", status=" + status + "]: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送事件结算消息
     * @param msg 结算消息DTO
     * @return 是否发送成功
     */
    public boolean sendSettlementMsg(SettlementMsgDTO msg) {
        try {
            // 生成唯一消息ID
            String messageId = UUID.randomUUID().toString();
            CorrelationData correlationData = new CorrelationData(messageId);
            
            // 记录发送前日志
            logger.info("准备发送结算消息: " + JSONObject.toJSONString(msg));
            
            // 发送消息
            rabbitTemplate.convertAndSend(
                EventConstant.MQ_EXCHANGE_EVENT_SETTLEMENT,
                EventConstant.MQ_ROUTING_KEY_EVENT_SETTLEMENT,
                msg,
                correlationData
            );
            
            logger.info("结算消息发送成功 [messageId=" + messageId + ", eventId=" + msg.getEventId() + "]");
            return true;
        } catch (Exception e) {
            logger.severe("发送结算消息失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送延时消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     * @param delay 延时时间（毫秒）
     * @return 是否发送成功
     */
    public boolean sendDelayMessage(String exchange, String routingKey, Object message, long delay) {
        try {
            String messageId = UUID.randomUUID().toString();
            CorrelationData correlationData = new CorrelationData(messageId);
            
            logger.info("准备发送延时消息到交换机 [" + exchange + ", " + routingKey + "]: " + 
                        (message instanceof String ? message : JSONObject.toJSONString(message)) + 
                        ", 延时: " + delay + "毫秒");
            
            // 添加延时参数
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setDelay((int) delay);
                return msg;
            }, correlationData);
            
            logger.info("延时消息发送成功 [messageId=" + messageId + ", delay=" + delay + "ms]");
            return true;
        } catch (Exception e) {
            logger.severe("发送延时消息失败 [exchange=" + exchange + ", routingKey=" + routingKey + "]: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送通用消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        try {
            String messageId = UUID.randomUUID().toString();
            CorrelationData correlationData = new CorrelationData(messageId);
            
            logger.info("准备发送消息到交换机 [" + exchange + ", " + routingKey + "]: " + 
                        (message instanceof String ? message : JSONObject.toJSONString(message)));
            
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            
            logger.info("消息发送成功 [messageId=" + messageId + "]");
            return true;
        } catch (Exception e) {
            logger.severe("发送消息失败 [exchange=" + exchange + ", routingKey=" + routingKey + "]: " + e.getMessage());
            return false;
        }
    }
}