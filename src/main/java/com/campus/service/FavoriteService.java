package com.campus.service;

import com.campus.vo.EventFavoriteVO;
import java.util.List;

/**
 * 事件收藏Service接口
 */
public interface FavoriteService {

    /**
     * 收藏事件
     * @param userId 用户ID
     * @param eventId 事件ID
     */
    void addFavorite(Long userId, String eventId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param eventId 事件ID
     */
    void removeFavorite(Long userId, String eventId);

    /**
     * 获取收藏列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 收藏列表
     */
    List<EventFavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param eventId 事件ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, String eventId);

    /**
     * 获取收藏数
     * @param userId 用户ID
     * @return 收藏数
     */
    Integer getFavoriteCount(Long userId);
}
