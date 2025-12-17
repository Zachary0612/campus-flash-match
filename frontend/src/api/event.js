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
      extMeta: data.extMeta,
      description: data.description,
      mediaUrls: data.mediaUrls
    }
  })
}

/**
 * 查询附近事件
 */
export function getNearbyEvents(eventType, radius = 3000) {
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

/**
 * 查询已完成事件（含参与者）
 */
export function getCompletedEvents() {
  return request({
    url: '/event/completed',
    method: 'get'
  })
}

/**
 * 获取事件详情
 */
export function getEventDetail(eventId) {
  return request({
    url: '/event/detail',
    method: 'get',
    params: { eventId }
  })
}

/**
 * 搜索事件
 */
export function searchEvents(data) {
  return request({
    url: '/event/search',
    method: 'post',
    data
  })
}

/**
 * 取消事件
 */
export function cancelEvent(eventId) {
  return request({
    url: '/event/cancel',
    method: 'post',
    params: { eventId }
  })
}

/**
 * 确认事件完成
 */
export function confirmEventCompletion(eventId) {
  return request({
    url: '/event/confirm',
    method: 'post',
    params: { eventId }
  })
}

/**
 * 获取事件确认状态
 */
export function getConfirmationStatus(eventId) {
  return request({
    url: '/event/confirm-status',
    method: 'get',
    params: { eventId }
  })
}

/**
 * 获取我的事件（包含进行中和历史事件）
 * @param type all-全部, created-我发起的, joined-我参与的
 */
export function getMyEvents(type = 'all', pageNum = 1, pageSize = 20) {
  return request({
    url: '/event/my-events',
    method: 'get',
    params: { type, pageNum, pageSize }
  })
}
