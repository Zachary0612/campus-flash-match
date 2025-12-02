package com.campus.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注VO
 */
@Data
public class UserFollowVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 信用分
     */
    private Integer creditScore;
    
    /**
     * 是否互相关注
     */
    private Boolean isMutual;
    
    /**
     * 关注时间
     */
    private LocalDateTime followTime;
}
