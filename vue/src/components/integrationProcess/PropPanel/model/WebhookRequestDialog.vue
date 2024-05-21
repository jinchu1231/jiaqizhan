<template>
  <el-dialog title="请求接口" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center webhook-request-dialog" lock-scroll width="600px"
    append-to-body destroy-on-close>
    <div class="main">
      <div class="mb-10px">向该接口发送请求，通过接口生成接口字段！</div>
      <el-input v-model="url" readonly>
        <template slot="append">
          <span class="cursor-pointer" @click="handleCopy(url,$event)">复制链接</span>
        </template>
      </el-input>
      <div class="mt-20px">
        <el-button type="primary" @click="handleRequest" v-if="!loading">接收请求</el-button>
        <template v-else>
          <el-alert title="请在3分钟内向URL发送一条GET或POST请求" type="warning" :closable='false' />
          <div class="tip" v-if="!isRetry">正在接收请求......</div>
          <template v-else>
            <div class="tip tip-error">当前URL没有接收到任何请求，是否重试？</div>
            <el-button type="primary" @click="handleRequest" class="mt-10px">重试</el-button>
          </template>
        </template>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="closeDialog()">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import clipboard from '@/utils/clipboard'
import { getWebhookParams, webhookStart } from '@/api/onlineDev/integrate';
import store from '@/store'
export default {
  data() {
    return {
      visible: false,
      url: '',
      loading: false,
      isRetry: false,
      timer: null,
      id: '',
      randomStr: ''
    }
  },
  computed: {
    sysConfig() {
      return store.getters && store.getters.sysConfig
    }
  },
  methods: {
    init(data) {
      this.visible = true
      this.url = this.sysConfig.jnpfDomain + data.url;
      this.randomStr = data.randomStr;
      this.id = data.id || '';
      this.loading = false;
      this.isRetry = false;
    },
    handleCopy(text, event) {
      clipboard(text, event)
    },
    handleRequest() {
      webhookStart(this.id, this.randomStr).then(_ => {
        this.loading = true;
        this.isRetry = false;
        this.timer = setInterval(() => {
          this.handleWebhookParams();
        }, 1000);
        setTimeout(() => {
          clearTimer();
          this.isRetry = true;
        }, 180000);
      });
    },
    clearTimer() {
      if (!this.timer) return;
      clearInterval(this.timer);
      this.timer = null;
    },
    handleWebhookParams() {
      getWebhookParams(this.randomStr).then(res => {
        if (res.data.length) {
          this.$emit('confirm', res.data);
          this.clearTimer();
          this.closeModal();
        }
      }).catch(() => {
        this.clearTimer();
        this.isRetry = true;
      });
    }
  }
}
</script>
<style lang="scss" scoped>
.webhook-request-dialog {
  >>> .el-dialog__body {
    padding: 20px 50px 0 !important;
  }
  .main {
    height: 300px;
    .tip {
      margin-top: 10px;
      height: 40px;
      line-height: 40px;
      padding: 0 15px;
      color: #1890ff;
      background-color: #ebeef5;
      &.tip-error {
        color: #ed6f6f;
      }
    }
    .mb-10px {
      margin-bottom: 10px;
    }
    .mt-20px {
      margin-top: 20px;
    }
    .mt-10px {
      margin-top: 10px;
    }
    .cursor-pointer {
      cursor: pointer;
    }
  }
}
</style>
