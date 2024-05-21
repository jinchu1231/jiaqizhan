const { APIURl } = require('./apiUrl')

module.exports = {
  APIURl,
  timeout: process.env.NODE_ENV === 'development' ? 1000000 : 1000000,
  comUploadUrl: process.env.VUE_APP_BASE_API + '/api/file/Uploader',
  comUrl: process.env.VUE_APP_BASE_API,
  // 本地文件预览
  filePreviewServer: process.env.NODE_ENV === 'development' ? 'http://localhost:30090/FileServer' : process.env.VUE_APP_BASE_API + '/FileServer',
  // 大屏应用前端路径
  dataV: process.env.NODE_ENV === 'development' ? 'http://localhost:8100/DataV' : window.location.origin + '/DataV',
  // 数据报表接口
  reportServer: process.env.NODE_ENV === 'development' ? 'http://localhost:30007' : process.env.VUE_APP_BASE_API + '/ReportServer',
  // 报表前端
  report: process.env.NODE_ENV === 'development' ? 'http://localhost:8200' : process.env.VUE_APP_BASE_API + '/Report',
  aMapWebKey: '09485f01587712b3c04e5a9abf324237',
  aMapJsKey: '26a65601349a5ec88318721884ef81b5',
  aMapSecurityJsCode: '243e837c2ba077b4143b9a9dd2893992',
  cipherKey: 'EY8WePvjM5GGwQzn', // 加密key
}
