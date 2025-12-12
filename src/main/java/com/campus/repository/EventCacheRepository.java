package com.campus.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;
import com.campus.constant.EventConstant;
import com.campus.util.RedisUtil;

/**
 * 事件缓存仓储层
 * 封装所有事件相关的Redis操作
 */
@Repository
public class EventCacheRepository {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 存储事件位置信息（GEO）
     */
    public void saveEventLocation(String eventType, String eventId, double longitude, double latitude) {
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        redisUtil.geoAdd(eventLocationKey, longitude, latitude, eventId);
    }

    /**
     * 存储事件详细信息（Hash）
     */
    public void saveEventInfo(String eventId, Map<String, Object> eventInfo, long expireSeconds) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        eventInfo.forEach((key, value) -> redisUtil.hSet(eventInfoKey, key, value));
        redisUtil.expire(eventInfoKey, expireSeconds + 300, TimeUnit.SECONDS);
    }

    /**
     * 添加事件参与者
     */
    public void addParticipant(String eventId, Long userId, long expireSeconds) {
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        redisUtil.sAdd(eventParticipantsKey, userId.toString());
        redisUtil.expire(eventParticipantsKey, expireSeconds + 300, TimeUnit.SECONDS);
    }

    /**
     * 获取用户位置
     */
    public Point getUserLocation(Long userId) {
        // 使用常量，key为 "user:location:"，member为userId
        String key = "user:location:";
        List<Point> positions = redisUtil.geoPosition(key, userId.toString());
        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            System.out.println("用户位置未找到，userId: " + userId + ", key: " + key);
            return null;
        }
        System.out.println("用户位置获取成功，userId: " + userId + ", 位置: " + positions.get(0));
        return positions.get(0);
    }

    /**
     * 保存用户位置
     */
    public void saveUserLocation(Long userId, double longitude, double latitude) {
        String key = "user:location:";
        redisUtil.geoAdd(key, longitude, latitude, userId.toString());
        System.out.println("保存用户位置成功，userId: " + userId + ", 经度: " + longitude + ", 纬度: " + latitude);
    }

    /**
     * 查询附近事件（GEO半径搜索）
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> findNearbyEvents(
            String eventType, double longitude, double latitude, double radius) {
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        
        System.out.println("查询附近事件 - eventType: " + eventType + ", key: " + eventLocationKey 
                + ", 用户位置: (" + longitude + ", " + latitude + "), 半径: " + radius + "米");
        
        if (!redisUtil.hasKey(eventLocationKey)) {
            System.out.println("事件位置key不存在: " + eventLocationKey);
            return null;
        }
        
        DataType keyType = redisUtil.type(eventLocationKey);
        if (keyType != DataType.ZSET) {
            return null;
        }
        
        try {
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisUtil.geoRadius(
                    eventLocationKey,
                    longitude,
                    latitude,
                    radius,
                    RedisGeoCommands.DistanceUnit.METERS
            );
            System.out.println("GEO查询结果数量: " + (results != null ? results.getContent().size() : 0));
            return results;
        } catch (Exception e) {
            System.err.println("执行geoRadius操作时发生异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取事件详细信息
     */
    public Map<Object, Object> getEventInfo(String eventId) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        if (!redisUtil.hasKey(eventInfoKey)) {
            return null;
        }
        
        DataType keyType = redisUtil.type(eventInfoKey);
        if (keyType != DataType.HASH) {
            return null;
        }
        
        return redisUtil.hGetAll(eventInfoKey);
    }

    /**
     * 获取事件单个字段
     */
    public Object getEventField(String eventId, String field) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        return redisUtil.hGet(eventInfoKey, field);
    }

    /**
     * 检查事件是否存在
     */
    public boolean eventExists(String eventId) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        return redisUtil.hasKey(eventInfoKey);
    }

    /**
     * 检查事件是否过期
     */
    public boolean isEventExpired(String eventId) {
        Object expireTimeObj = getEventField(eventId, "expire_time");
        if (expireTimeObj == null) {
            return true;
        }
        LocalDateTime expireTime = LocalDateTime.parse(String.valueOf(expireTimeObj));
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取事件参与者集合
     */
    public Set<Object> getParticipants(String eventId) {
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        return redisUtil.sMembers(eventParticipantsKey);
    }

    /**
     * 检查用户是否已参与事件
     */
    public boolean isParticipant(String eventId, Long userId) {
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        return redisUtil.sIsMember(eventParticipantsKey, userId.toString());
    }

    /**
     * 事务性加入事件（并发控制）
     */
    public JoinEventResult joinEventTransaction(String eventId, Long userId) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        
        final JoinEventResult result = new JoinEventResult();
        
        try {
            redisUtil.execute(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) {
                    try {
                        operations.watch(eventInfoKey);
                        operations.watch(eventParticipantsKey);

                        if (!operations.hasKey(eventInfoKey)) {
                            result.setSuccess(false);
                            result.setFailureReason("EVENT_NOT_FOUND");
                            return null;
                        }

                        Map<Object, Object> eventInfoMap = operations.opsForHash().entries(eventInfoKey);
                        if (eventInfoMap == null || eventInfoMap.isEmpty()) {
                            result.setSuccess(false);
                            result.setFailureReason("EVENT_NOT_FOUND");
                            return null;
                        }
                        
                        String eventType = String.valueOf(eventInfoMap.get("event_type"));
                        Integer currentNum = Integer.valueOf(String.valueOf(eventInfoMap.get("current_num")));
                        Integer targetNum = Integer.valueOf(String.valueOf(eventInfoMap.get("target_num")));

                        // 调试：打印参与者集合内容
                        Set<Object> currentParticipants = operations.opsForSet().members(eventParticipantsKey);
                        System.out.println("joinEventTransaction Debug: eventId=" + eventId + ", userId=" + userId 
                                + ", participantsKey=" + eventParticipantsKey 
                                + ", currentParticipants=" + currentParticipants);
                        
                        Boolean isMember = operations.opsForSet().isMember(eventParticipantsKey, userId.toString());
                        System.out.println("joinEventTransaction Debug: isMember check for userId=" + userId + " result=" + isMember);
                        
                        if (Boolean.TRUE.equals(isMember)) {
                            result.setSuccess(false);
                            result.setFailureReason("ALREADY_JOINED");
                            return null;
                        }

                        if (currentNum >= targetNum) {
                            result.setSuccess(false);
                            result.setFailureReason("EVENT_FULL");
                            return null;
                        }

                        if (EventConstant.EVENT_TYPE_BEACON.equals(eventType) && currentNum >= 1) {
                            result.setSuccess(false);
                            result.setFailureReason("BEACON_FULL");
                            return null;
                        }

                        operations.multi();
                        operations.opsForHash().increment(eventInfoKey, "current_num", 1);
                        operations.opsForSet().add(eventParticipantsKey, userId.toString());

                        List<Object> execResult = operations.exec();
                        boolean success = (execResult != null && !execResult.isEmpty());
                        result.setSuccess(success);
                        
                        if (success) {
                            try {
                                result.setCurrentNum(((Number) execResult.get(0)).intValue());
                                result.setTargetNum(targetNum);
                            } catch (Exception e) {
                                // 如果无法获取返回值，重新查询
                                Map<Object, Object> updatedInfo = operations.opsForHash().entries(eventInfoKey);
                                result.setCurrentNum(Integer.valueOf(String.valueOf(updatedInfo.get("current_num"))));
                                result.setTargetNum(Integer.valueOf(String.valueOf(updatedInfo.get("target_num"))));
                            }
                        } else {
                            result.setFailureReason("TRANSACTION_FAILED");
                        }
                    } catch (Exception e) {
                        System.err.println("joinEventTransaction内部错误: eventId=" + eventId + ", userId=" + userId + ", error=" + e.getMessage());
                        e.printStackTrace();
                        result.setSuccess(false);
                        result.setFailureReason("INTERNAL_ERROR");
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("joinEventTransaction执行失败: eventId=" + eventId + ", userId=" + userId + ", error=" + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setFailureReason("REDIS_ERROR");
        }
        
        return result;
    }

    /**
     * 事务性退出事件
     */
    public boolean quitEventTransaction(String eventId, Long userId) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        
        final Boolean[] success = {false};
        
        redisUtil.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(eventInfoKey);
                operations.watch(eventParticipantsKey);
                operations.multi();
                operations.opsForHash().increment(eventInfoKey, "current_num", -1);
                operations.opsForSet().remove(eventParticipantsKey, userId.toString());
                List<Object> result = operations.exec();
                success[0] = (result != null && !result.isEmpty());
                return null;
            }
        });
        
        return success[0];
    }

    /**
     * 清理事件数据
     */
    public void cleanupEventData(String eventId, String eventType) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        String eventConfirmationsKey = EventConstant.REDIS_KEY_EVENT_CONFIRMATIONS + eventId;
        
        redisUtil.delete(eventInfoKey);
        redisUtil.delete(eventParticipantsKey);
        redisUtil.delete(eventConfirmationsKey);
        redisUtil.geoRemove(eventLocationKey, eventId);
    }
    
    /**
     * 更新事件状态
     */
    public void updateEventStatus(String eventId, String status) {
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        redisUtil.hSet(eventInfoKey, "status", status);
    }
    
    /**
     * 获取事件状态
     */
    public String getEventStatus(String eventId) {
        Object status = getEventField(eventId, "status");
        return status != null ? String.valueOf(status) : EventConstant.EVENT_STATUS_ACTIVE;
    }
    
    /**
     * 添加用户确认
     */
    public void addConfirmation(String eventId, Long userId, long expireSeconds) {
        String confirmationsKey = EventConstant.REDIS_KEY_EVENT_CONFIRMATIONS + eventId;
        redisUtil.sAdd(confirmationsKey, userId.toString());
        redisUtil.expire(confirmationsKey, expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 检查用户是否已确认
     */
    public boolean hasConfirmed(String eventId, Long userId) {
        String confirmationsKey = EventConstant.REDIS_KEY_EVENT_CONFIRMATIONS + eventId;
        return redisUtil.sIsMember(confirmationsKey, userId.toString());
    }
    
    /**
     * 获取已确认用户数量
     */
    public long getConfirmationCount(String eventId) {
        String confirmationsKey = EventConstant.REDIS_KEY_EVENT_CONFIRMATIONS + eventId;
        Long size = redisUtil.sSize(confirmationsKey);
        return size != null ? size : 0;
    }
    
    /**
     * 获取已确认用户集合
     */
    public Set<Object> getConfirmations(String eventId) {
        String confirmationsKey = EventConstant.REDIS_KEY_EVENT_CONFIRMATIONS + eventId;
        return redisUtil.sMembers(confirmationsKey);
    }

    /**
     * 获取所有事件ID（用于定时任务扫描）
     */
    public Set<Object> getAllEventIds(String eventType) {
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        
        if (!redisUtil.hasKey(eventLocationKey)) {
            return null;
        }
        
        DataType keyType = redisUtil.type(eventLocationKey);
        if (keyType != DataType.ZSET) {
            return null;
        }
        
        return redisUtil.zRange(eventLocationKey, 0, -1);
    }

    /**
     * 加入事件结果DTO
     */
    public static class JoinEventResult {
        private boolean success;
        private String failureReason;
        private Integer currentNum;
        private Integer targetNum;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public void setFailureReason(String failureReason) {
            this.failureReason = failureReason;
        }

        public Integer getCurrentNum() {
            return currentNum;
        }

        public void setCurrentNum(Integer currentNum) {
            this.currentNum = currentNum;
        }

        public Integer getTargetNum() {
            return targetNum;
        }

        public void setTargetNum(Integer targetNum) {
            this.targetNum = targetNum;
        }
    }
}
