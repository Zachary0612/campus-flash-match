package com.campus.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SettlementMsgDTO {
    private String eventId;
    private String status;
    private Date sendTime;
}