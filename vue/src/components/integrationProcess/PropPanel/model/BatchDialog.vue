<template>
  <el-dialog title="JSON数据格式" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center" lock-scroll width="600px" append-to-body
    destroy-on-close>
    <el-form layout="vertical">
      <div class="json-demo">
        <pre>
          // 示例
          [
            {
              "id":"fullName",
              "fullName":"名称"
            }
          ]
        </pre>
      </div>
      <el-form-item label="请输入JSON数据格式生成接口字段">
        <div class="formCodeEditor">
          <JNPFCodeEditor :options="options" v-model="responseData" ref="CodeEditor" />
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="closeDialog()">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import JNPFCodeEditor from '@/components/JNPFEditor/monaco'
export default {
  components: { JNPFCodeEditor },
  data() {
    return {
      visible: false,
      options: {
        readOnly: false,
        language: 'json'
      },
      responseData: ''
    }
  },
  methods: {
    init() {
      this.visible = true
      this.responseData = ''
      this.$refs.CodeEditor.changeEditor({
        value: '',
        options: this.options
      })
    },
    closeDialog() {
      if (!this.responseData) return this.$message.warning('请输入字段JSON')
      const jsonStr = this.responseData.replace(/("\w+":)(?=[},])/g, '$1null')
      try {
        const data = JSON.parse(jsonStr);
        if (!Array.isArray(data)) throw 'error';
        this.$emit('confirm', data)
        this.visible = false
      } catch {
        this.$message.warning('JSON数据格式错误');
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.JNPF-dialog_center {
  >>> .el-dialog__body {
    padding-bottom: 20px !important;
  }
}
.json-demo {
  width: 100%;
  background: #f4f4f5;
  border-radius: 4px;
  color: #909399;
  padding-top: 10px;
  pre {
    margin-left: -40px;
    font-size: 12px;
  }
}
.formCodeEditor {
  width: 100%;
  height: 260px;
  margin: 0;
  padding: 0;
  border: 1px solid #c0c4cc;
  overflow: hidden;
}
</style>