import request from '@/utils/request'
import jnpf from '@/utils/jnpf'

// 用户登录
export function login(data) {
  return request({
    url: '/api/oauth/Login',
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}
// 第三方登录
export function otherLogin(data) {
  return request({
    url: `/api/oauth/render/${data}`,
    method: 'get',

  })
}

// 获取当前用户信息
export function getInfo() {
  const systemCode = jnpf.getJnpfAppId() ? jnpf.getJnpfAppId().replace('JNPF_APP_', '') : '';
  return request({
    url: '/api/oauth/CurrentUser',
    method: 'get',
    data: { systemCode }
  })
}

// 修改密码信息发送
export function updatePasswordMessage() {
  return request({
    url: '/api/oauth/updatePasswordMessage',
    method: 'Post',
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/oauth/Logout',
    method: 'get'
  })
}

// 锁屏解锁登录
export function unlock(data) {
  return request({
    url: '/api/oauth/LockScreen',
    method: 'post',
    data
  })
}

// 获取默认配置
export function getConfig(account) {
  return request({
    url: `/api/oauth/getConfig/${account}`,
    method: 'get'
  })
}
