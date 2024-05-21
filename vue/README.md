## 一 环境要求

### 1.1 开发环境

- 操作系统：`Windows 10/11`，`MacOS`；
- `Node.js v12/v4/v16`(v16 版本某些情况下可能需要安装 `Python3` 环境)；
- 浏览器推荐使用 `Chrome 90` 及以上版本；
- `Visual Studio Code`(简称 VSCode)

### 1.2 运行环境

`Nginx 1.18.0` 及以上版本或 `TongHttpServer 6.0` (信创环境)

## 二 关联项目

> 需要使用下表中的对应分支

| 项目                   | 分支   | 分支(Coding)  | 说明                |
| ---------------------- | ------ | ------------- | ------------------- |
| **后端**(任一后端服务) |        |               |                     |
| jnpf-java-boot         | v3.6.x | v3.6.x-stable | Java 单体项目源码   |
| jnpf-java-cloud        | v3.6.x | v3.6.x-stable | Java 微服务项目源码 |
| jnpf-dotnet            | v3.6.x | v3.6.x-stable | .NET 单体项目源码   |
| jnpf-dotnet-cloud      | v3.6.x | v3.6.x-stable | .NET 微服务项目源码 |
| **前端**               |        |               |                     |
| jnpf-web-datascreen    | v3.6.x | v3.6.x-stable | 大屏前端项目源码    |
| jnpf-web-datareport    | v3.6.x | v3.6.x-stable | 报表前端项目源码    |

## 三 使用说明

### 3.1 开发环境

#### 3.1.1 安装依赖

使用如下命令安装项目依赖

```bash
npm install --registry http://registry.npmmirror.com
```

#### 3.1.2 后端接口配置

修改 `src/utils/apiUrl.js` 中的后端接口地址

- Java 项目本地开发默认地址：`http://localhost:30000`
- .NET 项目本地开发默认地址：`http://localhost:5000`

```bash
module.exports = {
  // 开发环境接口配置
  APIURl: 'http://localhost:30000'
}
```

#### 3.1.3 关联项目配置

打开 `src/utils/define.js` 配置文件，默认配置如下所示

```bash
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
```

#### 3.1.4 本地运行

完成上述操作后，使用如下命令运行前端项目

```bash
npm run dev
```

### 3.2 运行环境

> 测试或生产环境

如果需要测试或生产环境发布，使用如下命令打包项目

```bash
npm run build
```

然后将项目根目录下 `/dist/` 中所有的文件上传至服务器。

## 四 常见问题

### 4.1 修改项目基本信息

打开项目根目录 `vue.config.js` 文件，可以看到 `本地运行端口号`、`网站标题` 等配置。

```bash
# 修改网站标题（第11行）
const name = defaultSettings.title || 'JNPF快速开发平台'

# 修改本地运行端口（第18行）
const port = process.env.port || process.env.npm_config_port || 3000
```

### 4.2 代码更新后报错

在开发或打包时报依赖缺失，可以先删除项目根目录下的 `package-lock.json` 文件，然后重新执行 `npm install --registry http://registry.npmmirror.com` 安装依赖即可解决。
