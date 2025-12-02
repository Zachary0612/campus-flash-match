package com.campus.service.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.campus.service.NotificationService;
import com.campus.util.RedisUtil;
import com.campus.util.WebSocketUtil;

/**
 * 事件通知服务
 * 负责WebSocket推送 + 数据库持久化
 */
@Service
public class EventNotificationService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private NotificationService notificationService;

    /**
     * 推送附近用户通知（新事件发布时）
     */
    public void pushNearbyUserNotify(String eventId, String title, double longitude, double latitude) {
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisUtil.geoRadius(
                RedisConstant.REDIS_KEY_USER_LOCATION,
                longitude,
                latitude,
                1000, // 1公里
                RedisGeoCommands.DistanceUnit.METERS
        );

        if (geoResults != null && !geoResults.getContent().isEmpty()) {
            String notifyMsg = String.format(
                    "附近有新事件：%s（%s）",
                    title,
                    eventId
            );

            for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {
                String userIdStr = (String) geoResult.getContent().getName();
                try {
                    WebSocketUtil.sendMessage(Long.valueOf(userIdStr), notifyMsg);
                } catch (Exception e) {
                    // 忽略发送失败
                }
            }
        }
    }

    /**
     * 推送事件满员通知
     */
    public void pushEventFullNotify(String eventId, String title, Set<Object> participants, Integer currentNum, Integer targetNum) {
        if (participants == null || participants.isEmpty()) {
            System.err.println("推送事件满员通知失败: 参与者列表为空, eventId=" + eventId);
            return;
        }
        
        String notifyMsg = String.format(
                "事件【%s】已满员（%d/%d），等待结算！",
                title, currentNum, targetNum
        );

        List<Long> userIds = new ArrayList<>();
        for (Object obj : participants) {
            Long userId = Long.valueOf(String.valueOf(obj));
            userIds.add(userId);
        }
        
        System.out.println("推送事件满员通知: eventId=" + eventId + ", userIds=" + userIds);

        // 持久化通知到数据库（会自动推送WebSocket）
        try {
            notificationService.sendBatchNotification(userIds, "event_full", "事件满员", notifyMsg, eventId, null);
        } catch (Exception e) {
            System.err.println("推送事件满员通知失败: eventId=" + eventId + ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 推送结算结果通知
     */
    public void pushSettleNotify(List<Long> userIds, String eventId, String title, String status) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        String statusText = EventConstant.SETTLE_STATUS_SUCCESS.equals(status) ? "成功" : 
                           EventConstant.EVENT_STATUS_CANCELLED.equals(status) ? "已取消" : "过期失败";
        String notifyMsg = String.format(
                "事件【%s】已结算，状态：%s",
                title, statusText
        );
        
        String notifyType = EventConstant.SETTLE_STATUS_SUCCESS.equals(status) ? "event_completed" : "event_expired";

        // 持久化通知到数据库（会自动推送WebSocket）
        try {
            notificationService.sendBatchNotification(userIds, notifyType, "事件结算", notifyMsg, eventId, null);
        } catch (Exception e) {
            System.err.println("推送结算通知失败: eventId=" + eventId + ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }
}
