<template>
  <el-popover placement="top" title="参数介绍及使用示例" width="454" popper-class="script-demo-popover"
    trigger="click" @show="onShow">
    <div class="script-demo-popover-main" :style="{ height: editorHeight }">
      <JNPFCodeEditor v-model="currentContent" :options="editorOptions" ref="CodeEditor"
        :key="key" />
    </div>
    <template #reference>
      <el-link type="primary" :underline="false">参数介绍及使用示例</el-link>
    </template>
  </el-popover>
</template>

<script>
import JNPFCodeEditor from '@/components/JNPFEditor/monaco'

const commonContent = `\n// onlineUtils--在线js工具类\n// 获取用户信息\nconst userInfo = onlineUtils.getUserInfo()\n// 获取设备信息\nconst deviceInfo = onlineUtils.getDeviceInfo()\n// 请求接口(url,method='get',data,headers)\nonlineUtils.request('url', 'get', { param: '1' }, { header: '1' })\n// 路由跳转(url,type) (type仅移动端支持)\nonlineUtils.route('url')\n// 消息提示(message,type='info',duration=3000)\nonlineUtils.toast('message', 'info', 3000)`;
const formContent = `// formData--表单数据\nconsole.log(formData)\n// setFormData--设置表单某个组件数据(prop,value)\nsetFormData('key', 'value')\n// setShowOrHide--设置显示或隐藏(prop,value)\nsetShowOrHide('key', true)\n// setRequired--设置必填项(prop,value)\nsetRequired('key', true)\n// setDisabled--设置禁用项(prop,value)\nsetDisabled('key', true)`;
const formFieldContent = `// data--当前组件的选中数据\nconsole.log(data)\n// rowIndex--当前行下标(仅在子表中可用)\nconsole.log(rowIndex)\n` + formContent;
const btnEventContent = `// data--列表当前行数据\nconsole.log(data)\n// index--列表当前行下标\nconsole.log(index)\n// refresh--刷新列表\nrefresh()`;
const afterOnloadContent = `// data--列表行数据\nconsole.log(data)\n// tableRef--表格DOM元素\nconsole.log(tableRef)`;
const rowStyleContent = `// row--列表行数据\n// rowIndex--列表行下标\n({ row, rowIndex }) => {\r\n   return {\r\n      background: rowIndex%2==0 ? 'red' : 'blue'\r\n   }\r\n}`;
const cellStyleContent = `// row--列表行数据\n// column--列表列数据\n// rowIndex--列表行下标\n// columnIndex--列表列下标\n({ row, column, rowIndex, columnIndex }) => {\r\n    return {\r\n        color: rowIndex%2 == 0 ? 'blue' : 'red'\r\n    }\r\n}`;

export default {
  components: { JNPFCodeEditor },
  props: {
    type: {
      type: String,
      default: 'formField',
    },
  },
  data() {
    return {
      currentContent: '',
      editorOptions: {
        language: 'javascript',
        readOnly: true,
        lineNumbers: 'off',
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        glyphMargin: 0
      },
      editorHeight: '500px',
      key: 0
    }
  },
  methods: {
    onShow() {
      if (this.type === 'form') {
        this.currentContent = formContent + commonContent;
        this.editorHeight = '420px';
      } else if (this.type === 'btnEvent') {
        this.currentContent = btnEventContent + commonContent;
        this.editorHeight = '350px';
      } else if (this.type === 'afterOnload') {
        this.currentContent = afterOnloadContent + commonContent;
        this.editorHeight = '310px';
      } else if (this.type === 'rowStyle') {
        this.currentContent = rowStyleContent;
        this.editorHeight = '140px';
      } else if (this.type === 'cellStyle') {
        this.currentContent = cellStyleContent;
        this.editorHeight = '180px';
      } else {
        this.currentContent = formFieldContent + commonContent;
        this.editorHeight = '500px';
      }
      this.$nextTick(() => {
        this.key = +new Date()
        this.$refs.CodeEditor.changeEditor({
          value: this.currentContent,
          options: this.editorOptions
        })
      });
    }
  }
}
</script>
<style lang="scss">
.script-demo-popover {
  .script-demo-popover-main {
    width: 440px;
  }
}
</style>