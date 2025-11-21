package com.campus.consumer;

import com.campus.dto.SettlementMsgDTO;
import com.campus.service.EventService;
import com.campus.constant.EventConstant;
import com.campus.exception.BusinessException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

/**
 * 事件结算消息消费者
 * 监听RabbitMQ中的事件结算消息，处理事件过期或满员后的结算逻辑
 */
@Component
public class EventSettlementConsumer {

    private static final Logger logger = Logger.getLogger(EventSettlementConsumer.class.getName());

    @Autowired
    private EventService eventService;

    /**
     * 监听事件结算队列
     * @param msgDTO 结算消息DTO
     */
    @RabbitListener(queues = EventConstant.MQ_QUEUE_EVENT_SETTLEMENT)
    public void handleEventSettlement(SettlementMsgDTO msgDTO) {
        try {
            eventService.settleEvent(msgDTO);
        } catch (BusinessException e) {
            // 事件数据不存在等情况属于正常流程，记录日志即可
            logger.info("事件结算处理完成（事件可能已提前结算或不存在）: " + e.getMessage() + ", eventId: " + msgDTO.getEventId());
        } catch (Exception e) {
            // 其他异常需要记录错误日志
            logger.severe("事件结算处理异常: " + e.getMessage() + ", eventId: " + msgDTO.getEventId());
            throw e;
        }
    }
}