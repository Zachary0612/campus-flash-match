import request from './request'

/**
 * 获取事件聊天记录
 */
export function getChatMessages(eventId) {
  return request({
    url: `/chat/messages/${eventId}`,
    method: 'get'
  })
}
