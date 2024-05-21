<template>
  <div class="main">
    <template v-if="config.popupType==='dialog'">
      <el-dialog :title="config.popupTitle" :close-on-click-modal="false" :visible.sync="visible"
        class="JNPF-dialog JNPF-dialog_center" lock-scroll append-to-body :width='config.popupWidth'
        @close="$emit('close')">
        <parser :form-conf="formConf" @submit="submitForm" :key="key" ref="dynamicForm"
          v-if="!loading" />
        <span slot="footer" class="dialog-footer">
          <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
          <el-button type="primary" @click="dataFormSubmit" :loading="btnLoading">
            {{$t('common.confirmButton')}}</el-button>
        </span>
      </el-dialog>
    </template>
    <template v-if="config.popupType==='drawer'">
      <el-drawer :title="config.popupTitle" :visible.sync="visible" :wrapperClosable="false"
        ref="drawer" :size='config.popupWidth' append-to-body class="JNPF-common-drawer"
        @close="$emit('close')">
        <div class="JNPF-flex-main">
          <div class="dynamicForm">
            <parser :form-conf="formConf" @submit="submitForm" :key="key" ref="dynamicForm"
              v-if="!loading" />
          </div>
          <div class="drawer-footer">
            <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
            <el-button type="primary" @click="dataFormSubmit" :loading="btnLoading">
              {{$t('common.confirmButton')}}</el-button>
          </div>
        </div>
      </el-drawer>
    </template>
  </div>
