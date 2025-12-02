package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventParticipantVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String status;
    private boolean owner;
    private LocalDateTime joinTime;
    private LocalDateTime settleTime;
}
