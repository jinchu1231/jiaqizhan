import request from '@/utils/request'

// 获取集成助手列表
export function getIntegrateList(data) {
  return request({
    url: '/api/visualdev/Integrate',
    method: 'get',
    data
  })
}
// 获取集成助手信息
export function getInfo(id) {
  return request({
    url: '/api/visualdev/Integrate' + `/${id}`,
    method: 'get',
  })
}
// 删除集成助手
export function delIntegrate(id) {
  return request({
    url: '/api/visualdev/Integrate' + `/${id}`,
    method: 'delete'
  })
}
// 修改集成助手
export function update(data) {
  return request({
    url: '/api/visualdev/Integrate' + `/${data.id}`,
    method: 'put',
    data
  })
}
// 更新功能状态
export function updateState(id) {
  return request({
    url: '/api/visualdev/Integrate' + `/${id}/Actions/State`,
    method: 'put'
  })
}
// 新建集成助手
export function create(data) {
  return request({
    url: '/api/visualdev/Integrate',
    method: 'post',
    data
  })
}
// 复制集成助手
export function copy(id) {
  return request({
    url: `/api/visualdev/Integrate/${id}/Actions/Copy`,
    method: 'post'
  })
}
// 导出集成助手
export function exportData(id) {
  return request({
    url: `/api/visualdev/Integrate/${id}/Actions/Export`,
    method: 'post'
  })
}
//执行列表
export function getQueueList() {
  return request({
    url: '/api/visualdev/IntegrateTask/queueList',
    method: 'get'
  })
}
//日志列表
export function getTaskList(data) {
  return request({
    url: `/api/visualdev/IntegrateTask`,
    method: 'get',
    data
  })
}
// 删除日志
export function delTask(id) {
  return request({
    url: `/api/visualdev/IntegrateTask/${id}`,
    method: 'delete',
  })
}
// 重试日志
export function retryTask(id) {
  return request({
    url: `/api/visualdev/IntegrateTask/${id}/retry`,
    method: 'put',
  })
}
//日志详情
export function getTaskInfo(id) {
  return request({
    url: `/api/visualdev/IntegrateTask/${id}`,
    method: 'get',
  })
}

// 节点重试
export function nodeRetryTask(data) {
  return request({
    url: `/api/visualdev/IntegrateTask/${data.id}/nodeRetry`,
    method: 'get',
    data
  })
}
// 获取webhookUrl
export function getWebhookUrl(id) {
  return request({
    url: `/api/visualdev/Hooks/getUrl?id=${id}`,
    method: 'get',
  })
}
// 获取webhook字段
export function getWebhookParams(randomStr) {
  return request({
    url: `/api/visualdev/Hooks/getParams/${randomStr}`,
    method: 'get',
  })
}
// webhook开启接收请求
export function webhookStart(id, randomStr) {
  return request({
    url: `/api/visualdev/Hooks/${id}/start/${randomStr}`,
    method: 'get',
  })
}