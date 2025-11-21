package com.campus.dto.request;

import lombok.Data;

/**
 * 信标创建请求DTO
 */
@Data
public class BeaconCreateDTO {
    /**
     * 信标位置描述（必填）
     */
    private String locationDesc;
    
    /**
     * 过期时间（分钟）
     */
    private Integer expireMinutes;
    
    /**
     * 校园点位ID
     */
    private Long pointId;
}