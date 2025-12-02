import request from './request'

// 发表评论
export function addComment(data) {
  return request.post('/comment/add', data)
}

// 删除评论
export function deleteComment(commentId) {
  return request.delete(`/comment/${commentId}`)
}

// 获取事件评论列表
export function getEventComments(eventId, params = {}) {
  return request.get(`/comment/event/${eventId}`, { params })
}

// 点赞评论
export function likeComment(commentId) {
  return request.post(`/comment/like/${commentId}`)
}

// 获取事件评论数
export function getCommentCount(eventId) {
  return request.get(`/comment/count/${eventId}`)
}
