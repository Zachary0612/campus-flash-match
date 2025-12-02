import request from './request'

// 提交评价
export function submitRating(data) {
  return request.post('/rating/submit', data)
}

// 评价用户（别名）
export function rateUser(data) {
  return request.post('/rating/submit', data)
}

// 获取收到的评价
export function getReceivedRatings(params = {}) {
  return request.get('/rating/received', { params })
}

// 获取指定用户收到的评价
export function getUserReceivedRatings(userId, params = {}) {
  return request.get(`/rating/received/${userId}`, { params })
}

// 获取发出的评价
export function getGivenRatings(params = {}) {
  return request.get('/rating/given', { params })
}

// 获取事件的评价列表
export function getEventRatings(eventId) {
  return request.get(`/rating/event/${eventId}`)
}

// 获取用户平均评分
export function getAverageRating(userId) {
  return request.get(`/rating/average/${userId}`)
}

// 检查是否可以评价
export function canRate(eventId, ratedUserId) {
  return request.get('/rating/can-rate', {
    params: { eventId, ratedUserId }
  })
}
