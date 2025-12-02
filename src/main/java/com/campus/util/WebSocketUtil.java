package com.campus.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * WebSocket工具类，处理实时消息推送
 */
@Component
public class WebSocketUtil extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WebSocketUtil.class.getName());
    
    // 用户ID到WebSocket会话的映射
    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 事件ID到订阅用户集合的映射，用于事件广播
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Long>> eventSubscribers = new ConcurrentHashMap<>();
    
    /**
     * 建立连接时调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从握手属性中获取用户ID
            Map<String, Object> attributes = session.getAttributes();
            Long userId = (Long) attributes.get("userId");
            
            if (userId == null) {
                // 如果握手属性中没有用户ID，尝试从URL路径获取（兼容旧版本）
                String uri = session.getUri().getPath();
                String[] parts = uri.split("/");
                userId = Long.parseLong(parts[parts.length - 1]);
                attributes.put("userId", userId);
            }
            
            addSession(userId, session);
            logger.info("用户 [" + userId + "] 建立WebSocket连接成功");
            
            // 发送连接成功消息
            sendMessage(userId, createMessage("connected", "WebSocket连接成功"));
        } catch (Exception e) {
            logger.warning("建立WebSocket连接失败: " + e.getMessage());
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    
    /**
     * 处理收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            
            // 解析消息内容
            JSONObject json = JSONObject.parseObject(payload);
            String type = json.getString("type");
            
            // 根据消息类型处理
            switch (type) {
                case "subscribe_event":
                    // 订阅事件更新
                    String eventId = json.getString("eventId");
                    Long userId = getUserIdFromSession(session);
                    subscribeEvent(userId, eventId);
                    logger.info("用户 [" + userId + "] 订阅事件: " + eventId);
                    break;
                case "ping":
                    // 心跳包响应（不记录日志，避免刷屏）
                    session.sendMessage(new TextMessage(createMessage("pong", null)));
                    break;
                default:
                    logger.warning("未知的消息类型: " + type);
            }
        } catch (Exception e) {
            logger.warning("处理WebSocket消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 连接关闭时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId != null) {
                removeSession(userId);
                
                // 移除用户的所有事件订阅
                for (Map.Entry<String, CopyOnWriteArraySet<Long>> entry : eventSubscribers.entrySet()) {
                    entry.getValue().remove(userId);
                }
                
                logger.info("用户 [" + userId + "] 关闭WebSocket连接");
            }
        } catch (Exception e) {
            logger.warning("关闭WebSocket连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.warning("WebSocket传输错误: " + exception.getMessage());
        try {
            // 尝试获取用户ID并清理会话
            Long userId = getUserIdFromSession(session);
            if (userId != null) {
                removeSession(userId);
            }
        } catch (Exception e) {
            // 忽略清理时的异常
        }
    }
    
    /**
     * 添加WebSocket会话
     */
    public static void addSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }
    
    /**
     * 移除WebSocket会话
     */
    public static void removeSession(Long userId) {
        sessions.remove(userId);
    }
    
    /**
     * 发送消息给指定用户
     */
    public static boolean sendMessage(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                logger.warning("发送消息给用户 [" + userId + "] 失败: " + e.getMessage());
                // 失败时清理会话
                removeSession(userId);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 广播消息给事件的所有订阅者
     */
    public static void broadcastToEvent(String eventId, String message) {
        CopyOnWriteArraySet<Long> subscribers = eventSubscribers.get(eventId);
        if (subscribers != null) {
            for (Long userId : subscribers) {
                sendMessage(userId, message);
            }
        }
    }
    
    /**
     * 用户订阅事件更新
     */
    public static void subscribeEvent(Long userId, String eventId) {
        eventSubscribers.computeIfAbsent(eventId, k -> new CopyOnWriteArraySet<>()).add(userId);
        logger.info("用户 [" + userId + "] 订阅事件 [" + eventId + "]");
    }
    
    /**
     * 用户取消订阅事件
     */
    public static void unsubscribeEvent(Long userId, String eventId) {
        CopyOnWriteArraySet<Long> subscribers = eventSubscribers.get(eventId);
        if (subscribers != null) {
            subscribers.remove(userId);
            logger.info("用户 [" + userId + "] 取消订阅事件 [" + eventId + "]");
        }
    }
    
    /**
     * 从会话中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 优先从握手属性中获取用户ID
            Map<String, Object> attributes = session.getAttributes();
            Long userId = (Long) attributes.get("userId");
            if (userId != null) {
                return userId;
            }
            return null;
        } catch (Exception e) {
            logger.warning("无法从会话中获取用户ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建标准格式的消息
     */
    private String createMessage(String type, Object data) {
        JSONObject message = new JSONObject();
        message.put("type", type);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);
        return message.toJSONString();
    }
    
    /**
     * 获取在线用户数量
     */
    public static int getOnlineCount() {
        return sessions.size();
    }
}