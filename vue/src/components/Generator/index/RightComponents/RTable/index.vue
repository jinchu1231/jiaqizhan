<template>
  <el-row>
    <el-form-item label="显示标题">
      <el-switch v-model="activeData.__config__.showTitle" />
    </el-form-item>
    <el-form-item label="合计设置">
      <el-switch v-model="activeData.showSummary" />
    </el-form-item>
    <el-form-item label="合计字段" v-if="activeData.showSummary">
      <el-select v-model="activeData.summaryField" multiple placeholder="请选择合计字段">
        <template v-for="(item,i) in activeData.__config__.children">
          <el-option :key="i" :label="item.__config__.label" :value="item.__vModel__"
            v-if="['input','inputNumber','calculate'].includes(item.__config__.jnpfKey) && item.__vModel__&& !item.useMask" />
        </template>
      </el-select>
    </el-form-item>
    <el-form-item label="复杂表头" v-show="showType==='pc'">
      <el-button style="width: 100%;"
        @click="addComplexHeader">{{activeData.__config__.complexHeaderList.length?'编辑复杂表头':'复杂表头配置'}}</el-button>
    </el-form-item>
    <el-divider>子表字段</el-divider>
    <draggable :list="activeData.__config__.children" :animation="340" group="selectItem"
      handle=".option-drag">
      <div v-for="(item, index) in activeData.__config__.children" :key="index" class="select-item">
        <div class="select-line-icon option-drag">
          <i class="icon-ym icon-ym-darg" />
        </div>
        <span class="select-label"> {{item.__config__.label}}</span>
        <div class="close-btn select-line-icon"
          @click="activeData.__config__.children.splice(index, 1)">
          <i class="el-icon-delete" style="font-size: 16px;" />
        </div>
      </div>
      <div style="margin-left: 10px;">
        <el-dropdown trigger="click">
          <el-button style="padding-bottom: 0" icon="el-icon-circle-plus-outline" type="text">
            添加字段
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <div class="dropdown">
              <el-dropdown-item v-for="item in getBaseOptions" :icon="item.__config__.tagIcon"
                :key='item.__config__.jnpfKey'
                @click.native="addHandle(item)">{{item.__config__.label}}
              </el-dropdown-item>
              <el-dropdown-item :divided='true' />
              <el-dropdown-item v-for="item in getSeniorOptions" :icon="item.__config__.tagIcon"
                :key='item.__config__.jnpfKey'
                @click.native="addHandle(item)">{{item.__config__.label}}
              </el-dropdown-item>
            </div>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </draggable>
    <el-divider>操作列按钮</el-divider>
    <draggable :list="activeData.columnBtnsList" :animation="340" group="selectItem"
      handle=".option-drag">
      <div v-for="(item, index) in activeData.columnBtnsList" :key="index" class="select-item">
        <div class="select-line-icon option-drag">
          <i class="icon-ym icon-ym-darg" />
        </div>
        <span class="select-label"> {{item.label}}</span>
        <div class="close-btn select-line-icon" @click="editConf(item,index,'columnBtnsList')">
          <i class="el-icon-edit-outline" style="font-size: 16px;color: #409eff" />
        </div>
      </div>
    </draggable>
    <el-divider>底部按钮</el-divider>
    <draggable :list="activeData.footerBtnsList" :animation="340" group="selectItem"
      handle=".option-drag">
      <div v-for="(item, index) in activeData.footerBtnsList" :key="index" class="select-item">
        <div class="select-line-icon option-drag">
          <i class="icon-ym icon-ym-darg" />
        </div>
        <span class="select-label">{{item.label}}</span>
        <div class="close-btn select-line-icon" @click="activeData.footerBtnsList.splice(index, 1)"
          v-if="![ 'add', 'batchRemove'].includes(item.value)">
          <i class="el-icon-delete" style="font-size: 16px" />
        </div>
        <div class="close-btn select-line-icon" @click="editConf(item,index,'footerBtnsList')">
          <i class="el-icon-edit-outline" style="font-size: 16px;color: #409eff" />
        </div>
      </div>
      <div style="margin-left: 10px;">
        <el-button style="padding-bottom: 0" icon="el-icon-circle-plus-outline" type="text"
          @click="editConf(false,-1,'footerBtnsList')" v-if="activeData.footerBtnsList.length<5">
          添加字段
        </el-button>
      </div>
    </draggable>
    <Form v-if="formVisible" ref="Form" @submit="updateConf" />
    <ComplexHeader v-if="complexHeaderVisible" ref="ComplexHeader" @closeForm='closeForm' />
  </el-row>
