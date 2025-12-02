package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件详情VO
 */
@Data
public class EventDetailVO {
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 目标人数
     */
    private Integer targetNum;
    
    /**
     * 当前人数
     */
    private Integer currentNum;
    
    /**
     * 过期分钟数
     */
    private Integer expireMinutes;
    
    /**
     * 事件状态
     */
    private String status;
    
    /**
     * 发起者ID
     */
    private Long ownerId;
    
    /**
     * 发起者昵称
     */
    private String ownerNickname;
    
    /**
     * 发起者头像
     */
    private String ownerAvatar;
    
    /**
     * 发起者信用分
     */
    private Integer ownerCreditScore;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> extMeta;
    
    /**
     * 媒体URL列表
     */
    private List<String> mediaUrls;
    
    /**
     * 参与者列表
     */
    private List<EventParticipantVO> participants;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
    
    /**
     * 是否已收藏
     */
    private Boolean isFavorite;
    
    /**
     * 是否已参与
     */
    private Boolean isJoined;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 结算时间
     */
    private LocalDateTime settleTime;
    
    /**
     * 距离（米）
     */
    private Double distance;
}
