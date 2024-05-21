<template>
  <el-dialog :close-on-click-modal="false" class="JNPF-dialog JNPF-dialog_center form-script-dialog"
    lock-scroll append-to-body v-bind="$attrs" width="1000px" :modal-append-to-body="false"
    v-on="$listeners" destroy-on-close :visible.sync="dialogVisible">
    <span slot="title" class="dialog-title">
      <span>{{title}}
        <el-tooltip content="小程序不支持在线JS脚本" placement="top-start">
          <a class="el-icon-question tooltip-question"></a>
        </el-tooltip>
      </span>
    </span>
    <div class="form-script-dialog-body">
      <div class="right-main">
        <div class="codeEditor">
          <JNPFCodeEditor v-model="text" :options="options" ref="CodeEditor" />
        </div>
        <div class="tips">
          <p>支持JavaScript的脚本，
            <ScriptDemo :type="type" />
          </p>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="onClose()">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import JNPFCodeEditor from '@/components/JNPFEditor/monaco'
import ScriptDemo from '@/components/Generator/index/ScriptDemo';
export default {
  components: { JNPFCodeEditor, ScriptDemo },
  props: ['value', 'type'],
  data() {
    return {
      text: '',
      options: {
        language: 'javascript'
      },
      title: "",
      dialogVisible: false
    }
  },

  methods: {
    init() {
      this.dialogVisible = true
      this.text = this.value
      this.$nextTick(() => {
        this.$refs.CodeEditor.changeEditor({
          value: this.text,
          options: this.options
        })
      });
      this.setTitle()
    },
    onClose() {
      this.$emit('updateScript', this.text)
      this.closeDialog()
    },
    closeDialog() {
      this.$emit('closeDialog')
    },
    setTitle() {
      switch (this.type) {
        case 'afterOnload':
          this.title = "表格脚本"
          break;
        case 'rowStyle':
          this.title = "行样式"
          break;
        case 'cellStyle':
          this.title = "单元格样式"
          break;
        default:
          break;
      }
    }
  }
}
</script>
