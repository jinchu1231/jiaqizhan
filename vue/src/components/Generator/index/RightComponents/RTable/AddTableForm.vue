<template>
  <el-dialog title="操作按钮" :close-on-click-modal="false" append-to-body :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center" lock-scroll width="600px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
      label-width="80px" label-position="left">
      <el-form-item label="按钮名称" prop="label">
        <el-input v-model="dataForm.label" placeholder="请输入按钮名称"
          :disabled="['copy', 'remove'].includes(dataForm.value)" :maxlength='10' />
      </el-form-item>
      <el-form-item label="是否显示">
        <el-switch v-model="dataForm.show" />
      </el-form-item>
      <el-form-item label="按钮类型" v-if="!['copy', 'remove'].includes(dataForm.value)">
        <el-select v-model="dataForm.btnType" placeholder="请选择按钮类型">
          <template v-for="(item,i) in butTypeOptions">
            <el-option :key="i" :label="item.fullName" :value="item.id" />
          </template>
        </el-select>
      </el-form-item>
      <el-form-item label="图标" v-if="!['copy', 'remove'].includes(dataForm.value)">
        <el-input v-model="dataForm.btnIcon" placeholder="请输入图标名称">
          <el-button slot="append" @click="openIconsDialog">
            选择
          </el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="删除确认" v-if="['remove', 'batchRemove'].includes(dataForm.value)">
        <el-switch v-model="dataForm.showConfirm" />
      </el-form-item>
      <template v-if="!['copy', 'remove', 'add', 'batchRemove'].includes(dataForm.value)">
        <JnpfGroupTitle content='动作设置' class="GroupTitle" />
        <el-form-item label="弹窗标题">
          <el-input v-model="actionConfig.popupTitle" placeholder="请输入弹窗标题" />
        </el-form-item>
        <el-form-item label="弹窗类型">
          <el-radio-group v-model="actionConfig.popupType">
            <el-radio-button label="dialog">居中弹窗</el-radio-button>
            <el-radio-button label="drawer">右侧弹窗</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="弹窗宽度">
          <el-select v-model="actionConfig.popupWidth" placeholder="请选择弹窗宽度" filterable>
            <el-option v-for="item in popupWidthOptions" :label="item" :value="item" :key="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据来源">
          <el-radio-group v-model="actionConfig.dataSource" @change="handleNull">
            <el-radio-button label="dynamic">远端数据</el-radio-button>
            <el-radio-button label="relationForm">表单数据</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="actionConfig.dataSource=='dynamic'">
          <el-form-item label="远端数据" prop="interfaceName">
            <interface-dialog :value="actionConfig.interfaceId" :title="actionConfig.interfaceName"
              @change="onInterfaceChange" :hasPage='1' />
          </el-form-item>
          <el-form-item label="参数设置" style="margin-bottom: 0;"></el-form-item>
          <el-table :data="actionConfig.templateJson" style="margin-bottom: 18px;">
            <el-table-column type="index" width="50" label="序号" align="center" />
            <el-table-column prop="field" label="参数名称" width="200">
              <template slot-scope="scope">
                <span class="required-sign">{{scope.row.required?'*':''}}</span>
                {{scope.row.fieldName?scope.row.field+'('+scope.row.fieldName+')':scope.row.field}}
              </template>
            </el-table-column>
            <el-table-column prop="value" label="表单字段">
              <template slot-scope="scope">
                <el-select v-model="scope.row.relationField" placeholder="请选择表单字段" clearable
                  filterable @change="onRelationFieldChange($event,scope.row)">
                  <el-option v-for="item in formFieldsOptions" :key="item.__vModel__"
                    :label="item.__config__.label?item.__vModel__+'('+item.__config__.label+')':item.__vModel__"
                    :value="item.__vModel__">
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-form-item label="表单数据" v-else>
          <JnpfTreeSelect :options="treeData" v-model="actionConfig.modelId" placeholder="请选择关联功能"
            lastLevel clearable @change="onModeIdChange" />
        </el-form-item>
        <el-form-item label="设置列表字段" label-width="100px" style="margin-bottom: 0;"></el-form-item>
        <el-table :data="actionConfig.columnOptions">
          <el-table-column type="index" width="50" label="序号" align="center" />
          <el-table-column prop="field" label="列名" v-if="actionConfig.dataSource=='dynamic'">
            <template slot-scope="scope">
              <el-input v-model="scope.row.label" placeholder="请输入列名"></el-input>
            </template>
          </el-table-column>
          <el-table-column prop="value" label="字段">
            <template slot-scope="scope">
              <el-autocomplete class="inline-input" v-model="scope.row.value"
                v-if="actionConfig.dataSource=='dynamic'" :fetch-suggestions="querySearch"
                placeholder="请输入字段" style="width:100%" @select="handleSelect($event,scope.row)">
                <template slot-scope="{ item }">
                  <div class="sale-order-popper-item">
                    <span>{{ item.defaultValue}}</span>
                  </div>
                </template>
              </el-autocomplete>
              <el-select v-model="scope.row.value" placeholder="请选择" filterable style="width:100%"
                @change="headValue(scope.$index,$event)" v-else>
                <el-option v-for="item in fieldOptions" :key="item.vmodel" :label="item.label"
                  :value="item.vmodel" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="50">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="JNPF-table-delBtn"
                @click="handleDel(scope.$index,'columnOptions')">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="table-actions" @click="addColumn" style="margin-bottom: 18px;">
          <el-button type="text" icon="el-icon-plus">新增</el-button>
        </div>
        <el-form-item label="设置关联字段" label-width="100px" style="margin-bottom: 0;"></el-form-item>
        <el-table :data="actionConfig.relationOptions">
          <el-table-column type="index" width="50" label="序号" align="center" />
          <el-table-column prop="field" label="目标表单字段">
            <template slot-scope="scope">
              <el-select v-model="scope.row.field" placeholder="请选择表单字段" filterable>
                <el-option v-for="item in childList" :key="item.__vModel__"
                  :label="item.__config__.label" :value="item.__vModel__">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="映射来源" width="120">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" placeholder="请选择映射来源"
                @change="onTypeChange(scope.row)">
                <el-option label="字段" :value="1" />
                <el-option label="自定义" :value="2" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="value" label="数据来源字段/值">
            <template slot-scope="scope">
              <el-autocomplete class="inline-input" v-model="scope.row.value"
                v-if="scope.row.type==1 &&actionConfig.dataSource=='dynamic'"
                :fetch-suggestions="querySearch" placeholder="请输入字段" style="width:100%"
                @select="handleSelect($event,scope.row)">
                <template slot-scope="{ item }">
                  <div class="sale-order-popper-item">
                    <span>{{ item.defaultValue}}</span>
                  </div>
                </template>
              </el-autocomplete>
              <el-select v-model="scope.row.value" placeholder="请选择" filterable style="width:100%"
                v-else-if="scope.row.type==1 &&actionConfig.dataSource=='relationForm'">
                <el-option v-for="item in fieldOptions" :key="item.vmodel" :label="item.label"
                  :value="item.vmodel" />
              </el-select>
              <el-input v-model="scope.row.value" placeholder="请输入值" v-else />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="50">
            <template slot-scope="scope">
              <el-button size="mini" type="text" class="JNPF-table-delBtn"
                @click="handleDel(scope.$index,'relationOptions')">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="table-actions" @click="addRelationOption" style="margin-bottom: 18px;">
          <el-button type="text" icon="el-icon-plus">新增</el-button>
        </div>
      </template>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
    <iconBox :visible.sync="iconsVisible" :current="dataForm.btnIcon" @choiceIcon="setIcon" />
  </el-dialog>
