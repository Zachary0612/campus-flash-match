package com.campus.dto.request;

import lombok.Data;

/**
 * 事件评论请求DTO
 */
@Data
public class EventCommentDTO {
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID（回复时使用）
     */
    private Long parentId;
    
    /**
     * 回复目标用户ID（回复时使用）
     */
    private Long replyToUserId;
}
