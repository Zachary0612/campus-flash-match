package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.dto.request.UserProfileUpdateDTO;
import com.campus.entity.SysUser;
import com.campus.exception.BusinessException;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.mapper.UserFollowMapper;
import com.campus.mapper.EventRatingMapper;
import com.campus.mapper.EventFavoriteMapper;
import com.campus.service.ProfileService;
import com.campus.service.FollowService;
import com.campus.vo.UserProfileVO;
import com.campus.vo.UserStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户个人资料Service实现类
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EventHistoryMapper eventHistoryMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private EventRatingMapper eventRatingMapper;

    @Autowired
    private EventFavoriteMapper eventFavoriteMapper;

    @Autowired
    private FollowService followService;

    @Override
    public UserProfileVO getUserProfile(Long userId, Long viewerId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setStudentId(maskStudentId(user.getStudentId()));
        vo.setNickname(user.getNickname());
        vo.setEmail(maskEmail(user.getEmail()));
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setGender(user.getGender());
        vo.setMajor(user.getMajor());
        vo.setGrade(user.getGrade());
        vo.setInterests(user.getInterests());
        vo.setCreditScore(user.getCreditScore());
        vo.setCreateTime(user.getCreateTime());

        // 统计数据
        vo.setCreatedEventCount(eventHistoryMapper.countCreatedEvents(userId));
        vo.setJoinedEventCount(eventHistoryMapper.countJoinedEvents(userId));
        vo.setFollowingCount(userFollowMapper.countFollowing(userId));
        vo.setFollowerCount(userFollowMapper.countFollowers(userId));

        // 是否已关注（查看他人主页时）
        if (viewerId != null && !viewerId.equals(userId)) {
            vo.setIsFollowed(followService.isFollowing(viewerId, userId));
        } else {
            vo.setIsFollowed(false);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getMajor() != null) {
            user.setMajor(dto.getMajor());
        }
        if (dto.getGrade() != null) {
            user.setGrade(dto.getGrade());
        }
        if (dto.getInterests() != null) {
            user.setInterests(dto.getInterests());
        }

        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public UserStatisticsVO getUserStatistics(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserStatisticsVO vo = new UserStatisticsVO();
        vo.setUserId(userId);
        vo.setCreatedEventCount(eventHistoryMapper.countCreatedEvents(userId));
        vo.setJoinedEventCount(eventHistoryMapper.countJoinedEvents(userId));
        vo.setCompletedEventCount(eventHistoryMapper.countCompletedEvents(userId));
        vo.setFollowingCount(userFollowMapper.countFollowing(userId));
        vo.setFollowerCount(userFollowMapper.countFollowers(userId));
        vo.setFavoriteCount(eventFavoriteMapper.countFavorites(userId));
        vo.setRatingCount(eventRatingMapper.countRatings(userId));
        vo.setAverageRating(eventRatingMapper.getAverageRating(userId));
        vo.setCreditScore(user.getCreditScore());
        vo.setMonthlyCreatedCount(eventHistoryMapper.countMonthlyCreatedEvents(userId));
        vo.setMonthlyJoinedCount(eventHistoryMapper.countMonthlyJoinedEvents(userId));

        return vo;
    }

    /**
     * 脱敏学号（显示前4位和后2位）
     */
    private String maskStudentId(String studentId) {
        if (studentId == null || studentId.length() <= 6) {
            return studentId;
        }
        return studentId.substring(0, 4) + "****" + studentId.substring(studentId.length() - 2);
    }

    /**
     * 脱敏邮箱
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }
}
