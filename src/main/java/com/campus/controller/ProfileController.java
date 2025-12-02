package com.campus.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.UserProfileUpdateDTO;
import com.campus.exception.BusinessException;
import com.campus.service.ProfileService;
import com.campus.service.storage.FileStorageService;
import com.campus.vo.UserProfileVO;
import com.campus.vo.UserStatisticsVO;

/**
 * 用户个人资料Controller
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 获取当前用户个人资料
     */
    @GetMapping("/me")
    public ResultDTO<UserProfileVO> getMyProfile(Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        UserProfileVO profile = profileService.getUserProfile(userId, userId);
        return ResultDTO.success("查询成功", profile);
    }

    /**
     * 获取指定用户个人资料（他人主页）
     */
    @GetMapping("/{userId}")
    public ResultDTO<UserProfileVO> getUserProfile(@PathVariable Long userId, Principal principal) {
        Long viewerId = null;
        if (principal != null) {
            viewerId = Long.valueOf(principal.getName());
        }
        UserProfileVO profile = profileService.getUserProfile(userId, viewerId);
        return ResultDTO.success("查询成功", profile);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/update")
    public ResultDTO<Void> updateProfile(@RequestBody UserProfileUpdateDTO dto, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        profileService.updateProfile(userId, dto);
        return ResultDTO.success("更新成功");
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResultDTO<String> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        
        // 上传文件
        String avatarUrl = fileStorageService.uploadFile(file, "avatar");
        
        // 更新用户头像
        profileService.updateAvatar(userId, avatarUrl);
        
        return ResultDTO.success("头像上传成功", avatarUrl);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/statistics")
    public ResultDTO<UserStatisticsVO> getStatistics(Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        UserStatisticsVO statistics = profileService.getUserStatistics(userId);
        return ResultDTO.success("查询成功", statistics);
    }

    /**
     * 获取指定用户统计数据
     */
    @GetMapping("/statistics/{userId}")
    public ResultDTO<UserStatisticsVO> getUserStatistics(@PathVariable Long userId) {
        UserStatisticsVO statistics = profileService.getUserStatistics(userId);
        return ResultDTO.success("查询成功", statistics);
    }
}
