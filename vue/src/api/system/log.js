import request from '@/utils/request'

// 获取系统日志信息
export function getLogList(data) {
  return request({
    url: `/api/system/Log`,
    method: 'GET',
    data
  })
}
// 获取系统日志详情
export function getInfo (id) {
  return request({
    url: `/api/system/Log/${id}`,
    method: 'GET'
  })
}
// 删除或批量删除日志
export function delLog(data) {
  return request({
    url: '/api/system/Log',
    method: 'DELETE',
    data
  })
}
// 一键清空
export function batchDelLog(type) {
  return request({
    url: `/api/system/Log/${type}`,
    method: 'DELETE'
  })
}
// 个人信息日志一键清空
export function deleteAll(type) {
  return request({
    url: `/api/system/Log/deleteLoginLog`,
    method: 'DELETE'
  })
}