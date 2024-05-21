<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main flow-form-main">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack" :content="!dataForm.id ? '新建' : '编辑'" />
        <div class="options">
          <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
            {{$t('common.confirmButton')}}</el-button>
          <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <div>
        <el-alert title="提醒方式设置在【消息中心】-【消息发送配置】维护；选择默认则站内信提醒，选择自定义则取自定义模板配置。" type="warning"
          :closable="false" show-icon>
        </el-alert>
      </div>
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
        label-width="100px" class="main">
        <jnpf-form-tip-item label="标题" prop="title">
          <el-input v-model="dataForm.title" placeholder="请输入公告标题" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="用户" prop="toUserIds">
          <JnpfUsersSelect v-model="toUserIds" placeholder="全部用户" multiple clearable />
        </jnpf-form-tip-item>
        <el-row>
          <el-col :span="12">
            <jnpf-form-tip-item label="类型" prop="category">
              <el-select v-model="dataForm.category" placeholder="请选择类型" clearable filterable
                :key="key">
                <el-option v-for="(item,index) in categoryList" :key="index" :label="item.fullName"
                  :value="item.enCode">
                </el-option>
              </el-select>
            </jnpf-form-tip-item>
          </el-col>
          <el-col :span="12">
            <jnpf-form-tip-item label="失效时间" prop="expirationTime" tipLabel='当前时间超过失效时间，状态更新已过期'>
              <el-date-picker v-model="dataForm.expirationTime" type="datetime"
                placeholder="请选择失效时间" value-format="timestamp">
              </el-date-picker>
            </jnpf-form-tip-item>
          </el-col>
        </el-row>
        <jnpf-form-tip-item label="附件" prop="files">
          <JnpfUploadFile v-model="files" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="封面图片" prop="coverImage" tipLabel='门户公告通知缩略图展示，无设置则系统默认图片'>
          <single-img v-model="dataForm.coverImage" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="内容" prop="bodyText">
          <JnpfEditor v-model="dataForm.bodyText" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="摘要" prop="excerpt">
          <el-input v-model="dataForm.excerpt" placeholder="请输入摘要" />
        </jnpf-form-tip-item>
        <el-row>
          <el-col :span="12">
            <jnpf-form-tip-item label="提醒方式" prop="remindCategory">
              <el-select v-model="dataForm.remindCategory" placeholder="请选择提醒方式" clearable
                filterable>
                <el-option v-for="(item,index) in remindCategoryList" :key="index"
                  :label="item.fullName" :value="item.enCode">
                </el-option>
              </el-select>
            </jnpf-form-tip-item>
          </el-col>
          <el-col :span="12" v-if="dataForm.remindCategory == 2">
            <jnpf-form-tip-item label="发送配置" prop="sendConfigId">
              <msg-dialog :value="dataForm.sendConfigId" :title="dataForm.sendConfigName"
                @change="onMsgChange" :messageSource="1" :key="key" />
            </jnpf-form-tip-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </transition>
</template>
<script>
import { createNotice, updateNotice, getNoticeInfo, sendMessageConfig } from '@/api/system/message'
import MsgDialog from "@/components/Process/PropPanel/msgDialog";
import singleImg from '@/components/Upload/SingleImg'
export default {
  components: { MsgDialog, singleImg },
  data() {
    return {
      uploadHeaders: { Authorization: this.$store.getters.token },
      visible: false,
      formLoading: false,
      btnLoading: false,
      key: +new Date(),
      dataForm: {
        id: '',
        toUserIds: '',
        title: '',
        files: '',
        bodyText: '',
        coverImage: '',
        remindCategory: 1,
        category: '1',
        sendConfigId: '',
        sendConfigName: '',
        expirationTime: null,
        excerpt: ""
      },
      sendConfiguredList: [],
      remindCategoryList: [{ 'fullName': '默认', 'enCode': 1 }, { 'fullName': '自定义', 'enCode': 2 }, { 'fullName': '不提醒', 'enCode': 3 }],
      categoryList: [],
      toUserIds: [],
      files: [],
      dataRule: {
        title: [
          { required: true, message: '公告标题不能为空', trigger: 'blur' }
        ],
        sendConfigId: [
          { required: true, message: '发送配置不能为空', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '类型不能为空', trigger: 'blur' }
        ],
        remindCategory: [
          { required: true, message: '提醒方式不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    init(id) {
      this.dataForm.id = id || 0
      this.files = []
      this.toUserIds = []
      this.visible = true
      this.formLoading = true
      this.$nextTick(() => {
        // 获取公告类型
        this.$store.dispatch('base/getDictionaryData', { sort: 'NoticeType' }).then(res => {
          this.categoryList = res
        })
        this.$refs['dataForm'].resetFields()
        if (this.remindCategory != 1 && !this.dataForm.id) this.dataForm.sendConfigName = ""
        if (this.dataForm.id) {
          getNoticeInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
            this.files = res.data.files ? JSON.parse(res.data.files) : []
            this.toUserIds = res.data.toUserIds ? res.data.toUserIds.split(',') : []
            this.$nextTick(() => {
              if (this.dataForm.sendConfigId) this.sendMessageConfig(this.dataForm.sendConfigId)
            })
          })
        }
        this.formLoading = false
        this.key = +new Date()
      })
    },
    sendMessageConfig(id) {
      sendMessageConfig(id).then(res => {
        this.dataForm.sendConfigName = res.data.fullName
        this.key = +new Date()
      })
    },
    onMsgChange(id, item) {
      if (!id) {
        this.dataForm.sendConfigId = ''
        this.dataForm.sendConfigName = ''
        return
      }
      if (this.dataForm.sendConfigId === id) return
      this.dataForm.sendConfigId = id
      this.dataForm.sendConfigName = item.fullName
    },
    handleAvatarSuccess(res) {
      if (res.code === 200 && res.data && res.data.url) {
        this.dataForm.coverImage = res.data.url
      } else {
        this.$message.error('头像上传失败');
      }
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          this.dataForm.files = JSON.stringify(this.files)
          this.dataForm.toUserIds = this.toUserIds.join(',')
          const formMethod = this.dataForm.id ? updateNotice : createNotice
          formMethod(this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('refreshDataList')
                this.goBack()
              }
            })
          }).catch(() => {
            this.btnLoading = false
          })
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.JNPF-preview-main {
  >>> .avatar-uploader {
    .el-upload {
      border: 1px dashed #dcdfe6;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
    }
    .el-upload:hover {
      border-color: #409eff;
    }
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 130px;
    height: 130px;
    line-height: 130px;
    text-align: center;
  }
  .avatar {
    width: 130px;
    height: 130px;
    display: block;
  }
  .content {
    >>> {
      p {
        line-height: 30px;
        img {
          display: block;
          margin-right: auto;
          margin-left: auto;
        }
      }
    }
  }
  .vab-quill-content {
    >>> {
      .jnpf-form-tip-item__content {
        line-height: normal;
      }
    }
  }
}
.transfer-pane__body-tab {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .el-tabs__header {
    margin-bottom: 0;
    flex-shrink: 0;

    .el-tabs__nav {
      width: 100%;

      .el-tabs__item {
        width: 25%;
        text-align: center;
        padding: 0 20px;
      }
    }
  }

  &.hasSys-tab {
    .el-tabs__header .el-tabs__nav .el-tabs__item {
      width: 25%;
    }
  }

  .el-tabs__content {
    flex: 1;

    .el-tab-pane {
      height: 100%;
      padding: 10px;
      overflow: auto;

      .JNPF-common-el-tree {
        margin: 0;
      }
    }
  }
}
</style>