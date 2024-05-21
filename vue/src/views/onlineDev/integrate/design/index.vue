<template>
  <el-dialog :visible.sync="visible" fullscreen lock-scroll class="JNPF-full-dialog"
    :show-close="false" :modal="false" append-to-body destroy-on-close>
    <div class="JNPF-full-dialog-header">
      <div class="header-title">
        <img src="@/assets/images/jnpf.png" class="header-logo" />
        <p class="header-txt" :title="title"> · {{title}}</p>
      </div>
      <div class="steps header-txt">系统集成设计</div>
      <div class="options">
        <el-button type="primary" @click="dataFormSubmit()" :disabled="loading"
          :loading="btnLoading">保 存</el-button>
        <el-button @click="closeDialog(true)">{{$t('common.cancelButton')}}</el-button>
      </div>
    </div>
    <div class="main" v-loading="loading">
      <Process ref="processDesign" :conf="templateJson" :type='dataForm.type' :key="key"
        :formInfo='dataForm' />
    </div>
  </el-dialog>
</template>

<script>
import { update } from '@/api/onlineDev/integrate'
import Process from "@/components/integrationProcess"

export default {
  components: { Process },
  data() {
    return {
      visible: false,
      activeStep: 0,
      dataForm: {
        id: '',
        templateJson: null
      },
      btnLoading: false,
      templateJson: {},
      loading: false,
      title: '',
      key: + new Date(),
    }
  },
  methods: {
    init(item) {
      this.dataForm.id = item.id || ''
      this.key = + new Date()
      this.visible = true
      this.loading = true
      this.$nextTick(() => {
        if (this.dataForm.id) {
          this.dataForm = item
          this.title = this.dataForm.fullName
          this.loading = false
        }
      })
    },
    dataFormSubmit() {
      this.$refs.processDesign && this.$refs.processDesign.getData().then(res => {
        this.dataForm = res.data
        this.dataForm.templateJson = JSON.stringify(res.data.templateJson)
        this.submit()
      }).catch(err => { err.msg && this.$message.warning(err.msg) })
    },
    submit() {
      this.btnLoading = true
      update(this.dataForm).then((res) => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            this.btnLoading = false
            this.closeDialog(true)
          }
        })
      }).catch(() => { this.btnLoading = false })
    },
    closeDialog(isRefresh) {
      this.visible = false
      this.$emit('close', isRefresh)
    },
  }
}
</script>