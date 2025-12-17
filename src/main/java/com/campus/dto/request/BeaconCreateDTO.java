package com.campus.dto.request;

import lombok.Data;
import java.util.List;

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
    
    /**
     * 地图选点位置（可选，与pointId二选一）
     */
    private java.util.Map<String, Object> mapLocation;
    
    /**
     * 图片/视频URL列表（可选）
     */
    private List<String> mediaUrls;
    
    /**
     * 详细说明（可选）
     */
    private String description;
}