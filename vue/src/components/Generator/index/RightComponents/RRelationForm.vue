<template>
  <el-row>
    <el-form-item label="控件标题">
      <el-input v-model="activeData.__config__.label" placeholder="请输入控件标题" />
    </el-form-item>
    <el-form-item label="显示标题">
      <el-switch v-model="activeData.__config__.showLabel" />
    </el-form-item>
    <el-form-item label="标题提示">
      <el-input type="textarea" :rows="2" v-model="activeData.__config__.tipLabel"
        placeholder="请输入标题提示" />
    </el-form-item>
    <el-form-item label="占位提示">
      <el-input v-model="activeData.placeholder" placeholder="请输入占位提示" />
    </el-form-item>
    <el-form-item label="弹窗标题">
      <el-input v-model="activeData.popupTitle" placeholder="请输入弹窗标题" />
    </el-form-item>
    <el-form-item label="弹窗类型" v-show="showType==='pc'">
      <el-select v-model="activeData.popupType" placeholder="请选择弹窗类型">
        <el-option label="居中弹窗" value="dialog"></el-option>
        <el-option label="右侧弹窗" value="drawer"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="弹窗宽度" v-show="showType==='pc'">
      <el-select v-model="activeData.popupWidth" placeholder="请选择弹窗宽度" filterable>
        <el-option v-for="item in popupWidthOptions" :label="item" :value="item" :key="item" />
      </el-select>
    </el-form-item>
    <el-form-item label="关联功能">
      <JnpfTreeSelect :options="treeData" v-model="activeData.modelId" placeholder="请选择关联功能"
        lastLevel clearable @change="onModeIdChange" />
    </el-form-item>
    <el-form-item label="显示字段">
      <el-select v-model="activeData.relationField" placeholder="请选择显示字段"
        @visible-change="visibleChange" clearable filterable>
        <el-option v-for="item in fieldOptions" :key="item.vmodel" :label="item.label"
          :value="item.vmodel" />
      </el-select>
    </el-form-item>
    <el-form-item label="填充字段" v-if="modelType == 1 || modelType == 6">
      <div slot="label">填充字段
        <el-tooltip content="选择数据后，将关联表单的字段值填充到当前表单字段。" placement="top">
          <i class="el-icon-question tooltip-question"></i>
        </el-tooltip>
      </div>
      <el-button style="width: 100%;"
        @click="onChangeRule">{{activeData.__config__.transferList.length?'编辑填充规则':'填充规则配置'}}</el-button>
    </el-form-item>
    <el-divider>列表字段</el-divider>
    <draggable :list="activeData.columnOptions" :animation="340" group="selectItem"
      handle=".option-drag">
      <div v-for="(item, index) in activeData.columnOptions" :key="index" class="select-item">
        <div class="select-line-icon option-drag">
          <i class="icon-ym icon-ym-darg" />
        </div>
        <el-select v-model="item.value" placeholder="请选择显示字段" @visible-change="visibleChange"
          clearable @change="onChange($event,item)" filterable>
          <el-option v-for="item in fieldOptions" :key="item.vmodel" :label="item.label"
            :value="item.vmodel" />
        </el-select>
        <div class="close-btn select-line-icon" @click="activeData.columnOptions.splice(index, 1)">
          <i class="el-icon-remove-outline" />
        </div>
      </div>
    </draggable>
    <div style="margin-left: 29px;">
      <el-button style="padding-bottom: 0" icon="el-icon-circle-plus-outline" type="text"
        @click="addSelectItem">
        添加字段
      </el-button>
    </div>
    <el-divider>列表分页</el-divider>
    <el-form-item label="分页设置">
      <el-switch v-model="activeData.hasPage"></el-switch>
    </el-form-item>
    <el-form-item label="分页条数" label-width="80px" v-if="activeData.hasPage">
      <el-radio-group v-model="activeData.pageSize">
        <el-radio-button :label="20">20条</el-radio-button>
        <el-radio-button :label="50">50条</el-radio-button>
        <el-radio-button :label="100">100条</el-radio-button>
        <el-radio-button :label="500">500条</el-radio-button>
      </el-radio-group>
    </el-form-item>
    <el-divider />
    <el-form-item label="能否清空">
      <el-switch v-model="activeData.clearable" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="activeData.disabled" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="activeData.__config__.noShow" />
    </el-form-item>
    <el-form-item label="是否必填">
      <el-switch v-model="activeData.__config__.required" />
    </el-form-item>
    <RuleDialog ref="rule" v-if="ruleVisible" @confirm='onChangeFieldRule' />
  </el-row>
