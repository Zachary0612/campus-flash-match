import request from './request'

/**
 * 发起事件
 */
export function createEvent(data) {
  return request({
    url: '/event/create',
    method: 'post',
    data: {
      title: data.title,
      eventType: data.eventType,
      targetNum: data.targetNum,
      expireMinutes: data.expireMinutes,
      pointId: data.pointId,
      extMeta: data.extMeta
    }
  })
}

/**
 * 查询附近事件
 */
export function getNearbyEvents(eventType, radius = 1000) {
  // 构建参数对象
  const params = { radius }
  
  // 只有当eventType有值时才添加到参数中
  if (eventType) {
    params.eventType = eventType
  }
  
  return request({
    url: '/event/nearby',
    method: 'get',
    params
  })
}

/**
 * 参与事件
 */
export function joinEvent(eventId) {
  return request({
    url: '/event/join',
    method: 'post',
    params: { eventId }
  })
}

/**
 * 退出事件
 */
export function quitEvent(eventId) {
  return request({
    url: '/event/quit',
    method: 'post',
    params: { eventId }
  })
}

/**
 * 查询事件历史
 */
export function getEventHistory(pageNum = 1, pageSize = 10) {
  return request({
    url: '/event/history',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}
