package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NearbyEventVO {
    private String eventId;
    private String eventType;
    private String title;
    private Integer distance;
    private Integer currentNum;
    private Integer targetNum;
    private LocalDateTime createTime;
}