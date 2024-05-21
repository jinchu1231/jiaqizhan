<template>
  <div>
    <template v-if="formData.popupType==='general'">
      <el-dialog title="详情" :close-on-click-modal="false" :visible.sync="visible"
        class="JNPF-dialog JNPF-dialog_center" lock-scroll :width="formData.generalWidth"
        append-to-body>
        <div class="dynamicDetail" v-loading="loading || mainLoading">
          <Parser :formConf="formData" @toDetail="toDetail" v-if="!loading"
            :formValue="formValue" />
        </div>
        <span slot="footer" class="dialog-footer">
          <template v-if="formData.hasPrintBtn && formData.printId">
            <el-button type="primary" @click="print">
              {{formData.printButtonText||'打 印'}}
            </el-button>
          </template>
          <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
        </span>
      </el-dialog>
    </template>
    <template v-if="formData.popupType==='fullScreen'">
      <transition name="el-zoom-in-center">
        <div class="JNPF-preview-main">
          <div class="JNPF-common-page-header">
            <el-page-header @back="goBack" content="详情" />
            <div class="options">
              <template v-if="formData.hasPrintBtn && formData.printId">
                <el-button type="primary" @click="print">
                  {{formData.printButtonText||'打 印'}}
                </el-button>
              </template>
              <el-button @click="goBack">{{$t('common.cancelButton')}}</el-button>
            </div>
          </div>
          <div class="dynamic-form-main dynamicDetail"
            :style="{margin: '0 auto',width:formData.fullScreenWidth}"
            v-loading="loading || mainLoading">
            <Parser :formConf="formData" @toDetail="toDetail" v-if="!loading"
              :formValue="formValue" />
          </div>
        </div>
      </transition>
    </template>
    <template v-if="formData.popupType==='drawer'">
      <el-drawer title="详情" :visible.sync="visible" :wrapperClosable="false"
        :size='formData.drawerWidth' append-to-body class="JNPF-common-drawer">
        <div class="JNPF-flex-main">
          <div class="dynamicForm dynamicDetail" v-loading="loading || mainLoading">
            <Parser :formConf="formData" @toDetail="toDetail" v-if="!loading"
              :formValue="formValue" />
          </div>
          <div class="drawer-footer">
            <template v-if="formData.hasPrintBtn && formData.printId">
              <el-button type="primary" @click="print">
                {{formData.printButtonText||'打 印'}}
              </el-button>
            </template>
            <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
          </div>
        </div>
      </el-drawer>
    </template>
    <Detail v-if="detailVisible" ref="Detail" @close="detailVisible = false" />
    <print-browse :visible.sync="printBrowseVisible" :id="printId" :formId="dataForm.id" />
    <PrintDialog v-if="printDialogVisible" ref="printDialog" @change="printBrowseHandle">
    </PrintDialog>
  </div>
</template>

