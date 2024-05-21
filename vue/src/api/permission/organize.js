import request from '@/utils/request'

// 获取组织/公司列表
export const getOrganizeList = (data) => {
  return request({
    url: '/api/permission/Organize',
    method: 'GET',
    data
  })
}

// 获取组织/公司下拉框列表
export const getOrganizeSelector = (id) => {
  return request({
    url: '/api/permission/Organize/Selector/' + (!!id ? id : 0),
    method: 'GET'
  })
}

// 获取组织/公司下拉框列表(带权限)
export const getOrganizeSelectorByAuth = (id) => {
  return request({
    url: '/api/permission/Organize/SelectorByAuth/' + (!!id ? id : 0),
    method: 'GET'
  })
}

// 获取组织/公司树形
export const getOrganizeTree = () => {
  return request({
    url: '/api/permission/Organize/Tree',
    method: 'GET'
  })
}

// 新建组织/公司
export const createOrganize = (data) => {
  return request({
    url: '/api/permission/Organize',
    method: 'POST',
    data
  })
}

// 修改组织/公司
export const updateOrganize = (data) => {
  return request({
    url: `/api/permission/Organize/${data.id}`,
    method: 'PUT',
    data
  })
}

// 获取组织/公司信息
export const getOrganizeInfo = (id) => {
  return request({
    url: `/api/permission/Organize/${id}`,
    method: 'GET'
  })
}

// 删除组织/公司
export const delOrganize = (id) => {
  return request({
    url: `/api/permission/Organize/${id}`,
    method: 'DELETE'
  })
}

// 更新组织/公司状态
export const updateOrganizeState = (id) => {
  return request({
    url: `/api/permission/Organize/${id}/Actions/State`,
    method: 'PUT'
  })
}
// 获取分级管理
export const getOrganizeTrator = (organizeId) => {
  return request({
    url: `/api/permission/organizeAdminIsTrator/${organizeId}`,
    method: 'get'
  })
}
// 更新分级管理
export const setOrganizeTrator = (data) => {
  return request({
    url: `/api/permission/organizeAdminIsTrator/${data.organizeId}`,
    method: 'PUT',
    data
  })
}
// 通过部门id获取部门树形
export const getOrgByOrganizeCondition = (data) => {
  return request({
    url: `/api/permission/Organize/OrganizeCondition`,
    method: 'post',
    data
  })
}

// 获取默认当前值部门ID
export function getDefaultCurrentValueDepartmentId(data) {
  return request({
    url: `/api/permission/Organize/getDefaultCurrentValueDepartmentId`,
    method: 'post',
    data
  })
}

// 获取默认当前值部门ID（同步）
export async function getDefaultCurrentValueDepartmentIdAsync(data) {
  return new Promise(resolve => {
    request({
      url: `/api/permission/Organize/getDefaultCurrentValueDepartmentId`,
      method: 'post',
      data
    }).then(ret => {
      resolve(ret)
    })
  })
}


// 获取菜单权限返回权限组
export const getPermissionGroup = (data) => {
  return request({
    url: `/api/permission/PermissionGroup/getPermissionGroup`,
    method: 'get',
    data
  })
}

// 通过权限组id获取相关权限
export const getPermission = (data) => {
  return request({
    url: `/api/permission/PermissionGroup/getPermission`,
    method: 'get',
    data
  })
}