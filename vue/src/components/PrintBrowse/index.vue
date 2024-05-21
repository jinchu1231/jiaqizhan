<template>
  <el-dialog v-bind="$attrs" :close-on-click-modal="false" :modal-append-to-body="false"
    v-on="$listeners" @open="onOpen" fullscreen lock-scroll class="JNPF-full-dialog"
    :show-close="false" :modal="false" append-to-body>
    <div class="JNPF-full-dialog-header">
      <div class="header-title">
        <img src="@/assets/images/jnpf.png" class="header-logo" />
        <p class="header-txt">· 打印预览</p>
      </div>
      <div class="options">
        <el-button type="primary" size="small" @click="word">下 载</el-button>
        <el-button type="primary" size="small" @click="print('single')">打 印</el-button>
        <el-button @click="closeDialog()">{{ $t("common.cancelButton") }}</el-button>
      </div>
    </div>
    <div ref="barcodewrap"></div>
    <div v-loading="loading">
      <div class="main" ref="tsPrint">
        <div class="print-content" v-html="item" v-for="(item, index) in batchData" :key="index" />
      </div>
    </div>
  </el-dialog>
</template>
<script>
import printOptionApi from "./printMinxin.js";
export default {
  mixins: [printOptionApi],
  methods: {
    onOpen() {
      this.initData()
    },
  },
};
</script>
<style lang="scss" scoped>
.print-content {
  background: white;
  padding: 40px 30px;
  margin: 0 auto;
  border-radius: 4px;
  width: 776px;
  height: 100%;
  overflow: auto;
  color: #000;
}
</style>
