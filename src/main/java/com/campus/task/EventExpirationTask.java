package com.campus.task;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.campus.constant.EventConstant;
import com.campus.dto.SettlementMsgDTO;
import com.campus.repository.EventCacheRepository;
import com.campus.util.RabbitMqProducer;

/**
 * 事件过期处理定时任务
 * 每分钟扫描Redis中的过期事件，发送结算消息到MQ
 */
@Component
public class EventExpirationTask {

    @Autowired
    private EventCacheRepository eventCacheRepository;

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
        try {
            // 获取所有事件ID
            Set<Object> eventIds = eventCacheRepository.getAllEventIds(eventType);
            
            if (eventIds == null || eventIds.isEmpty()) {
                return;
            }
            
            for (Object eventIdObj : eventIds) {
                String eventId = (String) eventIdObj;
                
                // 检查事件信息是否存在
                if (!eventCacheRepository.eventExists(eventId)) {
                    // 清理位置信息中的无效事件ID
                    eventCacheRepository.cleanupEventData(eventId, eventType);
                    continue;
                }
                
                // 检查事件是否过期
                if (eventCacheRepository.isEventExpired(eventId)) {
                    // 发送过期结算消息到MQ
                    SettlementMsgDTO msgDTO = new SettlementMsgDTO();
                    msgDTO.setEventId(eventId);
                    msgDTO.setStatus(EventConstant.SETTLE_STATUS_FAILED_TIMEOUT);
                    msgDTO.setSendTime(new Date());
                    
                    rabbitMqProducer.sendSettlementMsg(msgDTO);
                    System.out.println("发送过期事件结算消息: " + eventId);
                    
                    // 立即清理Redis中的过期事件数据，避免继续显示在附近事件中
                    eventCacheRepository.cleanupEventData(eventId, eventType);
                }
            }
        } catch (Exception e) {
            System.err.println("扫描事件类型 " + eventType + " 时发生异常: " + e.getMessage());
        }
    }
}