import request from '@/utils/request'

//获取表名
export function getVisualTables(menudId, type) {
  return request({
    url: `/api/system/ModuleDataAuthorizeLink/getVisualTables/${menudId}/${type}`,
    method: 'get',
  })
}
//据表名获取数据表字段
export function getTableInfoByTableName(linkId, tableName, menuType, dataType, data) {
  return request({
    url: `/api/system/ModuleDataAuthorizeLink/${linkId}/Tables/${tableName}/Fields/${menuType}/${dataType}`,
    method: 'get',
    data
  })
}


//保存编辑数据连接
export function saveLinkData(data) {
  return request({
    url: `/api/system/ModuleDataAuthorizeLink/saveLinkData`,
    method: 'post',
    data
  })
}
//数据连接信息
export function getInfo(menudId, type) {
  return request({
    url: `/api/system/ModuleDataAuthorizeLink/getInfo/${menudId}/${type}`,
    method: 'get',
  })
}

