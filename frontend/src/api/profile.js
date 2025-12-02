import request from './request'

// 获取当前用户资料
export function getMyProfile() {
  return request.get('/profile/me')
}

// 获取指定用户资料
export function getUserProfile(userId) {
  return request.get(`/profile/${userId}`)
}

// 更新个人资料
export function updateProfile(data) {
  return request.put('/profile/update', data)
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/profile/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取用户统计数据
export function getUserStatistics(userId) {
  if (userId) {
    return request.get(`/profile/statistics/${userId}`)
  }
  return request.get('/profile/statistics')
}
