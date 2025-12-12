package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户个人资料VO
 */
@Data
public class UserProfileVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学号
     */
    private String studentId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 年级
     */
    private String grade;
    
    /**
     * 兴趣标签
     */
    private String interests;
    
    /**
     * 信用分
     */
    private Integer creditScore;
    
    /**
     * 发起事件数
     */
    private Integer createdEventCount;
    
    /**
     * 参与事件数
     */
    private Integer joinedEventCount;
    
    /**
     * 关注数
     */
    private Integer followingCount;
    
    /**
     * 粉丝数
     */
    private Integer followerCount;
    
    /**
     * 注册时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否已关注（查看他人主页时使用）
     */
    private Boolean isFollowed;
    
    /**
     * 扩展元数据（JSON格式，包含地图位置等信息）
     */
    private java.util.Map<String, Object> extMeta;
}
