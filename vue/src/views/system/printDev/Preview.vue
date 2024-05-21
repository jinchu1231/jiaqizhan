<template>
  <el-dialog v-bind="$attrs" :close-on-click-modal="false" :modal-append-to-body="false"
    v-on="$listeners" @open="onOpen" fullscreen lock-scroll class="JNPF-full-dialog"
    :show-close="false" :modal="false">
    <div class="JNPF-full-dialog-header">
      <div class="header-title">
        <img src="@/assets/images/jnpf.png" class="header-logo" />
        <p class="header-txt"> · 打印预览</p>
      </div>
      <div class="options">
        <el-button type="primary" size="small" @click="print">打印</el-button>
        <el-button @click="closeDialog()">{{$t('common.cancelButton')}}</el-button>
      </div>
    </div>
    <div class="main" v-loading="loading">
      <div ref="tsPrint" class="print-content" v-html="data" />
    </div>
  </el-dialog>
</template>

<script>
import { getPrintDevInfo } from '@/api/system/printDev'
export default {
  props: ['id'],
  data() {
    return {
      data: '',
      loading: false
    }
  },
  methods: {
    onOpen() {
      this.loading = true
      getPrintDevInfo(this.id).then(res => {
        this.data = res.data.printTemplate
        const pageBreak = '<p style="page-break-after:always;"></p>';
        this.data = this.data.replace('<p><!-- pagebreak --></p>', pageBreak);
        this.loading = false
      })
    },
    closeDialog() {
      this.$emit('update:visible', false)
    },
    print() {
      let print = this.$refs.tsPrint.innerHTML
      let newWindow = window.open('_blank')
      newWindow.document.body.innerHTML = print
      newWindow.print()
      newWindow.close()
    },
  }
}
</script>
<style lang="scss" scoped>
.print-content {
  background: white;
  padding: 40px 30px;
  margin: 0 auto;
  border-radius: 4px;
  width: 776px;
  height: 100%;
  overflow: hidden auto;
}
</style>