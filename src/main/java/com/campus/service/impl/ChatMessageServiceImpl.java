package com.campus.service.impl;

import com.campus.entity.ChatMessage;
import com.campus.mapper.ChatMessageMapper;
import com.campus.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 聊天消息服务实现
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Override
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }
    
    @Override
    public List<ChatMessage> getEventMessages(String eventId) {
        List<ChatMessage> messages = chatMessageMapper.getRecentMessages(eventId);
        // 反转顺序，让最早的消息在前面
        Collections.reverse(messages);
        return messages;
    }
}
