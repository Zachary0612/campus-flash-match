package com.campus.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.EventRatingDTO;
import com.campus.exception.BusinessException;
import com.campus.service.RatingService;
import com.campus.vo.EventRatingVO;

/**
 * 事件评价Controller
 */
@RestController
@RequestMapping("/api/rating")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * 提交评价
     */
    @PostMapping("/submit")
    public ResultDTO<Void> submitRating(@RequestBody EventRatingDTO dto, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long raterId = Long.valueOf(principal.getName());
        ratingService.submitRating(raterId, dto);
        return ResultDTO.success("评价成功");
    }

    /**
     * 获取收到的评价列表
     */
    @GetMapping("/received")
    public ResultDTO<List<EventRatingVO>> getReceivedRatings(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<EventRatingVO> ratings = ratingService.getReceivedRatings(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", ratings);
    }

    /**
     * 获取指定用户收到的评价
     */
    @GetMapping("/received/{userId}")
    public ResultDTO<List<EventRatingVO>> getUserReceivedRatings(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        List<EventRatingVO> ratings = ratingService.getReceivedRatings(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", ratings);
    }

    /**
     * 获取发出的评价列表
     */
    @GetMapping("/given")
    public ResultDTO<List<EventRatingVO>> getGivenRatings(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<EventRatingVO> ratings = ratingService.getGivenRatings(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", ratings);
    }

    /**
     * 获取事件的评价列表
     */
    @GetMapping("/event/{eventId}")
    public ResultDTO<List<EventRatingVO>> getEventRatings(@PathVariable String eventId) {
        List<EventRatingVO> ratings = ratingService.getEventRatings(eventId);
        return ResultDTO.success("查询成功", ratings);
    }

    /**
     * 获取用户平均评分
     */
    @GetMapping("/average/{userId}")
    public ResultDTO<Double> getAverageRating(@PathVariable Long userId) {
        Double average = ratingService.getAverageRating(userId);
        return ResultDTO.success("查询成功", average);
    }

    /**
     * 检查是否可以评价
     */
    @GetMapping("/can-rate")
    public ResultDTO<Boolean> canRate(
            @RequestParam String eventId,
            @RequestParam Long ratedUserId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long raterId = Long.valueOf(principal.getName());
        boolean canRate = ratingService.canRate(eventId, raterId, ratedUserId);
        return ResultDTO.success("查询成功", canRate);
    }
}
