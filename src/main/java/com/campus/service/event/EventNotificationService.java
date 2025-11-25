package com.campus.service.event;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.campus.util.RedisUtil;
import com.campus.util.WebSocketUtil;

/**
 * 事件通知服务
 * 负责WebSocket推送逻辑
 */
@Service
public class EventNotificationService {

    @Autowired
    private RedisUtil redisUtil;

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
    public void pushEventFullNotify(String eventId, Set<Object> participants, Integer currentNum, Integer targetNum) {
        String notifyMsg = String.format(
                "事件【%s】已满员（%d/%d），等待结算！",
                eventId, currentNum, targetNum
        );

        for (Object obj : participants) {
            Long userId = Long.valueOf(String.valueOf(obj));
            try {
                WebSocketUtil.sendMessage(userId, notifyMsg);
            } catch (Exception e) {
                // 忽略发送失败
            }
        }
    }

    /**
     * 推送结算结果通知
     */
    public void pushSettleNotify(List<Long> userIds, String eventId, String title, String status) {
        String notifyMsg = String.format(
                "事件【%s】已结算，状态：%s（%s）",
                title,
                EventConstant.SETTLE_STATUS_SUCCESS.equals(status) ? "成功" : "过期失败",
                eventId
        );

        for (Long userId : userIds) {
            try {
                WebSocketUtil.sendMessage(userId, notifyMsg);
            } catch (Exception e) {
                // 忽略发送失败
            }
        }
    }
}
