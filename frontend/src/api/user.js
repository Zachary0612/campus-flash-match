import request from './request'

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data: {
      studentId: data.studentId,
      nickname: data.nickname,
      password: data.password
    }
  })
}

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      studentId: data.studentId,
      password: data.password
    }
  })
}

/**
 * 绑定校园位置
 */
export function bindLocation(pointId) {
  return request({
    url: '/user/location',
    method: 'put',
    params: { pointId }
  })
}

/**
 * 查询信用分及变更记录
 */
export function getCreditInfo() {
  return request({
    url: '/user/credit',
    method: 'get'
  })
}

/**
 * 获取校园点位列表
 */
export function getCampusPoints() {
  return request({
    url: '/user/points',
    method: 'get'
  })
}