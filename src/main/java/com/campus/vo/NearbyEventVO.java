package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NearbyEventVO {
    private String eventId;
    private String eventType;
    private String title;
    private Integer distance;
    private Integer currentNum;
    private Integer targetNum;
    private Integer expireMinutes;
    private LocalDateTime createTime;
    private String description;
    private List<String> mediaUrls;
}