package com.campus.service;

import com.campus.dto.request.UserProfileUpdateDTO;
import com.campus.vo.UserProfileVO;
import com.campus.vo.UserStatisticsVO;

/**
 * 用户个人资料Service接口
 */
public interface ProfileService {

    /**
     * 获取用户个人资料
     * @param userId 用户ID
     * @param viewerId 查看者ID（用于判断是否已关注）
     * @return 用户资料
     */
    UserProfileVO getUserProfile(Long userId, Long viewerId);

    /**
     * 更新用户个人资料
     * @param userId 用户ID
     * @param dto 更新内容
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 统计数据
     */
    UserStatisticsVO getUserStatistics(Long userId);
}
