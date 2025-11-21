package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventHistoryVO {
    private String eventId;
    private String eventType;
    private String title;
    private Integer targetNum;
    private Integer currentNum;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime settleTime;
}