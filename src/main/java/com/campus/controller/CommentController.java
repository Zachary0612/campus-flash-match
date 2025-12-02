package com.campus.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.EventCommentDTO;
import com.campus.exception.BusinessException;
import com.campus.service.CommentService;
import com.campus.vo.EventCommentVO;

/**
 * 事件评论Controller
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping("/add")
    public ResultDTO<Long> addComment(@RequestBody EventCommentDTO dto, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        Long commentId = commentService.addComment(userId, dto);
        return ResultDTO.success("评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResultDTO<Void> deleteComment(@PathVariable Long commentId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        commentService.deleteComment(userId, commentId);
        return ResultDTO.success("删除成功");
    }

    /**
     * 获取事件评论列表
     */
    @GetMapping("/event/{eventId}")
    public ResultDTO<List<EventCommentVO>> getEventComments(
            @PathVariable String eventId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        List<EventCommentVO> comments = commentService.getEventComments(eventId, pageNum, pageSize);
        return ResultDTO.success("查询成功", comments);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{commentId}")
    public ResultDTO<Void> likeComment(@PathVariable Long commentId) {
        commentService.likeComment(commentId);
        return ResultDTO.success("点赞成功");
    }

    /**
     * 获取事件评论数
     */
    @GetMapping("/count/{eventId}")
    public ResultDTO<Integer> getCommentCount(@PathVariable String eventId) {
        Integer count = commentService.getCommentCount(eventId);
        return ResultDTO.success("查询成功", count);
    }
}
