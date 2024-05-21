import request from '@/utils/request'

// 获取菜单列表
export const getMenuList = (systemId, data) => {
  return request({
    url: `/api/system/Menu/ModuleBySystem/${systemId}`,
    method: 'GET',
    data
  })
}

// 获取上级菜单下拉框
export const getMenuSelector = (data, id, systemId) => {
  return request({
    url: '/api/system/Menu/Selector/' + (!!id ? id : 0) + "/" + (systemId || 0),
    method: 'GET',
    data
  })
}


// 获取菜单列表（下拉框）
export const getSelectorAll = data => {
  return request({
    url: '/api/system/Menu/Selector/All',
    method: 'GET',
    data
  })
}

// 添加菜单
export const createMenu = (data) => {
  return request({
    url: '/api/system/Menu',
    method: 'POST',
    data
  })
}

// 修改菜单
export const updateMenu = (data) => {
  return request({
    url: `/api/system/Menu/${data.id}`,
    method: 'PUT',
    data
  })
}

// 获取菜单信息
export const getMenuInfo = (id) => {
  return request({
    url: `/api/system/Menu/${id}`,
    method: 'GET'
  })
}

// 删除菜单
export const delMenu = (id) => {
  return request({
    url: `/api/system/Menu/${id}`,
    method: 'DELETE'
  })
}

// 更新菜单状态
export const updateMenuState = (id) => {
  return request({
    url: `/api/system/Menu/${id}/Actions/State`,
    method: 'PUT'
  })
}

// 导出应用菜单数据
export const exportMenu = id => {
  return request({
    url: `/api/system/Menu/${id}/Actions/Export`,
    method: 'GET'
  })
}

// 数据权限字段名称
export const getFieldNameList = (id, name) => {
  return request({
    url: `/api/system/Module${name}/${id}/FieldList`,
    method: 'GET'
  })
}

//查看权限组
export const getMenuPermissionGroup = (data) => {
  return request({
    url: `/api/system/Menu/getPermissionGroup/${data.id}`,
    method: 'GET'
  })
}

//通过权限组id获取相关权限”
export const getMenuPermission = (data) => {
  return request({
    url: `/api/system/Menu/getPermission/${data.id}/${data.permissionId}`,
    method: 'GET'
  })
}