<script>
import PrintDialog from '@/components/PrintDialog'
import { getDataChange, getConfigData } from '@/api/onlineDev/visualDev'
import { deepClone } from '@/utils'
import Parser from './Parser'
import PrintBrowse from '@/components/PrintBrowse'
export default {
  name: 'Detail',
  components: { Parser, PrintBrowse, PrintDialog },
  data() {
    return {
      printId: '',
      printDialogVisible: false,
      visible: false,
      dataForm: {
        id: '',
        data: ''
      },
      modelId: '',
      formData: {},
      formValue: {},
      loading: true,
      mainLoading: false,
      detailVisible: false,
      useFormPermission: false,
      printBrowseVisible: false,
      formOperates: []
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    printBrowseHandle(id) {
      this.printDialogVisible = false
      this.printId = id;
      this.printBrowseVisible = true;
    },
    print() {
      if (this.isPreview) return this.$message({ message: '功能预览不支持打印', type: 'warning' })
      this.printDialogVisible = true
      this.$nextTick(() => {
        if (this.formData.printId.length == 1) {
          this.printBrowseHandle(this.formData.printId[0])
          return
        }
        this.$refs.printDialog.init(this.formData.printId)
      })
    },
    init(formData, modelId, id, useFormPermission) {
      this.formData = deepClone(formData)
      this.modelId = modelId
      this.useFormPermission = useFormPermission
      this.dataForm.id = id || ''
      this.getFormOperates()
      this.loading = true
      this.$nextTick(() => {
        if (this.dataForm.id) {
          let extra = {
            modelId,
            id,
            type: 2
          }
          this.$store.commit('generator/SET_DYNAMIC_MODEL_EXTRA', extra)
          getDataChange(modelId, this.dataForm.id).then(res => {
            this.dataForm = res.data
            if (!this.dataForm.data) return
            this.formValue = JSON.parse(this.dataForm.data)
            this.fillFormData(this.formData, this.formValue)
            this.initRelationForm(this.formData.fields)
            this.visible = true
          })
        } else {
          this.formValue = {}
          this.loading = false
          this.visible = false
          this.$emit('close')
        }
      })
    },
    initRelationForm(componentList) {
      componentList.forEach(cur => {
        const config = cur.__config__
        if (config.jnpfKey == 'relationFormAttr' || config.jnpfKey == 'popupAttr') {
          const relationKey = cur.relationField.split("_jnpfTable_")[0]
          componentList.forEach(item => {
            const noVisibility = Array.isArray(item.__config__.visibility) && !item.__config__.visibility.includes('pc')
            if ((relationKey == item.__vModel__) && (noVisibility || !!item.__config__.noShow) && !cur.__vModel__) {
              cur.__config__.noShow = true
            }
          })
        }
        if (cur.__config__.children && cur.__config__.children.length) this.initRelationForm(cur.__config__.children)
      })
    },
    toDetail(item) {
      if (!item.__config__.defaultValue) return
      this.mainLoading = true
      getConfigData(item.modelId).then(res => {
        this.mainLoading = false
        if (!res.data) return
        if (!res.data.formData) return
        let formData = JSON.parse(res.data.formData)
        formData.popupType = this.formData.popupType
        this.detailVisible = true
        this.$nextTick(() => {
          this.$refs.Detail.init(formData, item.modelId, item.__config__.defaultValue)
        })
      }).catch(() => { this.mainLoading = false })
    },
    getFormOperates() {
      if (!this.useFormPermission) return
      const permissionList = this.$store.getters.permissionList
      const modelId = this.$route.meta.modelId
      const list = permissionList.filter(o => o.modelId === modelId)
      this.formOperates = list[0] && list[0].form ? list[0].form : []
    },
    fillFormData(form, data) {
      const loop = (list, parent) => {
        for (let i = 0; i < list.length; i++) {
          let item = list[i]
          if (item.__vModel__) {
            if (item.__config__.jnpfKey === 'relationForm' || item.__config__.jnpfKey === 'popupSelect') {
              item.__config__.defaultValue = data[item.__vModel__ + '_id']
              this.$set(item, 'name', data[item.__vModel__] || '')
            } else {
              const val = data.hasOwnProperty(item.__vModel__) ? data[item.__vModel__] : item.__config__.defaultValue
              item.__config__.defaultValue = val
            }
            if (this.useFormPermission) {
              let id = item.__config__.isSubTable ? parent.__vModel__ + '-' + item.__vModel__ : item.__vModel__
              let noShow = true
              if (this.formOperates && this.formOperates.length) {
                noShow = !this.formOperates.some(o => o.enCode === id)
              }
              noShow = item.__config__.noShow ? item.__config__.noShow : noShow
              this.$set(item.__config__, 'noShow', noShow)
            }
          } else {
            if (['relationFormAttr', 'popupAttr'].includes(item.__config__.jnpfKey)) {
              item.__config__.defaultValue = data[item.relationField.split('_jnpfTable_')[0] + '_' + item.showField];
            }
          }
          if (item.__config__ && item.__config__.children && Array.isArray(item.__config__.children)) {
            loop(item.__config__.children, item)
          }
        }
      }
      loop(form.fields)
      this.loading = false
    }
  }
}
</script>