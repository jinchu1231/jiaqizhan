<template>
  <div>
    <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
      :visible.sync="visible" class="JNPF-dialog JNPF-dialog_center" lock-scroll width="600px">
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="loading"
        label-width="100px">
        <el-form-item label="权限组名称" prop="fullName">
          <el-input v-model="dataForm.fullName" placeholder="请输入权限组名称" maxlength="100">
          </el-input>
        </el-form-item>
        <el-form-item label="权限组编码" prop="enCode">
          <el-input v-model="dataForm.enCode" placeholder="请输入权限组编码" maxlength="50">
          </el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sortCode">
          <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
            controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="enabledMark">
          <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="dataForm.description" placeholder="请输入说明" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
        <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
          {{$t('common.confirmButton')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAuthorizeInfo, Update, Create } from '@/api/permission/authorize'
export default {
  components: {},
  data() {
    return {
      visible: false,
      loading: false,
      btnLoading: false,
      dataForm: {
        id: '',
        fullName: '',
        enCode: '',
        sortCode: 0,
        enabledMark: 1,
        description: ''
      },
      dataRule: {
        fullName: [
          { required: true, message: '权限组名称不能为空', trigger: 'blur' },
        ],
        enCode: [
          { required: true, message: '权限组编码不能为空', trigger: 'blur' },
          { validator: this.formValidate('enCode'), trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || ''
      this.visible = true
      this.btnLoading = false
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (this.dataForm.id) {
          this.loading = true
          getAuthorizeInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) return
        const formMethod = this.dataForm.id ? Update : Create
        formMethod(this.dataForm).then((res) => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.btnLoading = false
              this.visible = false
              this.$emit('close', true)
            }
          })
        }).catch(() => {
          this.btnLoading = false
        })
      })
    }
  }
}
</script>
