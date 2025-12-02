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
    private String status;  // 事件状态
    private Long ownerId;   // 发起者ID
    private String ownerNickname;  // 发起者昵称
    private String ownerAvatar;    // 发起者头像
}