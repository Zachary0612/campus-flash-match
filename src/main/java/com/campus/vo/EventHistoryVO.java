package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventHistoryVO {
    private String eventId;
    private String eventType;
    private String title;
    private Integer targetNum;
    private Integer currentNum;
    private Integer expireMinutes;
    private String status;
    private String extMeta;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private LocalDateTime settleTime;
    private String description;
    private List<String> mediaUrls;
}