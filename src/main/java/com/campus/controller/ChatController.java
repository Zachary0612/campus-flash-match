package com.campus.controller;

import com.campus.dto.ResultDTO;
import com.campus.entity.ChatMessage;
import com.campus.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    /**
     * 获取事件的聊天记录
     */
    @GetMapping("/messages/{eventId}")
    public ResultDTO<List<ChatMessage>> getMessages(@PathVariable String eventId) {
        List<ChatMessage> messages = chatMessageService.getEventMessages(eventId);
        return ResultDTO.success("获取成功", messages);
    }
}
