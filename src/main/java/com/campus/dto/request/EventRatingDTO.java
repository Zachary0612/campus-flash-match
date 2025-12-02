package com.campus.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 事件评价请求DTO
 */
@Data
public class EventRatingDTO {
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 被评价者ID
     */
    @JsonProperty("targetUserId")
    private Long ratedUserId;
    
    /**
     * 评分（1-5星）
     */
    private Integer score;
    
    /**
     * 评价内容
     */
    private String comment;
    
    /**
     * 评价标签（逗号分隔，如：准时,友好,靠谱）
     */
    private String tags;
}