</template>
<script>
import { getFeatureSelector, getFormDataFields } from '@/api/onlineDev/visualDev'
import { getDrawingList } from '@/components/Generator/utils/db'
import { noAllowRelationList } from '@/components/Generator/generator/comConfig'
import draggable from 'vuedraggable'
import comMixin from './mixin'
import RuleDialog from './RuleDialog'
import { systemComponentsList } from '@/components/Generator/generator/comConfig'
const noFieldList = ['relationForm', 'popupSelect']
export default {
  props: ['activeData', 'modelType'],
  mixins: [comMixin],
  components: { draggable, RuleDialog },
  data() {
    return {
      treeData: [],
      fieldOptions: [],
      popupWidthOptions: ['600px', '800px', '1000px', '40%', '50%', '60%', '70%', '80%'],
      ruleVisible: false
    }
  },
  created() {
    this.getFeatureSelector()
    this.getFieldOptions()
  },
  computed: {
    formFieldsOptions() {
      let list = []
      const loop = (data, parent) => {
        if (!data) return
        if (data.__config__ && this.isIncludesTable(data) && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data)
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent))
        if (data.__vModel__ && data.__vModel__ !== this.activeData.__vModel__) {
          const isTableChild = parent && parent.__config__ && parent.__config__.jnpfKey === 'table'
          if (isTableChild && !this.isSameSource(data)) return;
          list.push({
            realVModel: isTableChild ? parent.__vModel__ + '-' + data.__vModel__ : data.__vModel__,
            realLabel: isTableChild ? parent.__config__.label + '-' + data.__config__.label : data.__config__.label,
            fullName: isTableChild ? parent.__config__.label + '-' + data.__config__.label : data.__config__.label,
            id: isTableChild ? parent.__vModel__ + '-' + data.__vModel__ : data.__vModel__,
            ...data
          })
        }
      }
      loop(getDrawingList())
      return list
    }
  },
  methods: {
    getFeatureSelector() {
      getFeatureSelector({ type: 1, webType: '1,2', isRelease: 1 }).then(res => {
        this.treeData = res.data.list
      })
    },
    getFieldOptions() {
      if (!this.activeData.modelId) return
      getFormDataFields(this.activeData.modelId, 1).then(res => {
        this.fieldOptions = res.data.list
      })
    },
    visibleChange(val) {
      if (!val) return
      if (!this.activeData.modelId) this.$message.warning('请先选择关联功能')
    },
    onModeIdChange(val) {
      this.activeData.relationField = ''
      this.$emit('relationChange', this.activeData.__vModel__)
      if (!val) {
        this.fieldOptions = []
        return
      }
      this.getFieldOptions()
    },
    onChange(val, item) {
      const list = this.fieldOptions.filter(o => o.vmodel === val) || []
      if (!list.length) return
      const active = list[0]
      item.label = active.label
    },
    addSelectItem() {
      this.activeData.columnOptions.push({
        value: '',
        label: ''
      })
    },
    onChangeRule() {
      if (!this.activeData.modelId) return this.$message.error('请先选择关联功能')
      this.ruleVisible = true
      let list = JSON.parse(JSON.stringify(this.formFieldsOptions))
      if (this.activeData.__config__.isSubTable) list = list.filter(o => o.__config__.isSubTable)
      list = list.filter(o => ![...systemComponentsList, ...noFieldList].includes(o.__config__.jnpfKey))
      this.$nextTick(() => {
        this.$refs.rule.init({ transferList: this.activeData.__config__.transferList, fieldList: this.fieldOptions, formFieldsList: list })
      })
    },
    onChangeFieldRule(val) {
      this.activeData.__config__.transferList = val
    },
    isIncludesTable(data) {
      if ((!data.__config__.layout || data.__config__.layout === 'rowFormItem') && data.__config__.jnpfKey !== 'table') return true
      if (this.activeData.__config__.isSubTable) return this.activeData.__config__.parentVModel === data.__vModel__
      return data.__config__.jnpfKey !== 'table'
    },
    isSameSource(data) {
      const isSubTable = this.activeData.__config__.isSubTable;
      if (isSubTable) return data.__config__.isSubTable && this.activeData.__config__.parentVModel === data.__config__.parentVModel;
      return !data.__config__.isSubTable;
    }
  }
}
</script>