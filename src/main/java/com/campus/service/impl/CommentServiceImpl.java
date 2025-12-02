package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.request.EventCommentDTO;
import com.campus.entity.EventComment;
import com.campus.entity.EventHistory;
import com.campus.entity.SysUser;
import com.campus.exception.BusinessException;
import com.campus.mapper.EventCommentMapper;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.service.CommentService;
import com.campus.service.NotificationService;
import com.campus.vo.EventCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事件评论Service实现类
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private EventCommentMapper eventCommentMapper;

    @Autowired
    private EventHistoryMapper eventHistoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long userId, EventCommentDTO dto) {
        // 校验评论内容
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            throw new BusinessException("评论内容不能超过500字");
        }

        // 检查事件是否存在
        QueryWrapper<EventHistory> eventQuery = new QueryWrapper<>();
        eventQuery.eq("event_id", dto.getEventId());
        EventHistory event = eventHistoryMapper.selectOne(eventQuery);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        // 如果是回复，检查父评论是否存在
        if (dto.getParentId() != null) {
            EventComment parentComment = eventCommentMapper.selectById(dto.getParentId());
            if (parentComment == null || parentComment.getIsDeleted() == 1) {
                throw new BusinessException("回复的评论不存在");
            }
        }

        EventComment comment = new EventComment();
        comment.setEventId(dto.getEventId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        eventCommentMapper.insert(comment);

        // 发送通知
        SysUser commenter = sysUserMapper.selectById(userId);
        if (dto.getReplyToUserId() != null && !dto.getReplyToUserId().equals(userId)) {
            // 回复通知
            String content = commenter.getNickname() + " 回复了你的评论";
            notificationService.sendNotification(dto.getReplyToUserId(), "comment", "收到回复", content, dto.getEventId(), userId);
        } else if (!event.getUserId().equals(userId)) {
            // 评论通知（通知事件发起者）
            String content = commenter.getNickname() + " 评论了你的事件";
            notificationService.sendNotification(event.getUserId(), "comment", "收到评论", content, dto.getEventId(), userId);
        }

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        int rows = eventCommentMapper.softDelete(commentId, userId);
        if (rows == 0) {
            throw new BusinessException("删除失败，评论不存在或无权限");
        }
    }

    @Override
    public List<EventCommentVO> getEventComments(String eventId, Integer pageNum, Integer pageSize) {
        // 获取顶级评论
        QueryWrapper<EventComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("event_id", eventId)
                .isNull("parent_id")
                .eq("is_deleted", 0)
                .orderByDesc("create_time");

        Page<EventComment> page = new Page<>(pageNum, pageSize);
        Page<EventComment> result = eventCommentMapper.selectPage(page, queryWrapper);

        List<EventComment> topComments = result.getRecords();
        if (topComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有回复
        List<Long> topCommentIds = topComments.stream()
                .map(EventComment::getId)
                .collect(Collectors.toList());

        QueryWrapper<EventComment> replyQuery = new QueryWrapper<>();
        replyQuery.eq("event_id", eventId)
                .in("parent_id", topCommentIds)
                .eq("is_deleted", 0)
                .orderByAsc("create_time");
        List<EventComment> replies = eventCommentMapper.selectList(replyQuery);

        // 获取所有相关用户
        List<Long> userIds = new ArrayList<>();
        topComments.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) {
                userIds.add(c.getReplyToUserId());
            }
        });
        replies.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) {
                userIds.add(c.getReplyToUserId());
            }
        });

        List<Long> distinctUserIds = userIds.stream().distinct().collect(Collectors.toList());
        Map<Long, SysUser> userMap = new java.util.HashMap<>();
        if (!distinctUserIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(distinctUserIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        // 按父评论分组回复
        Map<Long, List<EventComment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(EventComment::getParentId));

        // 转换为VO
        List<EventCommentVO> voList = new ArrayList<>();
        for (EventComment c : topComments) {
            EventCommentVO vo = convertToVO(c, userMap);

            // 添加回复
            List<EventComment> commentReplies = replyMap.get(c.getId());
            if (commentReplies != null && !commentReplies.isEmpty()) {
                List<EventCommentVO> replyVOs = new ArrayList<>();
                for (EventComment reply : commentReplies) {
                    replyVOs.add(convertToVO(reply, userMap));
                }
                vo.setReplies(replyVOs);
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        eventCommentMapper.incrementLike(commentId);
    }

    @Override
    public Integer getCommentCount(String eventId) {
        return eventCommentMapper.countComments(eventId);
    }

    private EventCommentVO convertToVO(EventComment comment, Map<Long, SysUser> userMap) {
        EventCommentVO vo = new EventCommentVO();
        vo.setId(comment.getId());
        vo.setEventId(comment.getEventId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());

        SysUser user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        if (comment.getReplyToUserId() != null) {
            SysUser replyToUser = userMap.get(comment.getReplyToUserId());
            if (replyToUser != null) {
                vo.setReplyToNickname(replyToUser.getNickname());
            }
        }

        return vo;
    }
}
