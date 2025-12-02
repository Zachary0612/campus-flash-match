package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件评论VO
 */
@Data
public class EventCommentVO {
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 评论者ID
     */
    private Long userId;
    
    /**
     * 评论者昵称
     */
    private String nickname;
    
    /**
     * 评论者头像
     */
    private String avatar;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 回复目标用户ID
     */
    private Long replyToUserId;
    
    /**
     * 回复目标用户昵称
     */
    private String replyToNickname;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 子评论列表（回复）
     */
    private List<EventCommentVO> replies;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
