import request from '@/utils/request'

// 获取岗位/角色/用户权限树形结构及数据
export const getAuthorizeValues = (objectId, data) => {
  return request({
    url: `/api/permission/Authority/Data/${objectId}/Values`,
    method: 'POST',
    data
  })
}

// 更新岗位/角色/用户权限
export const updateAuthorizeList = (objectId, data) => {
  return request({
    url: `/api/permission/Authority/Data/${objectId}`,
    method: 'PUT',
    data
  })
}

// 批量设置权限
export const updateBatchAuthorize = (data) => {
  return request({
    url: `/api/permission/Authority/Data/Batch`,
    method: 'POST',
    data
  })
}
// 获取功能权限数据
export const getModelData = (id, ObjectType) => {
  return request({
    url: `/api/permission/Authority/Model/${id}/${ObjectType}`,
    method: 'GET'
  })
}
// 设置/更新功能权限
export const setModelData = (id, data) => {
  return request({
    url: `/api/permission/Authority/Model/${id}`,
    method: 'PUT',
    data
  })
}
//获取权限列表
export function getAuthorizeList(data) {
  return request({
    url: '/api/permission/PermissionGroup',
    method: 'get',
    data
  })
}
// 获取权限信息
export function getAuthorizeInfo(id) {
  return request({
    url: `/api/permission/PermissionGroup/${id}`,
    method: 'get'
  })
}
// 删除权限
export function Delete(id) {
  return request({
    url: `/api/permission/PermissionGroup/${id}`,
    method: 'DELETE'
  })
}
// 修改权限
export function Update(data) {
  return request({
    url: `/api/permission/PermissionGroup/${data.id}`,
    method: 'PUT',
    data
  })
}
// 新建权限
export function Create(data) {
  return request({
    url: '/api/permission/PermissionGroup',
    method: 'post',
    data
  })
}
//获取权限成员
export function getPermissionMember(id) {
  return request({
    url: `/api/permission/PermissionGroup/PermissionMember/${id}`,
    method: 'get'
  })
}
//保存权限成员
export function savePermissionMember(data) {
  return request({
    url: `/api/permission/PermissionGroup/PermissionMember/${data.id}`,
    method: 'post',
    data
  })
}
//复制权限
export function CopyPermissions(id) {
  return request({
    url: `/api/permission/PermissionGroup/${id}/Actions/Copy`,
    method: 'post',
  })
}
//获取权限组下拉
export function Selector() {
  return request({
    url: `/api/permission/PermissionGroup/Selector`,
    method: 'get',
  })
}
//获取门户权限组下拉
export function getPortalSelector(id) {
  return request({
    url: `/api/permission/Authority/Portal/${id}`,
    method: 'get',
  })
}
//保存门户权限
export const savePortal = (objectId, data) => {
  return request({
    url: `/api/permission/Authority/Portal/${objectId}`,
    method: 'post',
    data
  })
}