</template>

<script>
import { getDrawingList } from '@/components/Generator/utils/db'
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'
import { getDataInterfaceInfo } from '@/api/systemData/dataInterface'
import iconBox from '@/components/JNPF-iconBox'
import { getFeatureSelector, getFormDataFields } from '@/api/onlineDev/visualDev'
import { systemComponentsList, noAllowRelationList } from '@/components/Generator/generator/comConfig'
const noFieldList = ['relationForm', 'popupSelect']
const butTypeOptions = [
  { fullName: '默认按钮', id: '' },
  { fullName: '主要按钮', id: 'primary' },
  { fullName: '成功按钮', id: 'success' },
  { fullName: '信息按钮', id: 'info' },
  { fullName: '警告按钮', id: 'warning' },
  { fullName: '危险按钮', id: 'danger' },
  { fullName: '文字按钮', id: 'text' },
]
export default {
  components: { InterfaceDialog, iconBox },
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      childList: [],
      dataForm: {
        value: '',
        label: '',
        show: false,
        showConfirm: false,
        btnIcon: '',
        btnType: '',
      },
      dataRule: {
        label: { required: true, message: '按钮名称不能为空', trigger: 'blur' },
      },
      popupWidthOptions: ['600px', '800px', '1000px', '40%', '50%', '60%', '70%', '80%'],
      fieldJsonList: [],
      butTypeOptions,
      iconsVisible: false,
      actionConfig: {
        dataSource: '',
        interfaceId: '',
        interfaceName: '',
        templateJson: [],
        hasPage: true,
        pageSize: 20,
        columnOptions: [],
        relationOptions: []
      },
      treeData: [],
      fieldOptions: []
    }
  },
  computed: {
    formFieldsOptions() {
      let list = []
      const loop = (data, parent) => {
        if (!data) return
        if (data.__config__ && data.__config__.jnpfKey !== 'table' && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data)
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent))
        if (data.__vModel__ && !noAllowRelationList.includes(data.__config__.jnpfKey)) list.push(data)
      }
      loop(getDrawingList())
      return list
    }
  },
  methods: {
    init(item, actionConfig, children) {
      this.visible = true
      this.getFeatureSelector()
      this.getFieldOptions()
      this.dataForm = { ...this.dataForm, ...item }
      this.actionConfig = {}
      this.$refs.dataForm && this.$refs.dataForm.clearValidate()
      if (['copy', 'remove', 'add', 'batchRemove'].includes(item.value)) return
      this.actionConfig = { ...this.actionConfig, ...actionConfig }
      this.childList = children.filter(o => o.__vModel__ && ![...systemComponentsList, ...noFieldList, ...noAllowRelationList].includes(o.__config__.jnpfKey))
      this.getDataInterfaceInfo()
      this.getFieldOptions()
    },
    getFeatureSelector() {
      getFeatureSelector({ type: 1, webType: '2', isRelease: 1 }).then(res => {
        this.treeData = res.data.list
      })
    },
    getFieldOptions() {
      if (!this.actionConfig.modelId) return
      getFormDataFields(this.actionConfig.modelId, 1).then(res => {
        this.fieldOptions = res.data.list || []
      })
    },
    onModeIdChange(val) {
      if (!val) return this.fieldOptions = []
      this.actionConfig.templateJson = this.actionConfig.templateJson ? this.actionConfig.templateJson.map(o => ({
        ...o,
        relationField: '',
        value: ''
      })) : []
      this.getFieldOptions()
    },
    onInterfaceChange(id, row) {
      if (!id) return this.handleNull()
      if (this.dataForm.interfaceId === id) return
      this.actionConfig.interfaceId = id
      this.actionConfig.interfaceName = row.fullName
      this.actionConfig.templateJson = row.templateJson ? row.templateJson.map(o => ({
        ...o,
        relationField: '',
        value: ''
      })) : []
      this.getDataInterfaceInfo()
    },
    handleNull() {
      this.actionConfig.interfaceId = ''
      this.actionConfig.interfaceName = ''
      this.actionConfig.templateJson = []
      this.fieldJsonList = []
      this.actionConfig.modelId = ''
      this.actionConfig.columnOptions = []
      this.actionConfig.templateJson = this.actionConfig.templateJson ? this.actionConfig.templateJson.map(o => ({
        ...o,
        relationField: '',
        value: ''
      })) : []
    },
    onRelationFieldChange(val, row) {
      if (!val) return row.jnpfKey = ''
      let list = this.formFieldsOptions.filter(o => o.__vModel__ === val)
      if (!list.length) return row.jnpfKey = ''
      let item = list[0]
      row.jnpfKey = item.__config__.jnpfKey
    },
    addColumn() {
      this.actionConfig.columnOptions.push({
        value: '',
        label: ''
      })
    },
    addRelationOption() {
      this.actionConfig.relationOptions.push({
        value: '',
        field: '',
        type: 1
      })
    },
    handleDel(index, key) {
      this.actionConfig[key].splice(index, 1)
    },
    onTypeChange(row) {
      row.value = ''
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.$emit('submit', this.dataForm, this.actionConfig)
          this.visible = false
        }
      })
    },
    getDataInterfaceInfo() {
      getDataInterfaceInfo(this.dataForm.interfaceId).then(res => {
        this.fieldJsonList = res.data && res.data.fieldJson && (JSON.parse(res.data.fieldJson) || []) || []
      })
    },
    querySearch(queryString, cb) {
      var restaurants = this.fieldJsonList || [];
      var results = queryString ? restaurants.filter(this.createStateFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createStateFilter(queryString) {
      return (state) => {
        return (state.defaultValue.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
      };
    },
    handleSelect(item, field) {
      field.value = item.defaultValue
    },
    openIconsDialog() {
      this.iconsVisible = true
    },
    setIcon(val) {
      this.dataForm.btnIcon = val
    },
    headValue(index, val) {
      if (!val) return this.actionConfig.columnOptions[index].label = ''
      let item = this.fieldOptions.filter(o => o.vmodel == val)[0]
      this.actionConfig.columnOptions[index].label = item.label
    },
  }
}
</script>
<style lang="scss" scoped>
.GroupTitle {
  border-bottom: unset !important;
}
</style>
