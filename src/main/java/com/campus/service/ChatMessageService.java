package com.campus.service;

import com.campus.entity.ChatMessage;
import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService {
    
    /**
     * 保存聊天消息
     */
    void saveMessage(ChatMessage message);
    
    /**
     * 获取事件的聊天记录
     */
    List<ChatMessage> getEventMessages(String eventId);
}