</template>
<script>
import Form from './AddTableForm'
import draggable from 'vuedraggable'
import ComplexHeader from '@/components/Generator/components/ComplexHeader'
import {
  inputComponents, selectComponents
} from '@/components/Generator/generator/config'
import { noTableAllowList, calculateItem } from '@/components/Generator/generator/comConfig'
const defaultAddTableConf = {
  popupTitle: '选择数据',
  popupType: 'dialog',
  popupWidth: '800px',
  dataSource: 'dynamic',
  interfaceId: '',
  interfaceName: '',
  templateJson: [],
  hasPage: true,
  pageSize: 20,
  columnOptions: [],
  relationOptions: [],
}
const defaultColumnBtnsList = [
  { value: 'copy', label: '复制', show: true, btnType: 'primary', btnIcon: 'icon-ym icon-ym-btn-edit' },
  { value: 'remove', label: '删除', show: true, btnType: 'danger', btnIcon: 'icon-ym icon-ym-btn-clearn', showConfirm: true },
];
const defaultFooterBtnsList = [
  { value: 'add', label: '添加', show: true, btnType: 'primary', btnIcon: 'icon-ym icon-ym-btn-add' },
  { value: 'batchRemove', label: '批量删除', show: true, btnType: 'danger', btnIcon: 'icon-ym icon-ym-btn-clearn', showConfirm: true },
];
export default {
  props: ['activeData', 'showType', 'modelType'],
  components: { Form, ComplexHeader, draggable },
  data() {
    return {
      formVisible: false,
      thousandsOptions: [],
      complexHeaderVisible: false,
      defaultAddTableConf,
      activeRowIndex: 0,
      activeRowType: '',
    }
  },
  computed: {
    getBaseOptions() {
      let noTableList = [...noTableAllowList, 'table']
      return inputComponents.filter(o => !noTableList.includes(o.__config__ && o.__config__.jnpfKey))
    },
    getSeniorOptions() {
      let noTableList = [...noTableAllowList, 'table']
      let list = selectComponents.filter(o => !noTableList.includes(o.__config__ && o.__config__.jnpfKey))
      if (this.modelType == 1 || this.modelType == 6) list = [...list, calculateItem]
      return list
    }
  },
  mounted() {
    if (!this.activeData.columnBtnsList || !this.activeData.columnBtnsList.length) this.activeData.columnBtnsList = JSON.parse(JSON.stringify(defaultColumnBtnsList))
    if (!this.activeData.footerBtnsList || !this.activeData.footerBtnsList.length) this.activeData.footerBtnsList = JSON.parse(JSON.stringify(defaultFooterBtnsList))
  },
  methods: {
    editConf(item, index, type) {
      this.activeRowIndex = index
      this.activeRowType = type
      let defaultAddTableConf = {}
      if (!item) {
        item = {}
        defaultAddTableConf = JSON.parse(JSON.stringify(this.defaultAddTableConf))
        item.value = 'btn-' + this.jnpf.idGenerator()
        item.label = ''
        item.show = true
        item.btnIcon = ''
        item.btnType = ''
      } else if (!['copy', 'remove', 'add', 'batchRemove'].includes(item.value)) {
        defaultAddTableConf = JSON.parse(JSON.stringify(item.actionConfig))
      }
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(item, defaultAddTableConf, this.activeData.__config__.children)
      })
    },
    addBtnChange(e) {
      if (!e) {
        this.activeData.addType = e ? 1 : 0
        this.activeData.addTableConf = JSON.parse(JSON.stringify(defaultAddTableConf))
      }
    },
    setAction(e) {
      if (!e) this.activeData.addTableConf = JSON.parse(JSON.stringify(defaultAddTableConf))
    },
    closeForm(val) {
      if (val) this.activeData.__config__.complexHeaderList = val
      this.complexHeaderVisible = false
    },
    addComplexHeader() {
      this.complexHeaderVisible = true
      let list = this.activeData.__config__.children.filter(o => o.__vModel__ && o.__config__.tableFixed === 'none')
      let complexHeaderList = JSON.parse(JSON.stringify(this.activeData.__config__.complexHeaderList))
      complexHeaderList = this.updateComplexHeader(complexHeaderList, list) || []
      this.$nextTick(() => {
        this.$refs.ComplexHeader.init(complexHeaderList, list)
      })
    },
    updateComplexHeader(complexHeaderList, list) {
      if (!complexHeaderList.length) return [];
      for (let i = 0; i < complexHeaderList.length; i++) {
        const e = complexHeaderList[i];
        e.childColumns = e.childColumns.filter(o => list.some(item => item.__vModel__ == o));
      }
      return complexHeaderList
    },
    updateConf(item, actionConfig) {
      if (['copy', 'remove', 'add', 'batchRemove'].includes(item.value)) return this.$set(this.activeData[this.activeRowType], this.activeRowIndex, item)
      if (this.activeRowIndex >= 0) return this.$set(this.activeData[this.activeRowType], this.activeRowIndex, { ...item, actionConfig })
      this.activeData[this.activeRowType].push({ ...item, actionConfig })
    },
    addHandle(item) {
      if (this.$store.getters.hasTable && !this.activeData.__config__.tableName) return this.$message.warning('子表请先关联数据表')
      item.__config__.isSubTable = true
      item.__config__.parentVModel = this.activeData.__vModel__
      if (this.$store.getters.hasTable) item.__config__.relationTable = this.activeData.__config__.tableName
      this.$emit('addComponent', item, this.activeData.__config__.children)
    },
  }
}
</script>
<style lang="scss" scoped>
.select-label {
  line-height: 32px;
  width: 100%;
}
.dropdown {
  height: 400px;
  overflow: auto;
}
</style>
