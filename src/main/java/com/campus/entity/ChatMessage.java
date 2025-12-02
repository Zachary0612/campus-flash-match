package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String eventId;
    
    private Long userId;
    
    private String nickname;
    
    private String avatar;
    
    private String content;
    
    private LocalDateTime createTime;
}
