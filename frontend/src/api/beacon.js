import request from './request'

/**
 * 发布信标
 */
export function publishBeacon(data) {
  return request({
    url: '/beacon/publish',
    method: 'post',
    data: {
      locationDesc: data.locationDesc,
      expireMinutes: data.expireMinutes,
      pointId: data.pointId,
      mapLocation: data.mapLocation,
      description: data.description,
      mediaUrls: data.mediaUrls
    }
  })
}

/**
 * 举报虚假信标
 */
export function reportBeacon(eventId) {
  return request({
    url: '/beacon/report',
    method: 'post',
    params: { eventId }
  })
}
