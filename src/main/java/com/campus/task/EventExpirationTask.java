package com.campus.task;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.campus.constant.EventConstant;
import com.campus.dto.SettlementMsgDTO;
import com.campus.util.RedisUtil;
import com.campus.util.RabbitMqProducer;

/**
 * 事件过期处理定时任务
 * 每分钟扫描Redis中的过期事件，发送结算消息到MQ
 */
@Component
public class EventExpirationTask {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RabbitMqProducer rabbitMqProducer;

    /**
     * 每分钟执行一次，扫描过期事件
     */
    @Scheduled(fixedRate = 60000) // 60秒执行一次
    public void scanExpiredEvents() {
        try {
            // 扫描所有事件类型的过期事件
            scanExpiredEventsByType(EventConstant.EVENT_TYPE_GROUP_BUY);
            scanExpiredEventsByType(EventConstant.EVENT_TYPE_MEETUP);
            scanExpiredEventsByType(EventConstant.EVENT_TYPE_BEACON);
        } catch (Exception e) {
            System.err.println("扫描过期事件时发生异常: " + e.getMessage());
        }
    }

    /**
     * 扫描指定类型的过期事件
     */
    private void scanExpiredEventsByType(String eventType) {
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        
        // 检查键是否存在
        if (!redisUtil.hasKey(eventLocationKey)) {
            return;
        }
        
        // 检查键类型
        DataType keyType = redisUtil.type(eventLocationKey);
        if (keyType != DataType.ZSET) {
            return;
        }

        try {
            // 获取所有事件ID
            Set<Object> eventIds = redisUtil.zRange(eventLocationKey, 0, -1);
            
            for (Object eventIdObj : eventIds) {
                String eventId = (String) eventIdObj;
                String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
                
                // 检查事件信息是否存在
                if (!redisUtil.hasKey(eventInfoKey)) {
                    // 清理位置信息中的无效事件ID
                    redisUtil.geoRemove(eventLocationKey, eventId);
                    continue;
                }
                
                // 检查事件是否过期
                Object expireTimeObj = redisUtil.hGet(eventInfoKey, "expire_time");
                if (expireTimeObj != null) {
                    try {
                        LocalDateTime expireTime = LocalDateTime.parse(String.valueOf(expireTimeObj));
                        LocalDateTime now = LocalDateTime.now();
                        
                        // 如果事件已过期
                        if (now.isAfter(expireTime)) {
                            // 发送过期结算消息到MQ
                            SettlementMsgDTO msgDTO = new SettlementMsgDTO();
                            msgDTO.setEventId(eventId);
                            msgDTO.setStatus(EventConstant.SETTLE_STATUS_FAILED_TIMEOUT);
                            msgDTO.setSendTime(new Date());
                            
                            rabbitMqProducer.sendSettlementMsg(msgDTO);
                            System.out.println("发送过期事件结算消息: " + eventId);
                            
                            // 立即清理Redis中的过期事件数据，避免继续显示在附近事件中
                            cleanupExpiredEvent(eventId, eventType);
                        }
                    } catch (Exception e) {
                        System.err.println("处理事件过期时间时发生异常，事件ID: " + eventId + ", 错误: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("扫描事件类型 " + eventType + " 时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期事件的Redis数据
     */
    private void cleanupExpiredEvent(String eventId, String eventType) {
        try {
            String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
            String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
            String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
            
            // 删除事件信息
            redisUtil.delete(eventInfoKey);
            
            // 删除参与者集合
            redisUtil.delete(eventParticipantsKey);
            
            // 从地理位置中移除事件
            redisUtil.geoRemove(eventLocationKey, eventId);
            
            System.out.println("已清理过期事件数据: " + eventId);
        } catch (Exception e) {
            System.err.println("清理过期事件数据失败: " + eventId + ", 错误: " + e.getMessage());
        }
    }
}