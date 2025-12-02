package com.campus.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.dto.ResultDTO;
import com.campus.exception.BusinessException;
import com.campus.service.FollowService;
import com.campus.vo.UserFollowVO;

/**
 * 用户关注Controller
 */
@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * 关注用户
     */
    @PostMapping("/{userId}")
    public ResultDTO<Void> follow(@PathVariable Long userId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long followerId = Long.valueOf(principal.getName());
        followService.follow(followerId, userId);
        return ResultDTO.success("关注成功");
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    public ResultDTO<Void> unfollow(@PathVariable Long userId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long followerId = Long.valueOf(principal.getName());
        followService.unfollow(followerId, userId);
        return ResultDTO.success("取消关注成功");
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/following")
    public ResultDTO<List<UserFollowVO>> getFollowingList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<UserFollowVO> list = followService.getFollowingList(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", list);
    }

    /**
     * 获取指定用户的关注列表
     */
    @GetMapping("/following/{userId}")
    public ResultDTO<List<UserFollowVO>> getUserFollowingList(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        List<UserFollowVO> list = followService.getFollowingList(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", list);
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/followers")
    public ResultDTO<List<UserFollowVO>> getFollowerList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<UserFollowVO> list = followService.getFollowerList(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", list);
    }

    /**
     * 获取指定用户的粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public ResultDTO<List<UserFollowVO>> getUserFollowerList(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        List<UserFollowVO> list = followService.getFollowerList(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", list);
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/check/{userId}")
    public ResultDTO<Boolean> checkFollow(@PathVariable Long userId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long followerId = Long.valueOf(principal.getName());
        boolean isFollowing = followService.isFollowing(followerId, userId);
        return ResultDTO.success("查询成功", isFollowing);
    }
}
