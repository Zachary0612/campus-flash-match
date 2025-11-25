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
      password: data.password,
      email: data.email,
      verifyCode: data.verifyCode
    }
  })
}

/**
 * 发送邮箱验证码
 */
export function sendEmailCode(email) {
  return request({
    url: '/user/email-code',
    method: 'post',
    data: { email }
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
      account: data.account,
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