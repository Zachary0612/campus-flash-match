package com.campus.vo;

import lombok.Data;

/**
 * 用户统计数据VO
 */
@Data
public class UserStatisticsVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 发起事件总数
     */
    private Integer createdEventCount;
    
    /**
     * 参与事件总数
     */
    private Integer joinedEventCount;
    
    /**
     * 成功完成事件数
     */
    private Integer completedEventCount;
    
    /**
     * 关注数
     */
    private Integer followingCount;
    
    /**
     * 粉丝数
     */
    private Integer followerCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
    
    /**
     * 收到评价数
     */
    private Integer ratingCount;
    
    /**
     * 平均评分
     */
    private Double averageRating;
    
    /**
     * 信用分
     */
    private Integer creditScore;
    
    /**
     * 本月发起事件数
     */
    private Integer monthlyCreatedCount;
    
    /**
     * 本月参与事件数
     */
    private Integer monthlyJoinedCount;
}
