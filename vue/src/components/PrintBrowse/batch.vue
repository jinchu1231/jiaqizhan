<template>
  <el-dialog v-bind="$attrs" :close-on-click-modal="false" :modal-append-to-body="false"
    v-on="$listeners" @open="onOpen" fullscreen lock-scroll class="JNPF-full-dialog"
    :show-close="false" :modal="false" append-to-body>
    <div class="JNPF-full-dialog-header">
      <div class="header-title">
        <img src="@/assets/images/jnpf.png" class="header-logo" />
        <p class="header-txt">· 打印预览</p>
      </div>
      <div class="header-page">
        <el-button @click="pageIndex = pageIndex - 1">上一页</el-button>
        <el-input-number style="width: 60px" class="text-center" v-model="pageIndex"
          @change="scrollpage" @blur="scrollpage" :controls="false" controls-position="right"
          :min="1" :max="batchData.length"></el-input-number>
        <span class="page-size">/ {{ batchData.length }}</span>
        <el-button @click="pageIndex = pageIndex + 1">下一页</el-button>
      </div>
      <div class="options">
        <el-button type="primary" size="small" @click="word">下 载</el-button>
        <el-button type="primary" size="small" @click="print('batch')">打 印</el-button>
        <el-button @click="closeDialog()">{{ $t("common.cancelButton") }}</el-button>
      </div>
    </div>
    <div ref="barcodewrap"></div>
    <div class="main" ref="tsPrint" v-loading="loading">
      <div class="print-content" v-html="item" v-for="(item, index) in batchData" :key="index" ></div>
    </div>
  </el-dialog>
</template>
<script>
import printOptionApi from "./printMinxin.js";
export default {
  mixins: [printOptionApi],
  data() {
    return {
      pageIndex: 1,
    };
  },
  watch: {
    pageIndex: {
      handler(val) {
        this.scrollpage(val);
      },
      deep: true,
      immediate: true
    },
  },
  methods: {
    scrollpage() {
      let index = this.pageIndex - 1 < 0 ? 0 : this.pageIndex - 1;
      this.$nextTick(() => {
        let dom = this.$refs.tsPrint
        if (dom) {
          dom.scrollTo({
            top: (window.document.body.clientHeight - 10) * index,
            behavior: "smooth",
          });
        }
      });
    },
    onOpen() {
      this.initData('batch')
    },
  },
};
</script>
<style lang="scss" scoped>
.main {
  overflow: auto !important;
  .print-content {
    background: white;
    padding: 40px 30px;
    margin: 20px auto 0;
    border: 1px solid #ccc;
    height: calc(100% - 10px);
    border-radius: 4px;
    width: 776px;
    height: 100%;
    overflow: auto;
  }
}
.header-page {
  vertical-align: middle;
  min-width: 300px;
  .el-button {
    margin: 0 10px;
  }
  .page-size {
    vertical-align: middle;
    margin-left: 10px;
    font-size: 18px;
    font-weight: 400;
    color: #333;
  }
}
</style>