</template>
<script>
import Parser from '@/components/Generator/parser/Parser'
import { getConfigData, getModelInfo, createModel } from '@/api/onlineDev/visualDev'
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
import { mapGetters } from "vuex";
export default {
  name: 'popup',
  components: { Parser },
  props: {},
  data() {
    return {
      visible: false,
      btnLoading: false,
      loading: false,
      config: {},
      formConf: {},
      formData: {},
      isPreview: false,
      key: +new Date(),
      id: "",
      dataForm: {
        id: "",
        data: ""
      },
      modelId: "",
      isAdd: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
  },
  methods: {
    init(config, modelId, id, isPreview = true, row) {
      this.loading = true
      this.config = config
      this.id = id
      this.isPreview = isPreview
      this.modelId = modelId
      this.isAdd = true
      this.$nextTick(() => {
        if (this.config.modelId) this.getConfigData(row)
      })
    },
    getConfigData(row) {
      getConfigData(this.config.modelId).then(res => {
        if (res.code !== 200 || !res.data) {
          this.$message({
            message: res.msg || '请求出错，请重试',
            type: 'error',
            duration: 1500,
          })
          return
        }
        this.formConf = JSON.parse(res.data.formData)
        const setDataFun = (formData) => {
          if (this.config.formOptions.length) {
            for (let k in formData) {
              for (let i = 0; i < this.config.formOptions.length; i++) {
                const e = this.config.formOptions[i]
                if (e.currentField == '@formId') this.formData[e.field] = formData.id;
                if (e.currentField == k) this.formData[e.field] = formData[k];
              }
            }
          }
          this.fillFormData(this.formConf, this.formData)
          this.$nextTick(() => {
            this.visible = true
            this.loading = false
          })
        }
        if (this.id) {
          getModelInfo(this.modelId, this.id).then(res => {
            let dataForm = res.data
            if (!dataForm.data) return
            const formData = JSON.parse(dataForm.data)
            this.formData = {}
            setDataFun({ ...formData, id: this.id })
          })
        } else {
          const formData = row
          setDataFun(formData)
        }
      }).catch(() => { })
    },
    fillFormData(form, data) {
      const loop = (list, parent) => {
        for (let i = 0; i < list.length; i++) {
          let item = list[i]
          if (item.__vModel__) {
            let val = data.hasOwnProperty(item.__vModel__) ? data[item.__vModel__] : item.__config__.defaultValue
            if (!item.__config__.isSubTable) item.__config__.defaultValue = val
            if (this.isAdd || item.__config__.isSubTable) {//新增时候，默认当前
              if (item.__config__.defaultCurrent) {
                if (item.__config__.jnpfKey === 'datePicker') {
                  let format = item.format
                  let dateStr = this.jnpf.toDate(new Date().getTime(), format)
                  let time = format === 'yyyy' ? '-01-01 00:00:00' : format === 'yyyy-MM' ? '-01 00:00:00' : format === 'yyyy-MM-dd' ?
                    ' 00:00:00' : ''
                  item.__config__.defaultValue = new Date(dateStr + time).getTime()
                }
                if (item.__config__.jnpfKey === 'timePicker') {
                  item.__config__.defaultValue = this.jnpf.toDate(new Date(), item.format)
                }
                if (item.__config__.jnpfKey === 'organizeSelect' && (this.userInfo.organizeIdList instanceof Array && this.userInfo.organizeIdList.length > 0)) {
                  item.__config__.defaultValue = item.multiple == true ? [this.userInfo.organizeIdList] : this.userInfo.organizeIdList
                }
                if (item.__config__.jnpfKey === 'depSelect' && this.userInfo.departmentId) {
                  item.__config__.defaultValue = item.multiple ? [this.userInfo.departmentId] : this.userInfo.departmentId;
                }
                if (item.__config__.jnpfKey === 'posSelect' && (this.userInfo.positionIds instanceof Array && this.userInfo.positionIds.length > 0)) {
                  item.__config__.defaultValue = item.multiple ? this.userInfo.positionIds.map(o => o.id) : this.userInfo.positionIds[0].id
                }
                if (item.__config__.jnpfKey === 'roleSelect' && (this.userInfo.roleIds instanceof Array && this.userInfo.roleIds.length > 0)) {
                  item.__config__.defaultValue = item.multiple ? this.userInfo.roleIds : this.userInfo.roleIds[0];
                }
                if (item.__config__.jnpfKey === 'groupSelect' && (this.userInfo.groupIds instanceof Array && this.userInfo.groupIds.length > 0)) {
                  item.__config__.defaultValue = item.multiple ? this.userInfo.groupIds : this.userInfo.groupIds[0];
                }
                if (['userSelect'].includes(item.__config__.jnpfKey) && this.userInfo.userId) {
                  item.__config__.defaultValue = item.multiple ? [this.userInfo.userId] : this.userInfo.userId;
                }
                if (item.__config__.jnpfKey === 'usersSelect' && this.userInfo.userId) {
                  item.__config__.defaultValue = item.multiple ? [this.userInfo.userId + '--user'] : this.userInfo.userId + '--user';
                }
                if (item.__config__.jnpfKey == 'sign' && this.userInfo.signImg) item.__config__.defaultValue = this.userInfo.signImg
              }
            }
          }
          if (!this.isPreview && this.useFormPermission) {
            let id = item.__config__.isSubTable ? parent.__vModel__ + '-' + item.__vModel__ : item.__vModel__
            let noShow = true
            if (this.formOperates && this.formOperates.length) {
              noShow = !this.formOperates.some(o => o.enCode === id)
            }
            noShow = item.__config__.noShow ? item.__config__.noShow : noShow
            this.$set(item.__config__, 'noShow', noShow)
          }
          if (item.__config__ && item.__config__.children && Array.isArray(item.__config__.children)) {
            loop(item.__config__.children, item)
          }
        }
      }
      loop(form.fields)
      form.formData = data
    },
    submitForm(data, callback) {
      console.log(data)
      if (!data) return
      this.btnLoading = true
      const successFun = (res, callback) => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            if (callback && typeof callback === "function") callback()
            this.visible = false
            this.btnLoading = false
          }
        })
      }
      if (this.config.customBtn) {
        if (this.config.templateJson && this.config.templateJson.length) {
          this.config.templateJson.forEach((ele) => {
            const value = data[ele.relationField] || data[ele.relationField] == 0 || data[ele.relationField] == false ? data[ele.relationField] : '';
            ele.defaultValue = (ele.relationField === '@formId' && ele.sourceType == 1) ? data.id : ele.sourceType == 1 ? value : ele.relationField;
          })
        }
        const query = {
          paramList: this.config.templateJson || [],
        }
        getDataInterfaceRes(this.config.interfaceId, query).then(res => {
          successFun(res, callback)
        }).catch(() => { this.btnLoading = false })
      } else {
        const formData = { ...this.formData, ...data }
        this.dataForm.data = JSON.stringify(formData)
        createModel(this.config.modelId, this.dataForm).then(res => {
          successFun(res, callback)
        }).catch(() => { this.btnLoading = false })
      }
    },
    dataFormSubmit() {
      if (this.isPreview) return this.$message({ message: '功能预览不支持数据保存', type: 'warning' })
      this.$refs.dynamicForm && this.$refs.dynamicForm.submitForm()
    }
  }
}
</script>
