import request from './request'

/**
 * 查询信用分变更记录
 */
export function getCreditRecords(pageNum = 1, pageSize = 10) {
  return request({
    url: '/credit/records',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}
