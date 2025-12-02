import request from './request'

// 关注用户
export function followUser(userId) {
  return request.post(`/follow/${userId}`)
}

// 取消关注
export function unfollowUser(userId) {
  return request.delete(`/follow/${userId}`)
}

// 获取关注列表
export function getFollowingList(params = {}) {
  return request.get('/follow/following', { params })
}

// 获取指定用户的关注列表
export function getUserFollowingList(userId, params = {}) {
  return request.get(`/follow/following/${userId}`, { params })
}

// 获取粉丝列表
export function getFollowerList(params = {}) {
  return request.get('/follow/followers', { params })
}

// 获取指定用户的粉丝列表
export function getUserFollowerList(userId, params = {}) {
  return request.get(`/follow/followers/${userId}`, { params })
}

// 检查是否已关注
export function checkFollow(userId) {
  return request.get(`/follow/check/${userId}`)
}
