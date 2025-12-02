package com.campus.service;

import com.campus.dto.request.EventRatingDTO;
import com.campus.vo.EventRatingVO;
import java.util.List;

/**
 * 事件评价Service接口
 */
public interface RatingService {

    /**
     * 提交评价
     * @param raterId 评价者ID
     * @param dto 评价内容
     */
    void submitRating(Long raterId, EventRatingDTO dto);

    /**
     * 获取用户收到的评价列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 评价列表
     */
    List<EventRatingVO> getReceivedRatings(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户发出的评价列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 评价列表
     */
    List<EventRatingVO> getGivenRatings(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取事件的评价列表
     * @param eventId 事件ID
     * @return 评价列表
     */
    List<EventRatingVO> getEventRatings(String eventId);

    /**
     * 获取用户平均评分
     * @param userId 用户ID
     * @return 平均评分
     */
    Double getAverageRating(Long userId);

    /**
     * 检查是否可以评价
     * @param eventId 事件ID
     * @param raterId 评价者ID
     * @param ratedUserId 被评价者ID
     * @return 是否可以评价
     */
    boolean canRate(String eventId, Long raterId, Long ratedUserId);
}
