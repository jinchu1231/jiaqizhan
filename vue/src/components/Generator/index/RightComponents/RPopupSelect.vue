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
    <el-form-item label="弹窗标题" v-if="activeData.__config__.jnpfKey==='popupSelect'">
      <el-input v-model="activeData.popupTitle" placeholder="请输入弹窗标题" />
    </el-form-item>
    <el-form-item label="弹窗类型"
      v-if="activeData.__config__.jnpfKey==='popupSelect'&&showType==='pc'">
      <el-select v-model="activeData.popupType" placeholder="请选择弹窗类型">
        <el-option label="居中弹窗" value="dialog"></el-option>
        <el-option label="右侧弹窗" value="drawer"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="弹窗宽度"
      v-if="activeData.__config__.jnpfKey==='popupSelect'&&showType==='pc'">
      <el-select v-model="activeData.popupWidth" placeholder="请选择弹窗宽度" filterable>
        <el-option v-for="item in popupWidthOptions" :label="item" :value="item" :key="item" />
      </el-select>
    </el-form-item>
    <el-form-item label="远端数据">
      <interface-dialog :value="activeData.interfaceId" :title="activeData.interfaceName"
        popupTitle="远端数据" @change="onInterfaceIdChange" :hasPage='1' />
    </el-form-item>
    <el-form-item label="存储字段">
      <el-autocomplete class="inline-input" v-model="activeData.propsValue"
        :fetch-suggestions="querySearch" placeholder="请输入内容" style="width:100%"
        @select="handleSelect($event,'propsValue')">
        <template slot-scope="{ item }">
          <div class="sale-order-popper-item">
            <span>{{ item.defaultValue}}</span>
          </div>
        </template>
      </el-autocomplete>
    </el-form-item>
    <el-form-item label="显示字段">
      <el-autocomplete class="inline-input" v-model="activeData.relationField"
        :fetch-suggestions="querySearch" placeholder="请输入内容" style="width:100%"
        @select="handleSelect($event,'relationField')">
        <template slot-scope="{ item }">
          <div class="sale-order-popper-item">
            <span>{{ item.defaultValue}}</span>
          </div>
        </template>
      </el-autocomplete>
    </el-form-item>
    <el-table :data="activeData.templateJson"
      v-if="activeData.templateJson && activeData.templateJson.length">
      <el-table-column type="index" width="50" label="序号" align="center" />
      <el-table-column prop="field" label="参数名称">
        <template slot-scope="scope">
          <span class="required-sign">{{scope.row.required?'*':''}}</span>
          {{scope.row.fieldName?scope.row.field+'('+scope.row.fieldName+')':scope.row.field}}
        </template>
      </el-table-column>
      <el-table-column prop="value" label="表单字段">
        <template slot-scope="scope">
          <el-select v-model="scope.row.relationField" placeholder="请选择表单字段" clearable filterable
            @change="onRelationFieldChange($event,scope.row)">
            <el-option v-for="item in formFieldsOptions" :key="item.realVModel"
              :label="item.realLabel" :value="item.realVModel">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
    </el-table>
    <el-form-item label="填充字段"
      v-if="activeData.__config__.jnpfKey=='popupSelect'&&(modelType == 1 || modelType == 6)"
      :class="{'mt-18px': activeData.templateJson && activeData.templateJson.length}">
      <div slot="label">填充字段
        <el-tooltip content="选择数据后，将远端数据的字段值填充到当前表单字段。" placement="top">
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
        <el-input v-model="item.label" placeholder="列名" size="small" />
        <el-autocomplete class="inline-input" v-model="item.value" :fetch-suggestions="querySearch"
          placeholder="请输入内容" style="width:100%;margin-left: 6px;"
          @select="handleSelect($event,'value',index)">
          <template slot-scope="{ item }">
            <div class="sale-order-popper-item">
              <span>{{ item.defaultValue}}</span>
            </div>
          </template>
        </el-autocomplete>
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
      <el-switch v-model="activeData.hasPage"
        :disabled="activeData.interfaceHasPage == 1 && activeData.hasPage"></el-switch>
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
    <el-form-item label="能否搜索" v-if="activeData.__config__.jnpfKey==='popupTableSelect'">
      <el-switch v-model="activeData.filterable" />
    </el-form-item>
    <el-form-item label="能否多选" v-if="activeData.__config__.jnpfKey==='popupTableSelect'">
      <el-switch v-model="activeData.multiple" @change="multipleChange" />
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
    <RuleDialog ref="rule" v-if="ruleVisible" :type='2' @confirm='onChangeFieldRule' />
  </el-row>
</template>
<script>
import comMixin from './mixin';
import handelFlidMixin from './handelFlidMixin';
import draggable from 'vuedraggable'
import { noAllowRelationList } from '@/components/Generator/generator/comConfig'
import { getDrawingList } from '@/components/Generator/utils/db'
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'
import RuleDialog from './RuleDialog'
import { systemComponentsList } from '@/components/Generator/generator/comConfig'
const noFieldList = ['relationForm', 'popupSelect']
export default {
  props: ['activeData', 'modelType'],
  mixins: [comMixin, handelFlidMixin],
  components: { draggable, InterfaceDialog, RuleDialog },
  data() {
    return {
      popupWidthOptions: ['600px', '800px', '1000px', '40%', '50%', '60%', '70%', '80%'],
      treeData: [],
      fieldJsonList: [],
      ruleVisible: false
    }
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
  mounted() {
    this.getDataInterfaceInfo()
  },
  methods: {
    multipleChange(val) {
      this.$set(this.activeData.__config__, 'defaultValue', val ? [] : '')
    },
    isIncludesTable(data) {
      if ((!data.__config__.layout || data.__config__.layout === 'rowFormItem') && data.__config__.jnpfKey !== 'table') return true
      if (this.activeData.__config__.isSubTable) return this.activeData.__config__.parentVModel === data.__vModel__
      return data.__config__.jnpfKey !== 'table'
    },
    onRelationFieldChange(val, row) {
      if (!val) return row.jnpfKey = ''
      let list = this.formFieldsOptions.filter(o => o.realVModel === val)
      if (!list.length) return row.jnpfKey = ''
      let item = list[0]
      row.jnpfKey = item.__config__.jnpfKey
    },
    addSelectItem() {
      this.activeData.columnOptions.push({
        value: '',
        label: ''
      })
    },
    onChangeRule() {
      if (!this.activeData.interfaceId) return this.$message.error('请先选择远端数据')
      this.ruleVisible = true
      let list = JSON.parse(JSON.stringify(this.formFieldsOptions))
      if (this.activeData.__config__.isSubTable) list = list.filter(o => o.__config__.isSubTable)
      list = list.filter(o => ![...systemComponentsList, ...noFieldList].includes(o.__config__.jnpfKey))
      this.$nextTick(() => {
        this.$refs.rule.init({ transferList: this.activeData.__config__.transferList, fieldList: this.fieldJsonList, formFieldsList: list })
      })
    },
    onChangeFieldRule(val) {
      this.activeData.__config__.transferList = val
    },
    isSameSource(data) {
      const isSubTable = this.activeData.__config__.isSubTable;
      if (isSubTable) return data.__config__.isSubTable && this.activeData.__config__.parentVModel === data.__config__.parentVModel;
      return !data.__config__.isSubTable;
    }
  }
}
</script>
<style lang="scss" scoped>
.mt-18px {
  margin-top: 18px;
}
</style>