import request from '@/utils/request'

// 获取下载文件链接
export function getDownloadUrl(type, fileName) {
  return request({
    url: `/api/file/Download/${type}/${fileName}`,
    method: 'get'
  })
}
// 获取全部下载文件链接（打包下载）
export function getPackDownloadUrl(type, data) {
  return request({
    url: `/api/file/PackDownload/${type}`,
    method: 'POST',
    data
  })
}
// 预览文件
export function PreviewFile(data) {
  return request({
    url: `/api/file/Uploader/Preview`,
    method: 'get',
    data
  })
}
// 获取列设置
export const getColumnsByModuleId = (moduleId) => {
  return request({
    url: `/api/permission/Authority/GetColumnsByModuleId/${moduleId}`,
    method: 'GET'
  })
}
// 设置列设置
export const setColumnsByModuleId = (data) => {
  return request({
    url: `/api/permission/Authority/SetColumnsByModuleId`,
    method: 'PUT',
    data
  })
}
// 分片组装
export const chunkMerge = (data) => {
  return request({
    url: `/api/file/merge`,
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}

// 下载导入示例模板
export function getTemplateDownload(modelId, url) {
  return request({
    url: `/api/${url}/TemplateDownload`,
    method: 'get'
  })
}

// 导入预览
export function getImportPreview(modelId, data, url) {
  return request({
    url: `/api/${url}/ImportPreview`,
    method: 'get',
    data
  })
}

// 导入数据
export function importData(modelId, data, url) {
  return request({
    url: `/api/${url}/ImportData`,
    method: 'post',
    data
  })
}

// 导出错误数据
export function getImportExceptionData(modelId, data, url) {
  return request({
    url: `/api/${url}/ImportExceptionData`,
    method: 'post',
    data
  })
}

// 导入
export function upload(url, data) {
  return request({
    url: url,
    method: 'post',
    data
  })
}

// 查询附近数据
export function getAroundList(data) {
  return request({
    url: '/api/system/Location/around',
    method: 'get',
    data
  })
}

// 根据关键字查询附近数据
export function getTextList(data) {
  return request({
    url: '/api/system/Location/text',
    method: 'get',
    data
  })
}

// 查询附近数据
export function getInputTips(data) {
  return request({
    url: '/api/system/Location/inputtips',
    method: 'get',
    data
  })
}
