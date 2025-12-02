package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 事件评价VO
 */
@Data
public class EventRatingVO {
    /**
     * 评价ID
     */
    private Long id;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件标题
     */
    private String eventTitle;
    
    /**
     * 评价者ID
     */
    private Long raterId;
    
    /**
     * 评价者昵称
     */
    private String raterNickname;
    
    /**
     * 评价者头像
     */
    private String raterAvatar;
    
    /**
     * 被评价者ID
     */
    private Long ratedUserId;
    
    /**
     * 被评价者昵称
     */
    private String ratedUserNickname;
    
    /**
     * 评分
     */
    private Integer score;
    
    /**
     * 评价内容
     */
    private String comment;
    
    /**
     * 评价标签
     */
    private String tags;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
