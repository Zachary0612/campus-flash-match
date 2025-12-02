package com.campus.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.dto.ResultDTO;
import com.campus.exception.BusinessException;
import com.campus.service.NotificationService;
import com.campus.vo.NotificationVO;

/**
 * 通知消息Controller
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public ResultDTO<List<NotificationVO>> getNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        List<NotificationVO> notifications = notificationService.getNotifications(userId, type, pageNum, pageSize);
        return ResultDTO.success("查询成功", notifications);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public ResultDTO<Integer> getUnreadCount(Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        Integer count = notificationService.getUnreadCount(userId);
        return ResultDTO.success("查询成功", count);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public ResultDTO<Void> markAsRead(
            @RequestParam(required = false) Long notificationId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        Long userId = Long.valueOf(principal.getName());
        notificationService.markAsRead(userId, notificationId);
        return ResultDTO.success(notificationId == null ? "全部标记已读" : "标记已读成功");
    }
}
