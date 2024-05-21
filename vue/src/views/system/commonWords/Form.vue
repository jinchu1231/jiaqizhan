<template>
  <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
    :visible.sync="visible" class="JNPF-dialog JNPF-dialog_center" lock-scroll width="600px"
    @close='cancel()'>
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
      label-width="100px">
      <el-form-item label="常用语" prop="commonWordsText">
        <el-input v-model="dataForm.commonWordsText" placeholder="输入常用语" />
      </el-form-item>
      <el-form-item label="排序" prop="sortCode">
        <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
          controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="enabledMark">
        <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="cancel()">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()"
        :disabled='continueBtnLoading'>
        {{$t('common.confirmButton')}}</el-button>
      <el-button @click="dataFormSubmit(2)" type="primary" v-if="!dataForm.id"
        :loading="continueBtnLoading" :disabled='btnLoading'>
        确定并新增
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  getCommonWordsInfo,
  Create, Update
} from '@/api/system/commonWords'
import { getSystem } from "@/api/system/system";
export default {
  data() {
    return {
      visible: false,
      formLoading: false,
      treeLoading: false,
      btnLoading: false,
      continueBtnLoading: false,
      singleSelectTreeVal: "", //单选树默认label值
      singleSelectTreeKey: "", //单选树默认key值
      systemOptions: [],
      value: '',
      dataForm: {
        commonWordsText: '',
        sortCode: '',
        commonWordsType: 0,
        enabledMark: 1,
      },
      nodeId: '0',
      dataRule: {
        commonWordsText: [
          { required: true, message: '请输入常用语', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0
      this.visible = true
      this.formLoading = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.treeLoading = true
        getSystem({ enableMark: "1" }).then(res => {
          this.systemOptions = res.data.list || []
        })
        if (this.dataForm.id) {
          getCommonWordsInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
          })
        }
        this.formLoading = false
      })
    },
    cancel() {
      this.visible = false
      this.$emit('refreshDataList')
    },
    dataFormSubmit(type) {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          if (type == 2) {
            this.continueBtnLoading = true
          } else {
            this.btnLoading = true
          }
          const formMethod = this.dataForm.id ? Update : Create
          formMethod(this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                if (type == 2) {
                  this.$nextTick(() => {
                    this.$refs['dataForm'].resetFields()
                    this.continueBtnLoading = false
                  }
                  )
                } else {
                  this.visible = false
                  this.btnLoading = false
                  this.$emit('refreshDataList')
                }
              }
            })
          }).catch(() => {
            this.continueBtnLoading = false
            this.btnLoading = false
          })
        }
      })
    }
  }
}
</script>
