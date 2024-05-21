import request from '@/utils/request'

// 获取用户列表
export const getUserList = (data) => {
  return request({
    url: '/api/permission/Users',
    method: 'GET',
    data
  })
}
// 获取离职交接数据，获取用户工作详情
export const getWorkByUser = (fromId) => {
  return request({
    url: `/api/permission/Users/getWorkByUser/?fromId=${fromId}`,
    method: 'GET'
  })
}
// 获取离职交接数据，获取工作交接
export const getWorkHandover = (data) => {
  return request({
    url: `/api/permission/Users/workHandover`,
    method: 'post',
    data
  })
}
// 获取所有用户列表
export const UserListAll = () => {
  return request({
    url: '/api/permission/Users/All',
    method: 'GET'
  })
}

// 获取用户下拉框列表(公司+部门+用户)
export const getUserSelector = () => {
  return request({
    url: '/api/permission/Users/Selector',
    method: 'GET'
  })
}

// 新建用户
export const createUser = (data) => {
  return request({
    url: '/api/permission/Users',
    method: 'POST',
    data
  })
}

// 修改用户
export const updateUser = (data) => {
  return request({
    url: `/api/permission/Users/${data.id}`,
    method: 'PUT',
    data
  })
}

// 获取用户信息
export const getUserInfo = (id) => {
  return request({
    url: `/api/permission/Users/${id}`,
    method: 'GET'
  })
}

// 删除用户
export const delUser = (id) => {
  return request({
    url: `/api/permission/Users/${id}`,
    method: 'DELETE'
  })
}

// 修改用户密码
export const resetUserPassword = (data) => {
  return request({
    url: `/api/permission/Users/${data.id}/Actions/ResetPassword`,
    method: 'POST',
    data
  })
}

// 更新用户状态
export const updateUserState = (id) => {
  return request({
    url: `/api/permission/Users/${id}/Actions/State`,
    method: 'PUT'
  })
}

// 解除锁定
export const unlockUser = (id) => {
  return request({
    url: `/api/permission/Users/${id}/Actions/unlock`,
    method: 'PUT'
  })
}

// IM通讯获取用户
export const getImUser = data => {
  return request({
    url: '/api/permission/Users/ImUser',
    method: 'GET',
    data
  })
}

// 获取用户下拉框列表
export const getImUserSelector = (organizeId, data = {}) => {
  return request({
    url: `/api/permission/Users/ImUser/Selector/${organizeId}`,
    method: 'post',
    data
  })
}
// 获取用户基本信息
export const getUserInfoList = ids => {
  return request({
    url: '/api/permission/Users/getUserList',
    method: 'post',
    data: { ids }
  })
}

// 获取我的下属
export const getSubordinates = (keyword) => {
  return request({
    url: '/api/permission/Users/getSubordinates',
    method: 'post',
    data: { keyword }
  })
}
// 获取当前组织用户
export const getOrganization = (data) => {
  return request({
    url: '/api/permission/Users/getOrganization',
    method: 'get',
    data
  })
}
// 通过岗位id获取用户树
export const getUsersByPositionId = (data) => {
  return request({
    url: '/api/permission/Users/GetUsersByPositionId',
    method: 'get',
    data
  })
}
// 通过角色id获取用户列表
export const getUsersByRoleId = (data) => {
  return request({
    url: '/api/permission/Users/getUsersByRoleId',
    method: 'get',
    data
  })
}
// 通过角色id获取组织下的用户列表
export const getUsersByRoleOrgId = (data) => {
  return request({
    url: `/api/permission/Users/GetUsersByRoleOrgId`,
    method: 'get',
    data
  })
}
// 通过部门id,岗位id,角色id,分组id,用户id获取用户列表(带分页)
export const getUsersByUserCondition = (data) => {
  return request({
    url: '/api/permission/Users/UserCondition',
    method: 'post',
    data
  })
}
// 导出Excel
export function ExportExcel(data) {
  return request({
    url: '/api/permission/Users/ExportData',
    method: 'get',
    data
  })
}
// 模板下载
export function TemplateDownload(data) {
  return request({
    url: '/api/permission/Users/TemplateDownload',
    method: 'get',
    data
  })
}
// 导入
export function ImportData(data) {
  return request({
    url: '/api/permission/Users/ImportData',
    method: 'post',
    data
  })
}
// 导入预览
export function ImportPreview(data) {
  return request({
    url: '/api/permission/Users/ImportPreview',
    method: 'get',
    data
  })
}
// 导出错误数据
export function ExportExceptionData(data) {
  return request({
    url: '/api/permission/Users/ExportExceptionData',
    method: 'post',
    data
  })
}
// 获取用户下拉框列表
export const getListByAuthorize = (organizeId, keyword) => {
  return request({
    url: `/api/permission/Users/GetListByAuthorize/${organizeId}`,
    method: 'post',
    data: { keyword }
  })
}
// 获取选中组织、岗位、角色、用户基本信息
export const getSelectedList = ids => {
  return request({
    url: '/api/permission/Users/getSelectedList',
    method: 'post',
    data: { ids }
  })
}
// 通过组织、岗位、角色、用户ids获取选中用户基本信息
export const getSelectedUserList = data => {
  return request({
    url: '/api/permission/Users/getSelectedUserList',
    method: 'post',
    data
  })
}

// 获取默认当前值用户ID
export function getDefaultCurrentValueUserId(data) {
  return request({
    url: `/api/permission/Users/getDefaultCurrentValueUserId`,
    method: 'post',
    data
  })
}

// 获取默认当前值用户ID（同步）
export async function getDefaultCurrentValueUserIdAsync(data) {
  return new Promise(resolve => {
    request({
      url: `/api/permission/Users/getDefaultCurrentValueUserId`,
      method: 'post',
      data
    }).then(ret => {
      resolve(ret)
    })
  })
}
