import { reportServer } from '@/utils/define'
import request from '@/utils/request'

// 获取报表列表
export const getDataReportList = data => {
  return request({
    url: reportServer + '/Data',
    method: 'GET',
    data
  })
}
// 获取报表下拉列表
export const getDataReportSelector = () => {
  return request({
    url: reportServer + '/Data/Selector',
    method: 'GET'
  })
}
// 删除报表
export const delDataReport = id => {
  return request({
    url: `${reportServer}/Data/${id}`,
    method: 'delete'
  })
}
// 复制报表
export const copyDataReport = id => {
  return request({
    url: `${reportServer}/Data/${id}/Actions/Copy`,
    method: 'post'
  })
}
// 启用禁用报表
export function release(id) {
  return request({
    url: `${reportServer}/Data/${id}/Actions/State`,
    method: 'put'
  })
}
// 导入报表
export function importData(url, data) {
  return request({
    url: `${reportServer}/Data/Actions/Import`,
    method: 'post',
    data
  })
}
// 获取信息
export const getDataReportInfo = id => {
  return request({
    url: `${reportServer}/Data/${id}`,
    method: 'GET'
  })
}