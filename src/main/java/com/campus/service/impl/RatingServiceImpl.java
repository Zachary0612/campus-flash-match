package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.constant.EventConstant;
import com.campus.dto.request.EventRatingDTO;
import com.campus.entity.EventHistory;
import com.campus.entity.EventRating;
import com.campus.entity.SysUser;
import com.campus.exception.BusinessException;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.EventRatingMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.service.NotificationService;
import com.campus.service.RatingService;
import com.campus.vo.EventRatingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事件评价Service实现类
 */
@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private EventRatingMapper eventRatingMapper;

    @Autowired
    private EventHistoryMapper eventHistoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long raterId, EventRatingDTO dto) {
        // 校验评分范围
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        // 检查是否可以评价
        if (!canRate(dto.getEventId(), raterId, dto.getRatedUserId())) {
            throw new BusinessException("无法评价该用户");
        }

        // 检查是否已评价
        Integer rated = eventRatingMapper.checkRated(dto.getEventId(), raterId, dto.getRatedUserId());
        if (rated != null && rated > 0) {
            throw new BusinessException("已经评价过该用户");
        }

        EventRating rating = new EventRating();
        rating.setEventId(dto.getEventId());
        rating.setRaterId(raterId);
        rating.setRatedUserId(dto.getRatedUserId());
        rating.setScore(dto.getScore());
        rating.setComment(dto.getComment());
        rating.setTags(dto.getTags());
        rating.setCreateTime(LocalDateTime.now());
        eventRatingMapper.insert(rating);

        // 发送通知
        SysUser rater = sysUserMapper.selectById(raterId);
        String content = rater.getNickname() + " 给你的评价：" + dto.getScore() + "星";
        notificationService.sendNotification(dto.getRatedUserId(), "rating", "收到新评价", content, dto.getEventId(), raterId);
    }

    @Override
    public List<EventRatingVO> getReceivedRatings(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<EventRating> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rated_user_id", userId)
                .orderByDesc("create_time");

        Page<EventRating> page = new Page<>(pageNum, pageSize);
        Page<EventRating> result = eventRatingMapper.selectPage(page, queryWrapper);

        return convertToVOList(result.getRecords());
    }

    @Override
    public List<EventRatingVO> getGivenRatings(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<EventRating> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rater_id", userId)
                .orderByDesc("create_time");

        Page<EventRating> page = new Page<>(pageNum, pageSize);
        Page<EventRating> result = eventRatingMapper.selectPage(page, queryWrapper);

        return convertToVOList(result.getRecords());
    }

    @Override
    public List<EventRatingVO> getEventRatings(String eventId) {
        QueryWrapper<EventRating> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("event_id", eventId)
                .orderByDesc("create_time");

        List<EventRating> ratings = eventRatingMapper.selectList(queryWrapper);
        return convertToVOList(ratings);
    }

    @Override
    public Double getAverageRating(Long userId) {
        return eventRatingMapper.getAverageRating(userId);
    }

    @Override
    public boolean canRate(String eventId, Long raterId, Long ratedUserId) {
        // 不能评价自己
        if (raterId.equals(ratedUserId)) {
            return false;
        }

        // 检查事件是否已完成
        QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("event_id", eventId)
                .eq("status", EventConstant.EVENT_STATUS_COMPLETED);
        EventHistory event = eventHistoryMapper.selectOne(queryWrapper);
        if (event == null) {
            return false;
        }

        // 检查评价者是否参与了该事件
        String participants = event.getParticipants();
        if (participants == null || !participants.contains(raterId.toString())) {
            // 如果评价者是发起者也可以
            if (!event.getUserId().equals(raterId)) {
                return false;
            }
        }

        // 检查被评价者是否参与了该事件
        if (participants == null || !participants.contains(ratedUserId.toString())) {
            if (!event.getUserId().equals(ratedUserId)) {
                return false;
            }
        }

        return true;
    }

    private List<EventRatingVO> convertToVOList(List<EventRating> ratings) {
        if (ratings.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有相关用户ID
        List<Long> userIds = new ArrayList<>();
        for (EventRating r : ratings) {
            userIds.add(r.getRaterId());
            userIds.add(r.getRatedUserId());
        }
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 获取事件信息
        List<String> eventIds = ratings.stream()
                .map(EventRating::getEventId)
                .distinct()
                .collect(Collectors.toList());

        QueryWrapper<EventHistory> eventQuery = new QueryWrapper<>();
        eventQuery.in("event_id", eventIds);
        List<EventHistory> events = eventHistoryMapper.selectList(eventQuery);
        Map<String, EventHistory> eventMap = events.stream()
                .collect(Collectors.toMap(EventHistory::getEventId, e -> e, (e1, e2) -> e1));

        List<EventRatingVO> voList = new ArrayList<>();
        for (EventRating r : ratings) {
            EventRatingVO vo = new EventRatingVO();
            vo.setId(r.getId());
            vo.setEventId(r.getEventId());
            vo.setRaterId(r.getRaterId());
            vo.setRatedUserId(r.getRatedUserId());
            vo.setScore(r.getScore());
            vo.setComment(r.getComment());
            vo.setTags(r.getTags());
            vo.setCreateTime(r.getCreateTime());

            SysUser rater = userMap.get(r.getRaterId());
            if (rater != null) {
                vo.setRaterNickname(rater.getNickname());
                vo.setRaterAvatar(rater.getAvatar());
            }

            SysUser ratedUser = userMap.get(r.getRatedUserId());
            if (ratedUser != null) {
                vo.setRatedUserNickname(ratedUser.getNickname());
            }

            EventHistory event = eventMap.get(r.getEventId());
            if (event != null) {
                vo.setEventTitle(event.getTitle());
            }

            voList.add(vo);
        }

        return voList;
    }
}
