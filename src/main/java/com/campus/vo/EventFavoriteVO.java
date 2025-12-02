package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 事件收藏VO
 */
@Data
public class EventFavoriteVO {
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 事件状态
     */
    private String status;
    
    /**
     * 目标人数
     */
    private Integer targetNum;
    
    /**
     * 当前人数
     */
    private Integer currentNum;
    
    /**
     * 发起者昵称
     */
    private String ownerNickname;
    
    /**
     * 收藏时间
     */
    private LocalDateTime favoriteTime;
    
    /**
     * 事件创建时间
     */
    private LocalDateTime eventCreateTime;
}
