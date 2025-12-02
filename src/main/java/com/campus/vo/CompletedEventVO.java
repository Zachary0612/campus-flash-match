package com.campus.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CompletedEventVO {
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
    private List<EventParticipantVO> participants;
    
    // 新增字段
    private Boolean isOwner;           // 当前用户是否为发起者
    private String ownerNickname;      // 发起者昵称
    private String ownerAvatar;        // 发起者头像
}
