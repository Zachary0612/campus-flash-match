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
import com.campus.service.FavoriteService;
import com.campus.vo.EventFavoriteVO;

/**
 * 事件收藏Controller
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 收藏事件
     */
    @PostMapping("/{eventId}")
    public ResultDTO<Void> addFavorite(@PathVariable String eventId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        favoriteService.addFavorite(userId, eventId);
        return ResultDTO.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{eventId}")
    public ResultDTO<Void> removeFavorite(@PathVariable String eventId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        favoriteService.removeFavorite(userId, eventId);
        return ResultDTO.success("取消收藏成功");
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public ResultDTO<List<EventFavoriteVO>> getFavoriteList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<EventFavoriteVO> favorites = favoriteService.getFavoriteList(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", favorites);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{eventId}")
    public ResultDTO<Boolean> checkFavorite(@PathVariable String eventId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        boolean isFavorite = favoriteService.isFavorite(userId, eventId);
        return ResultDTO.success("查询成功", isFavorite);
    }
}
