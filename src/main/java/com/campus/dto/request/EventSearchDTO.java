package com.campus.dto.request;

import lombok.Data;

/**
 * 事件搜索请求DTO
 */
@Data
public class EventSearchDTO {
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 事件状态
     */
    private String status;
    
    /**
     * 发起者ID
     */
    private Long ownerId;
    
    /**
     * 最小目标人数
     */
    private Integer minTargetNum;
    
    /**
     * 最大目标人数
     */
    private Integer maxTargetNum;
    
    /**
     * 开始时间（创建时间范围）
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段（create_time/target_num/current_num）
     */
    private String sortBy = "create_time";
    
    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}
