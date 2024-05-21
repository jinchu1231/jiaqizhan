<template>
  <el-dialog :title="!dataForm.id ? '添加' : '编辑'" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll append-to-body
    class="JNPF-dialog JNPF-dialog_center" width="600px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="80px"
      v-loading="formLoading" class="menuForm">
      <el-form-item label="门户名称" prop="fullName">
        <portalDialog :value="dataForm.portalId" :title="dataForm.fullName"
          :systemId="dataForm.systemId" @change="portalChange" :disabled="dataForm.id?true:false"
          :platform='dataForm.platform' />
      </el-form-item>
      <el-form-item label="门户分类" prop="category">
        <el-select v-model="dataForm.category" placeholder="请选择分类" disabled>
          <el-option :key="item.id" :label="item.fullName" :value="item.id"
            v-for="item in categoryList" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sortCode">
        <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
          controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="enabledMark">
        <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="说明" prop="bodyText">
        <el-input v-model="dataForm.description" type="textarea" :rows="4" placeholder="说明" />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { createPortal, updatePortal, getPortalManageInfo } from '@/api/system/portal'
import portalDialog from './portalDialog'
export default {
  components: { portalDialog },
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      dataForm: {
        id: '',
        portalId: '',
        fullName: '',
        category: '',
        enCode: '',
        homePageMark: 0,
        sortCode: 0,
        enabledMark: 1,
        description: '',
        systemId: '',//系统id
        platform: 'Web'
      },
      categoryList: [],
      dataRule: {
        fullName: [
          { required: true, message: '门户名称不能为空', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    init(id, systemId, categoryList, platform) {
      this.dataForm.id = id || ''
      this.categoryList = categoryList || []
      this.dataForm.systemId = systemId || ''
      this.dataForm.portalId = ''
      this.dataForm.category = ''
      this.dataForm.fullName = ''
      this.dataForm.description = ''
      this.dataForm.platform = ''
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        // 获取表单数据
        if (this.dataForm.id) {
          this.formLoading = true
          getPortalManageInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
            this.dataForm.category = this.dataForm.categoryId
            this.$nextTick(() => { this.formLoading = false })
          }).catch(() => { })
        } else {
          this.dataForm.platform = platform || 'Web'
        }
      })
    },
    portalChange(id, item) {
      if (!id) {
        this.dataForm.portalId = ''
        this.dataForm.category = ''
        this.dataForm.fullName = ''
        return
      }
      if (this.dataForm.portalId === id) return
      this.dataForm.portalId = id
      this.dataForm.category = item.categoryId
      this.dataForm.fullName = item.fullName
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          const formMethod = this.dataForm.id ? updatePortal : createPortal
          formMethod(this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('refreshDataList', true)
              }
            })
          }).catch(() => {
            this.btnLoading = false
          })
        }
      })
    },
  }
}
</script>
