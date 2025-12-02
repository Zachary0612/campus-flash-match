package com.campus.dto.request;

import lombok.Data;

/**
 * 用户资料更新请求DTO
 */
@Data
public class UserProfileUpdateDTO {
    /**
     * 昵称
     */
    private String nickname;
    
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
     * 兴趣标签（逗号分隔）
     */
    private String interests;
}
