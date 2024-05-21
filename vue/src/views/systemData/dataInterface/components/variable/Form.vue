<template>
  <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll width="600px"
    class="JNPF-dialog JNPF-dialog_center" append-to-body>
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
      label-width="80px">
      <el-form-item label="变量名" prop="fullName">
        <el-input v-model="dataForm.fullName" placeholder="请输入变量名" />
      </el-form-item>
      <el-form-item label="JS表达式" prop="expression">
        <el-input v-model="dataForm.expression" type="textarea" :rows="3" placeholder="请输入JS表达式" />
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
import {
  getVariateDetail,
  createVariate,
  updateVariate
} from '@/api/systemData/dataInterface'
export default {
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      interfaceId: '',
      dataForm: {
        id: '',
        fullName: '',
        expression: ""
      },
      dataRule: {
        fullName: [
          { required: true, message: '请输入变量名', trigger: 'blur' },
        ],
        expression: [
          { required: true, message: '请输入JS表达式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    init(item, id) {
      this.visible = true
      this.dataForm.id = ''
      if (item) this.dataForm.id = item.id || ''
      this.formLoading = true
      this.dataForm.interfaceId = id
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (this.dataForm.id) {
          getVariateDetail(item.id).then(res => {
            this.dataForm = res.data
            this.formLoading = false
          })
        } else {
          this.formLoading = false
        }
      })
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          const formMethod = this.dataForm.id ? updateVariate : createVariate
          formMethod(this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('refreshDataList')
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
