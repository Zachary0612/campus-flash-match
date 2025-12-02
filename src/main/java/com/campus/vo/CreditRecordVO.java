package com.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreditRecordVO {
    private Long recordId;
    private String eventId;
    private Integer changeScore;
    private String reason;
    private LocalDateTime createTime;
}