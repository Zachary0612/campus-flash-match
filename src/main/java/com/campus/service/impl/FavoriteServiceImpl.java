package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.EventFavorite;
import com.campus.entity.EventHistory;
import com.campus.entity.SysUser;
import com.campus.exception.BusinessException;
import com.campus.mapper.EventFavoriteMapper;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.service.FavoriteService;
import com.campus.vo.EventFavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事件收藏Service实现类
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private EventFavoriteMapper eventFavoriteMapper;

    @Autowired
    private EventHistoryMapper eventHistoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, String eventId) {
        // 检查事件是否存在
        QueryWrapper<EventHistory> eventQuery = new QueryWrapper<>();
        eventQuery.eq("event_id", eventId);
        EventHistory event = eventHistoryMapper.selectOne(eventQuery);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        // 检查是否已收藏
        if (isFavorite(userId, eventId)) {
            throw new BusinessException("已经收藏过了");
        }

        EventFavorite favorite = new EventFavorite();
        favorite.setUserId(userId);
        favorite.setEventId(eventId);
        favorite.setCreateTime(LocalDateTime.now());
        eventFavoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, String eventId) {
        QueryWrapper<EventFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("event_id", eventId);
        eventFavoriteMapper.delete(queryWrapper);
    }

    @Override
    public List<EventFavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<EventFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        Page<EventFavorite> page = new Page<>(pageNum, pageSize);
        Page<EventFavorite> result = eventFavoriteMapper.selectPage(page, queryWrapper);

        List<EventFavorite> favorites = result.getRecords();
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取事件信息
        List<String> eventIds = favorites.stream()
                .map(EventFavorite::getEventId)
                .collect(Collectors.toList());

        QueryWrapper<EventHistory> eventQuery = new QueryWrapper<>();
        eventQuery.in("event_id", eventIds);
        List<EventHistory> events = eventHistoryMapper.selectList(eventQuery);
        Map<String, EventHistory> eventMap = events.stream()
                .collect(Collectors.toMap(EventHistory::getEventId, e -> e, (e1, e2) -> e1));

        // 获取发起者信息
        List<Long> ownerIds = events.stream()
                .map(EventHistory::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SysUser> userMap = new java.util.HashMap<>();
        if (!ownerIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(ownerIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        List<EventFavoriteVO> voList = new ArrayList<>();
        for (EventFavorite f : favorites) {
            EventFavoriteVO vo = new EventFavoriteVO();
            vo.setId(f.getId());
            vo.setEventId(f.getEventId());
            vo.setFavoriteTime(f.getCreateTime());

            EventHistory event = eventMap.get(f.getEventId());
            if (event != null) {
                vo.setTitle(event.getTitle());
                vo.setEventType(event.getEventType());
                vo.setStatus(event.getStatus());
                vo.setTargetNum(event.getTargetNum());
                vo.setCurrentNum(event.getCurrentNum());
                vo.setEventCreateTime(event.getCreateTime());

                SysUser owner = userMap.get(event.getUserId());
                if (owner != null) {
                    vo.setOwnerNickname(owner.getNickname());
                }
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public boolean isFavorite(Long userId, String eventId) {
        Integer count = eventFavoriteMapper.checkFavorite(userId, eventId);
        return count != null && count > 0;
    }

    @Override
    public Integer getFavoriteCount(Long userId) {
        return eventFavoriteMapper.countFavorites(userId);
    }
}
