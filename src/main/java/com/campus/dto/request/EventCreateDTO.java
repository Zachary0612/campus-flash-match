package com.campus.dto.request;

import lombok.Data;

/**
 * 事件创建请求DTO
 */
@Data
public class EventCreateDTO {
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件类型（group_buy/meetup/beacon）
     */
    private String eventType;
    
    /**
     * 目标人数
     */
    private Integer targetNum;
    
    /**
     * 过期时间（分钟）
     */
    private Integer expireMinutes;
    
    /**
     * 校园点位ID
     */
    private Long pointId;
    
    /**
     * 扩展元数据
     */
    private java.util.Map<String, Object> extMeta;
}