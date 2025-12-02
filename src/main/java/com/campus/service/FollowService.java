package com.campus.service;

import com.campus.vo.UserFollowVO;
import java.util.List;

/**
 * 用户关注Service接口
 */
public interface FollowService {

    /**
     * 关注用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 获取关注列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 关注列表
     */
    List<UserFollowVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取粉丝列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 粉丝列表
     */
    List<UserFollowVO> getFollowerList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 检查是否已关注
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取关注数
     * @param userId 用户ID
     * @return 关注数
     */
    Integer getFollowingCount(Long userId);

    /**
     * 获取粉丝数
     * @param userId 用户ID
     * @return 粉丝数
     */
    Integer getFollowerCount(Long userId);
}
