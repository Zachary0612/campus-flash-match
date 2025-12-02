import request from './request'

// 获取通知列表
export function getNotifications(params = {}) {
  return request.get('/notification/list', { params })
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

// 标记消息已读
export function markAsRead(notificationId) {
  return request.post('/notification/read', null, {
    params: { notificationId }
  })
}

// 标记全部已读
export function markAllAsRead() {
  return request.post('/notification/read')
}
