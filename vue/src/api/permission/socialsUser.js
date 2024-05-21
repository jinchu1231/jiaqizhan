import request from '@/utils/request'

// 第三方登录获取登录页面
export function otherLogin(data, param) {
  return request({
    url: `/api/oauth/socials/render/${data}?ticket=${param}`,
    method: 'get',

  })
}
// 第三方登录回调列表后点击登录
export function socialsLogin(data) {
  return request({
    url: `/api/oauth/Login/socials`,
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}
//获取登录配置
export function getLoginConfig() {
  return request({
    url: `/api/oauth/getLoginConfig`,
    method: 'get',
  })
}
//获取登录票据
export function getTicket() {
  return request({
    url: `/api/oauth/getTicket`,
    method: 'get',
  })
}
//根据票据获取登录状态
export function getTicketStatus(data) {
  return request({
    url: `/api/oauth/getTicketStatus/${data}`,
    method: 'get',
  })
}
// 获取用户授权列表
export function getSocialsLoginList(data) {
  return request({
    url: '/api/permission/socials/login',
    method: 'GET',
    data
  })
}
// 获取用户授权列表
export function getSocialsUserList() {
  return request({
    url: '/api/permission/socials',
    method: 'GET',
  })
}
// 获取用户授权列表
export function getSocialsUserListByUser(userId) {
  return request({
    url: '/api/permission/socials?userId=' + userId,
    method: 'GET',
  })
}
// 获取用户授权列表
export function binding(data) {
  return request({
    url: `/api/permission/socials/render/${data}`,
    method: 'GET',
  })
}
// 解绑
export function deleteSocials(userId, id) {
  return request({
    url: `/api/permission/socials/${id}?userId=${userId}`,
    method: 'DELETE',
  })
}