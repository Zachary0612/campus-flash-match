package com.campus.service;

import com.campus.dto.request.EventCommentDTO;
import com.campus.vo.EventCommentVO;
import java.util.List;

/**
 * 事件评论Service接口
 */
public interface CommentService {

    /**
     * 发表评论
     * @param userId 用户ID
     * @param dto 评论内容
     * @return 评论ID
     */
    Long addComment(Long userId, EventCommentDTO dto);

    /**
     * 删除评论
     * @param userId 用户ID
     * @param commentId 评论ID
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 获取事件评论列表（树形结构）
     * @param eventId 事件ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 评论列表
     */
    List<EventCommentVO> getEventComments(String eventId, Integer pageNum, Integer pageSize);

    /**
     * 点赞评论
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 获取事件评论数
     * @param eventId 事件ID
     * @return 评论数
     */
    Integer getCommentCount(String eventId);
}
