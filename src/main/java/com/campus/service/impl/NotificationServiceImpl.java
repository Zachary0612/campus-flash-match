package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Notification;
import com.campus.entity.SysUser;
import com.campus.mapper.NotificationMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.service.NotificationService;
import com.campus.util.RedisUtil;
import com.campus.util.WebSocketUtil;
import com.campus.vo.NotificationVO;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通知消息Service实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String UNREAD_COUNT_CACHE_KEY = "notification:unread:";
    private static final long UNREAD_COUNT_CACHE_TTL = 30; // 30秒缓存

    @Override
    public List<NotificationVO> getNotifications(Long userId, String type, Integer pageNum, Integer pageSize) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (type != null && !type.isBlank()) {
            queryWrapper.eq("type", type);
        }
        queryWrapper.orderByDesc("create_time");

        Page<Notification> page = new Page<>(pageNum, pageSize);
        Page<Notification> result = notificationMapper.selectPage(page, queryWrapper);

        List<Notification> notifications = result.getRecords();
        if (notifications.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取发送者信息
        List<Long> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SysUser> senderMap = new java.util.HashMap<>();
        if (!senderIds.isEmpty()) {
            List<SysUser> senders = sysUserMapper.selectBatchIds(senderIds);
            senderMap = senders.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        List<NotificationVO> voList = new ArrayList<>();
        for (Notification n : notifications) {
            NotificationVO vo = new NotificationVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setRelatedId(n.getRelatedId());
            vo.setSenderId(n.getSenderId());
            vo.setIsRead(n.getIsRead() == 1);
            vo.setCreateTime(n.getCreateTime());

            if (n.getSenderId() != null && senderMap.containsKey(n.getSenderId())) {
                SysUser sender = senderMap.get(n.getSenderId());
                vo.setSenderNickname(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        // 使用Redis缓存，减少数据库查询
        String cacheKey = UNREAD_COUNT_CACHE_KEY + userId;
        Object cached = redisUtil.get(cacheKey);
        
        if (cached != null) {
            return Integer.valueOf(String.valueOf(cached));
        }
        
        Integer count = notificationMapper.countUnread(userId);
        redisUtil.setEx(cacheKey, count, UNREAD_COUNT_CACHE_TTL, TimeUnit.SECONDS);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        if (notificationId == null) {
            // 标记全部已读
            notificationMapper.markAllAsRead(userId);
        } else {
            // 标记单条已读
            notificationMapper.markAsRead(notificationId, userId);
        }
        
        // 清除未读计数缓存
        String cacheKey = UNREAD_COUNT_CACHE_KEY + userId;
        redisUtil.delete(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String type, String title, String content, String relatedId, Long senderId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(relatedId);
            notification.setSenderId(senderId);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            
            // 清除未读计数缓存
            String cacheKey = UNREAD_COUNT_CACHE_KEY + userId;
            redisUtil.delete(cacheKey);
            
            // 实时推送WebSocket通知
            pushWebSocketNotification(userId, type, title, content);
        } catch (Exception e) {
            System.err.println("发送通知失败: userId=" + userId + ", error=" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendBatchNotification(List<Long> userIds, String type, String title, String content, String relatedId, Long senderId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        try {
            for (Long userId : userIds) {
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setType(type);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setRelatedId(relatedId);
                notification.setSenderId(senderId);
                notification.setIsRead(0);
                notification.setCreateTime(now);
                notificationMapper.insert(notification);
                
                // 清除未读计数缓存
                String cacheKey = UNREAD_COUNT_CACHE_KEY + userId;
                redisUtil.delete(cacheKey);
                
                // 实时推送WebSocket通知
                pushWebSocketNotification(userId, type, title, content);
            }
            System.out.println("批量发送通知成功: type=" + type + ", userCount=" + userIds.size());
        } catch (Exception e) {
            System.err.println("批量发送通知失败: type=" + type + ", userIds=" + userIds + ", error=" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 通过WebSocket推送实时通知
     */
    private void pushWebSocketNotification(Long userId, String type, String title, String content) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "notification");
            message.put("notificationType", type);
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            
            WebSocketUtil.sendMessage(userId, message.toJSONString());
        } catch (Exception e) {
            // WebSocket推送失败不影响通知创建
            System.err.println("WebSocket推送通知失败: userId=" + userId + ", error=" + e.getMessage());
        }
    }
}
