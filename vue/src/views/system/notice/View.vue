<template>
  <el-dialog title="详情" :close-on-press-escape="false" :close-on-click-modal="false"
    :visible.sync="visible" class="JNPF-dialog JNPF-dialog_center JNPF-dialog-notice" lock-scroll
    width="80%">
    <div class="notice-wrapper" v-loading="loading">
      <div class="notice-hd">
        <h1 class="title">{{dataForm.title}}</h1>
        <div class="info">
          <span>{{dataForm.releaseTime | toDate() }}</span><span>{{dataForm.releaseUser}}</span>
        </div>
      </div>
      <p class="excerpt" v-if="dataForm.excerpt">{{ dataForm.excerpt }}</p>
      <div class="main">
        <div class="ql-editor" v-html="dataForm.bodyText" />
      </div>
      <div class="file-list" v-if="files.length">
        <JnpfUploadFile v-model="files" disabled detailed />
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { getNoticeInfo } from '@/api/system/message'
export default {
  data() {
    return {
      visible: false,
      loading: false,
      files: [],
      dataForm: {
        id: '',
        title: '',
        creatorUser: '',
        creatorTime: Number,
        bodyText: ''
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id
      this.dataForm.bodyText = ''
      this.visible = true
      this.loading = true
      this.$nextTick(() => {
        getNoticeInfo(this.dataForm.id).then(res => {
          this.dataForm = res.data || {}
          this.files = res.data.files ? JSON.parse(res.data.files) : []
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.main >>> a {
  color: #06c !important;
}
.main {
  margin-top: 0px !important;
  >>> .ql-editor {
    white-space: unset;
  }
}
.notice-wrapper {
  .notice-hd {
    padding: 24px 0;
    .title {
      color: #303133;
      margin-top: 0;
      margin-bottom: 8px;
    }
    .info {
      color: #606266;
      padding-bottom: 24px;
      line-height: normal;
    }
  }
  .excerpt {
    color: #303133;
    margin-bottom: 8px !important;
  }
}
</style>