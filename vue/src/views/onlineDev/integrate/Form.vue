<template>
  <el-dialog :title="(!dataForm.id ? '新建' : '编辑')+title" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll append-to-body
    destroy-on-close class="JNPF-dialog JNPF-dialog_center" width="600px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="80px"
      v-loading="formLoading" class="menuForm">
      <el-form-item label="名称" prop="fullName">
        <el-input v-model="dataForm.fullName" placeholder="输入名称" />
      </el-form-item>
      <el-form-item label="编码" prop="enCode">
        <el-input v-model="dataForm.enCode" placeholder="输入编码" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="dataForm.type" placeholder="请选择类型" clearable disabled>
          <el-option label="事件触发" :value="1" />
          <el-option label="定时触发" :value="2" />
          <el-option label="webhook触发" :value="3" />
        </el-select>
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
      <el-button type="primary" :loading="btnLoading" :disabled='designBtnLoading'
        @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
      <el-button type="primary" :loading="designBtnLoading" :disabled='btnLoading'
        @click="dataFormSubmit(1)">
        确定并设计</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getInfo, update, create } from '@/api/onlineDev/integrate'
export default {
  components: {},
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      designBtnLoading: false,
      dataForm: {
        id: '',
        fullName: '',
        enCode: '',
        enabledMark: 1,
        type: 0,
        bodyText: '',
        templateJson: ""
      },
      title: '',
      categoryList: [],
      dataRule: {
        fullName: [
          { required: true, message: '名称不能为空', trigger: 'blur' },
        ],
        enCode: [
          { required: true, message: '编码不能为空', trigger: 'blur' },
          { validator: this.formValidate('enCode'), trigger: 'blur' },
        ],
        type: [
          { required: true, message: '类型不能为空', trigger: 'change' },
        ],
      }
    }
  },
  methods: {
    init(id, type) {
      this.visible = true
      this.dataForm.id = id || ''
      this.btnLoading = false
      this.designBtnLoading = false
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.dataForm.type = type || 1
        // 获取表单数据
        if (this.dataForm.id) {
          this.formLoading = true
          getInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
            this.$nextTick(() => { this.formLoading = false })
          }).catch(() => { })
        } else {
          this.dataForm.fullName = ''
          this.dataForm.enCode = ''
          this.dataForm.enabledMark = 1
          this.dataForm.templateJson = ""
          this.dataForm.description = ""
        }
      })
    },
    dataFormSubmit(type) {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) return
        if (type) {
          this.designBtnLoading = true
        } else {
          this.btnLoading = true
        }
        const formMethod = this.dataForm.id ? update : create
        formMethod(this.dataForm).then((res) => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.btnLoading = false
              this.designBtnLoading = false
              this.visible = false
              if (type == 1) return this.$emit('integrateDesign', res.data, this.dataForm.fullName)
              this.$emit('close', true)
            }
          })
        }).catch(() => {
          this.btnLoading = false
          this.designBtnLoading = false
        })
      })
    }
  }
}
</script>
