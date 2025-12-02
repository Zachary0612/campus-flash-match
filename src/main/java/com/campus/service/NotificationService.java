package com.campus.service;

import com.campus.vo.NotificationVO;
import java.util.List;

/**
 * 通知消息Service接口
 */
public interface NotificationService {

    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 通知列表
     */
    List<NotificationVO> getNotifications(Long userId, String type, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数
     * @param userId 用户ID
     * @return 未读数
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     * @param userId 用户ID
     * @param notificationId 通知ID（为null时标记全部已读）
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 发送通知
     * @param userId 接收者ID
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     * @param senderId 发送者ID
     */
    void sendNotification(Long userId, String type, String title, String content, String relatedId, Long senderId);

    /**
     * 批量发送通知
     * @param userIds 接收者ID列表
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     * @param senderId 发送者ID
     */
    void sendBatchNotification(List<Long> userIds, String type, String title, String content, String relatedId, Long senderId);
}
