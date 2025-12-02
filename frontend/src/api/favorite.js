import request from './request'

// 收藏事件
export function addFavorite(eventId) {
  return request.post(`/favorite/${eventId}`)
}

// 取消收藏
export function removeFavorite(eventId) {
  return request.delete(`/favorite/${eventId}`)
}

// 获取收藏列表
export function getFavoriteList(params = {}) {
  return request.get('/favorite/list', { params })
}

// 检查是否已收藏
export function checkFavorite(eventId) {
  return request.get(`/favorite/check/${eventId}`)
}
