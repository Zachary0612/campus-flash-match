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
    
    // 新增字段：用户角色和发起者信息
    private Boolean isOwner;           // 是否为发起者
    private Long ownerId;              // 发起者ID
    private String ownerNickname;      // 发起者昵称
    private String ownerAvatar;        // 发起者头像
    private List<EventParticipantVO> participantList;  // 参与者列表
    private Double longitude;          // 事件经度
    private Double latitude;           // 事件纬度
    private String locationName;       // 地点名称
}