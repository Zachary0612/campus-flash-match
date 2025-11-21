package com.campus.vo;

import lombok.Data;

@Data
public class CreditRecordVO {
    private Long recordId;
    private String eventId;
    private Integer changeScore;
    private String reason;
    private Long createTime;
}