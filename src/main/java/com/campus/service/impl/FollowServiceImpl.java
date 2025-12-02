package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.SysUser;
import com.campus.entity.UserFollow;
import com.campus.exception.BusinessException;
import com.campus.mapper.SysUserMapper;
import com.campus.mapper.UserFollowMapper;
import com.campus.service.FollowService;
import com.campus.service.NotificationService;
import com.campus.vo.UserFollowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户关注Service实现类
 */
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        // 检查被关注者是否存在
        SysUser following = sysUserMapper.selectById(followingId);
        if (following == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否已关注
        if (isFollowing(followerId, followingId)) {
            throw new BusinessException("已经关注过了");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollow.setCreateTime(LocalDateTime.now());
        userFollowMapper.insert(userFollow);

        // 发送通知
        SysUser follower = sysUserMapper.selectById(followerId);
        String content = follower.getNickname() + " 关注了你";
        notificationService.sendNotification(followingId, "follow", "新增关注", content, null, followerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followingId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", followerId)
                .eq("following_id", followingId);
        userFollowMapper.delete(queryWrapper);
    }

    @Override
    public List<UserFollowVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId)
                .orderByDesc("create_time");

        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        Page<UserFollow> result = userFollowMapper.selectPage(page, queryWrapper);

        List<UserFollow> follows = result.getRecords();
        if (follows.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取被关注者信息
        List<Long> followingIds = follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserMapper.selectBatchIds(followingIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        List<UserFollowVO> voList = new ArrayList<>();
        for (UserFollow f : follows) {
            UserFollowVO vo = new UserFollowVO();
            vo.setUserId(f.getFollowingId());
            vo.setFollowTime(f.getCreateTime());

            SysUser user = userMap.get(f.getFollowingId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setBio(user.getBio());
                vo.setCreditScore(user.getCreditScore());
            }

            // 检查是否互相关注
            vo.setIsMutual(isFollowing(f.getFollowingId(), userId));

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<UserFollowVO> getFollowerList(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("following_id", userId)
                .orderByDesc("create_time");

        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        Page<UserFollow> result = userFollowMapper.selectPage(page, queryWrapper);

        List<UserFollow> follows = result.getRecords();
        if (follows.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取粉丝信息
        List<Long> followerIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserMapper.selectBatchIds(followerIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        List<UserFollowVO> voList = new ArrayList<>();
        for (UserFollow f : follows) {
            UserFollowVO vo = new UserFollowVO();
            vo.setUserId(f.getFollowerId());
            vo.setFollowTime(f.getCreateTime());

            SysUser user = userMap.get(f.getFollowerId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setBio(user.getBio());
                vo.setCreditScore(user.getCreditScore());
            }

            // 检查是否互相关注
            vo.setIsMutual(isFollowing(userId, f.getFollowerId()));

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        Integer count = userFollowMapper.checkFollow(followerId, followingId);
        return count != null && count > 0;
    }

    @Override
    public Integer getFollowingCount(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    @Override
    public Integer getFollowerCount(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }
}
