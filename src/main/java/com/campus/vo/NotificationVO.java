package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知消息VO
 */
@Data
public class NotificationVO {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型
     */
    private String type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联ID
     */
    private String relatedId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者昵称
     */
    private String senderNickname;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
