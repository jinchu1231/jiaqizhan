<template>
  <div class="columnDesign-container">
    <div class="main-board">
      <div class="search-box">
        <h4 class="cap">查询字段</h4>
        <el-table :data="columnData.searchList" class="JNPF-common-table" ref="dragTableSearch"
          :row-key="row=>row.prop">
          <el-table-column align="center" label="拖动" width="50">
            <template>
              <i class="drag-handler icon-ym icon-ym-darg" style="cursor: move;font-size:20px"
                title='点击拖动' />
            </template>
          </el-table-column>
          <el-table-column prop="label" label="列名" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.label" placeholder="列名" />
            </template>
          </el-table-column>
          <el-table-column prop="prop" label="字段" width="600" />
          <el-table-column prop="jnpfKey" label="输入类型" v-if="webType == 4" width="240">
            <template slot-scope="scope">
              <el-select v-model="scope.row.jnpfKey" placeholder="请选择"
                :disabled="scope.row.__config__.isFromParam" :key="key"
                @change="onJnpfKeyChange($event, scope.row, scope.$index)">
                <el-option v-for="item in viewJnpfKeyOptions" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
              </el-select>
              <template
                v-if="['select', 'datePicker', 'timePicker', 'organizeSelect', 'depSelect', 'userSelect'].includes(scope.row.__config__.jnpfKey) && !scope.row.__config__.isFromParam">
                <i style="margin-left: 10px;font-size: 16px;cursor: pointer;"
                  class="el-icon-setting" title="组件属性设置"
                  @click="openExtraConfig(scope.row, scope.$index)" />
              </template>
            </template>
          </el-table-column>
          <el-table-column prop="searchType" label="条件类型" width="200">
            <template slot-scope="scope">
              <el-select v-model="scope.row.searchType" placeholder="请选择"
                :disabled="scope.row.jnpfKey!=='input'&&scope.row.jnpfKey!=='textarea'"
                v-if="!scope.row.__config__.isFromParam">
                <el-option label="等于查询" :value="1"></el-option>
                <el-option label="模糊查询" :value="2"></el-option>
                <el-option label="范围查询" :value="3"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="label" label="默认值" width="200" v-if="webType != 4">
            <template slot-scope="scope">
              <el-input v-model="scope.row.value" placeholder="默认值"
                v-if="textBoxList.includes(scope.row.jnpfKey)" />
              <num-range
                v-if="scope.row.jnpfKey==='inputNumber'|| scope.row.jnpfKey==='rate' || scope.row.jnpfKey==='slider'"
                v-model="scope.row.value" :precision="scope.row.precision" clearable
                filterable></num-range>
              <el-select v-model="scope.row.value" placeholder="请选择" style="width: 100%;"
                v-if="selectionBoxList.includes(scope.row.jnpfKey)" filterable clearable
                :multiple="scope.row.searchMultiple" :key="scope.row.__config__.renderKey">
                <el-option v-for="item,index in scope.row.options" :key="index"
                  :label="item[scope.row.props.label]" :value="item[scope.row.props.value]">
                </el-option>
              </el-select>
              <JnpfCascader v-if="scope.row.jnpfKey === 'cascader'" :options="scope.row.options"
                clearable :show-all-levels="scope.row.showAllLevels" :props="scope.row.props"
                v-model="scope.row.value" :multiple="scope.row.searchMultiple" filterable
                placeholder="请选择" style="width: 100%;" />
              <JnpfAutoComplete v-model="scope.row.value" placeholder="请选择"
                v-if="scope.row.jnpfKey === 'autoComplete'" :interfaceId="scope.row.interfaceId"
                clearable :propsValue="scope.row.propsValue"
                :relationField="scope.row.relationField" :templateJson="scope.row.templateJson"
                :total="scope.row.total" />
              <JnpfAreaSelect v-model="scope.row.value" :placeholder="'请选择'+scope.row.fullName"
                :level="scope.row.level" clearable v-if="scope.row.jnpfKey === 'areaSelect'" />
              <JnpfDateRangePicker v-model="scope.row.value" :type="scope.row.type" clearable
                :format="scope.row.format" startPlaceholder="开始日期" endPlaceholder="结束日期"
                v-if="scope.row.jnpfKey === 'datePicker'">
              </JnpfDateRangePicker>
              <el-time-picker v-if="scope.row.jnpfKey === 'timePicker'" v-model="scope.row.value"
                start-placeholder="开始时间" end-placeholder="结束时间" clearable
                :value-format="scope.row.format" :format="scope.row.format" is-range class="item"
                style="width:100%" />
              <JnpfOrganizeSelect v-model="scope.row.value" :placeholder="'请选择'+scope.row.fullName"
                clearable :multiple="scope.row.searchMultiple" :selectType="scope.row.selectType"
                :ableIds="scope.row.ableIds"
                v-if="scope.row.__config__.jnpfKey==='organizeSelect'" />
              <JnpfDepSelect v-model="scope.row.value"
                v-if="scope.row.__config__.jnpfKey==='depSelect'"
                :placeholder="'请选择'+scope.row.fullName" clearable :selectType="scope.row.selectType"
                :ableIds="scope.row.ableIds" :multiple="scope.row.searchMultiple" />
              <JnpfPosSelect v-model="scope.row.value" :placeholder="'请选择'+scope.row.fullName"
                clearable :selectType="scope.row.selectType" :ableIds="scope.row.ableIds"
                :ablePosIds="scope.row.ablePosIds" :multiple="scope.row.searchMultiple"
                v-if="scope.row.__config__.jnpfKey==='posSelect'" />
              <JnpfUserSelect v-if="scope.row.jnpfKey==='userSelect'" v-model="scope.row.value"
                :placeholder="'请选择'+scope.row.fullName" clearable class="item"
                :selectType="(scope.row.selectType!='all' && scope.row.selectType!='custom')?'all':scope.row.selectType"
                :ableIds="scope.row.ableIds" :multiple="scope.row.searchMultiple" />
              <JnpfUsersSelect v-if="scope.row.jnpfKey==='usersSelect'" v-model="scope.row.value"
                :placeholder="'请选择'+scope.row.fullName" clearable class="item"
                :selectType="scope.row.selectType" :ableIds="scope.row.ableIds"
                :multiple="scope.row.searchMultiple" />
              <JnpfRoleSelect v-model="scope.row.value" :placeholder="'请选择'+scope.row.fullName"
                clearable :multiple="scope.row.searchMultiple" :ableIds="scope.row.ableIds"
                v-if="scope.row.__config__.jnpfKey==='roleSelect'"
                :selectType="scope.row.selectType" />
              <JnpfGroupSelect v-model="scope.row.value" :placeholder="'请选择'+scope.row.fullName"
                clearable :selectType="scope.row.selectType" :multiple="scope.row.searchMultiple"
                :ableIds="scope.row.ableIds" v-if="scope.row.__config__.jnpfKey==='groupSelect'" />
            </template>
          </el-table-column>
          <el-table-column prop='isKeyword' label="关键词" align="center" v-if="webType != 4">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.isKeyword"
                :disabled="!canSetKeyword.includes(scope.row.jnpfKey) || (getIsKeywordDisabled && !scope.row.isKeyword)">
              </el-checkbox>
            </template>
          </el-table-column>
          <el-table-column prop='searchMultiple' label="是否多选" align="center">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.searchMultiple"
                @change="onSearchMultipleChange(scope.row,scope.$index)"
                v-if="['select','depSelect','roleSelect','userSelect','usersSelect','organizeSelect','posSelect','groupSelect'].includes(scope.row.jnpfKey)">
              </el-checkbox>
              <el-checkbox v-else disabled></el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-divider></el-divider>
      <div class="column-box">
        <h4 class="cap">列表字段</h4>
        <el-table :data="columnData.columnList" class="JNPF-common-table" ref="dragTable"
          :row-key="row=>row.prop">
          <el-table-column align="center" label="拖动" width="50">
            <template>
              <i class="drag-handler icon-ym icon-ym-darg" style="cursor: move;font-size:20px"
                title='点击拖动' />
            </template>
          </el-table-column>
          <el-table-column prop="label" label="列名" v-if="webType!=4" />
          <el-table-column prop="label" label="列名" v-else>
            <template slot-scope="scope">
              <el-input v-model="scope.row.label" placeholder="列名" />
            </template>
          </el-table-column>
          <el-table-column prop="prop" label="字段" />
          <el-table-column prop="sortable" label="排序" width="60" align="center">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.sortable"
                :disabled="scope.row.__config__&&(scope.row.__config__.isSubTable ||noGroupList.includes(scope.row.__config__.jnpfKey) )" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="right-board">
      <el-tabs v-model="currentTab" class="top-tabs">
        <el-tab-pane label="查询字段" name="search" />
        <el-tab-pane label="列表字段" name="column" />
        <el-tab-pane label="列表属性" name="attr" />
      </el-tabs>
      <div class="field-box">
        <div class="columnList" v-show="currentTab==='search'">
          <el-table :data="searchOptions" class="JNPF-common-table" height="100%"
            @selection-change="searchSelectionChange" ref="searchTable">
            <el-table-column prop="fullName" label="查询字段" v-if="webType!=4" />
            <el-table-column prop="fullName" label="查询字段" v-else />
            <el-table-column type="selection" width="55" align="center" />
          </el-table>
        </div>
        <div class="columnList" v-show="currentTab==='column'">
          <el-table :data="columnOptions" class="JNPF-common-table" height="100%"
            @selection-change="columnSelectionChange" ref="columnTable">
            <el-table-column prop="label" label="列表字段" v-if="webType!=4" />
            <el-table-column prop="fullName" label="列表字段" v-else />
            <el-table-column type="selection" width="55" align="center" />
          </el-table>
        </div>
        <el-scrollbar class="right-scrollbar" v-show="currentTab==='attr'">
          <div class="setting-box">
            <el-form :model="columnData" label-width="80px" label-position="left">
              <el-divider>表格配置</el-divider>
              <el-form-item label="数据过滤" v-if="webType != 4">
                <el-button style="width: 100%;" @click="filterPanelShow">
                  {{ this.columnData.ruleListApp && this.columnData.ruleListApp.conditionList&&this.columnData.ruleListApp.conditionList.length > 0 ? '编辑过滤条件' : '添加过滤条件' }}
                </el-button>
              </el-form-item>
              <Condition ref="conditionpane" :modelType="modelType"
                :formFieldsOptions='webType==4?columnOptions:list' @ruleConfig="ruleConfig">
              </Condition>
              <el-form-item label="默认排序">
                <el-button style="width: 100%;" @click="editDefaultSortConfig">
                  默认排序配置
                </el-button>
              </el-form-item>
              <el-form-item label="分页设置">
                <el-switch v-model="columnData.hasPage"></el-switch>
              </el-form-item>
              <el-form-item label="分页条数" v-if="columnData.hasPage">
                <el-radio-group v-model="columnData.pageSize">
                  <el-radio-button :label="20">20条</el-radio-button>
                  <el-radio-button :label="50">50条</el-radio-button>
                  <el-radio-button :label="100">100条</el-radio-button>
                  <el-radio-button :label="500">500条</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-divider>按钮配置</el-divider>
              <el-checkbox-group v-model="btnsList" class="btnsList" v-if="webType!=4">
                <el-checkbox :label="item.value" v-for="item in btnsOption" :key="item.value">
                  <span class="btn-label">{{ item.value | btnText }}</span>
                  <el-input v-model="item.label" />
                </el-checkbox>
              </el-checkbox-group>
              <el-checkbox-group v-model="columnBtnsList" class="btnsList columnBtnList"
                v-if="webType!=4">
                <el-checkbox :label="item.value" v-for="item in columnBtnsOption" :key="item.value">
                  <span class="btn-label">{{ item.value | btnText }}</span>
                  <el-input v-model="item.label" />
                </el-checkbox>
              </el-checkbox-group>
              <template v-if="modelType==1">
                <p class="btn-cap mt-10 mb-10">自定义按钮区</p>
                <div class="custom-btns-list">
                  <draggable :list="columnData.customBtnsList" :animation="340" group="customItem"
                    handle=".option-drag">
                    <div v-for="(item, index) in columnData.customBtnsList" :key="index"
                      class="custom-item">
                      <div class="custom-line-icon option-drag">
                        <i class="icon-ym icon-ym-darg" />
                      </div>
                      <p class="custom-line-value">{{item.value}}</p>
                      <el-input v-model="item.label" placeholder="按钮名称" size="small">
                        <template slot="append">
                          <el-button type="primary" @click="editFunc(item)" class="custom-btn">事件
                          </el-button>
                        </template>
                      </el-input>
                      <div class="close-btn custom-line-icon"
                        @click="columnData.customBtnsList.splice(index, 1)">
                        <i class="el-icon-remove-outline" />
                      </div>
                    </div>
                  </draggable>
                </div>
                <div>
                  <el-button type="text" icon="el-icon-plus" @click="addCustomBtn">添加按钮</el-button>
                </div>
              </template>
              <template v-if="webType!=4">
                <el-divider>权限设置</el-divider>
                <el-form-item label="按钮权限">
                  <el-switch v-model="columnData.useBtnPermission"></el-switch>
                </el-form-item>
                <el-form-item label="列表权限">
                  <el-switch v-model="columnData.useColumnPermission"></el-switch>
                </el-form-item>
                <el-form-item label="表单权限">
                  <el-switch v-model="columnData.useFormPermission"></el-switch>
                </el-form-item>
                <el-form-item label="数据权限">
                  <el-switch v-model="columnData.useDataPermission"></el-switch>
                </el-form-item>
              </template>
              <template v-if="modelType==1">
                <el-divider>脚本事件</el-divider>
                <el-form-item label="表格事件">
                  <el-button style="width: 100%;"
                    @click="addFunc(columnData.funcs.afterOnload,'afterOnload',true)">脚本编写
                  </el-button>
                </el-form-item>
              </template>
            </el-form>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <form-script v-if="formScriptVisible" ref="formScript" :value="activeItem" :type="funcName"
      @updateScript="updateScript" @closeDialog="formScriptVisible=false" />
    <custom-btn v-if="customBtnVisible" :activeItem="activeItem" ref="customBtn"
      @updateCustomBtn="updateCustomBtn" @closeDialog="customBtnVisible=false" />
    <DefaultSortConfigModal v-if="searchConfigVisible" ref="SearchConfig"
      @confirm='updateDefaultSortConfig' />
    <ExtraConfigModal v-if="ExtraConfigModalVisible" ref="ExtraConfigModal"
      @confirm="updateSearchRow" />
  </div>
</template>
<script>
import Sortable from 'sortablejs'
import Condition from './FlowCondition'
import draggable from 'vuedraggable'
import FormScript from './FormScript'
import CustomBtn from './CustomBtn'
import { getDictionaryDataSelector } from '@/api/systemData/dictionary'
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
import { getDrawingList } from '@/components/Generator/utils/db'
import { noColumnShowList, noSearchList, useInputList, useDateList, useSelectList, dyOptionsList, noGroupList } from '@/components/Generator/generator/comConfig'
import ExtraConfigModal from './ExtraConfigModal.vue';
import DefaultSortConfigModal from './DefaultSortConfigModal.vue'
const canSetAttrs = ['select', 'datePicker', 'timePicker', 'organizeSelect', 'depSelect', 'userSelect'];
const getSearchMultiple = item => {
  const jnpfKey = item.__config__.jnpfKey
  const searchMultipleList = ['select', 'depSelect', 'roleSelect', 'userSelect', 'usersSelect', 'organizeSelect', 'posSelect', 'groupSelect']
  if (searchMultipleList.includes(jnpfKey)) return true
  return false
}
const getDefaultValue = item => {
  const jnpfKey = item.__config__.jnpfKey;
  const list = ['areaSelect', 'datePicker', 'inputNumber', 'organizeSelect', 'calculate'];
  return list.includes(jnpfKey) || item.multiple ? [] : undefined;
};
const getSearchType = item => {
  const jnpfKey = item.__config__.jnpfKey
  // 等于-1  模糊-2  范围-3
  const fuzzyList = [...useInputList]
  const RangeList = [...useDateList, 'timePicker', 'datePicker', 'inputNumber', 'calculate']
  if (RangeList.includes(jnpfKey)) return 3
  if (fuzzyList.includes(jnpfKey)) return 2
  return 1
}
const defaultFunc = '({ data, index, refresh, onlineUtils }) => {\r\n   \r\n}'
const defaultFuncs = '({ data, tableRef, onlineUtils }) => {\r\n   \r\n}'
const defaultColumnData = {
  ruleListApp: { matchLogic: 'and', conditionList: [] }, // 过滤规则
  searchList: [], // 查询字段
  hasSuperQuery: false, // 高级查询
  columnList: [], // 字段列表
  columnOptions: [],
  defaultColumnList: [], // 所有可选择字段列表
  // type: 1, //列表类型
  defaultSortConfig: [], // 默认排序配置
  hasPage: true,  // 列表分页
  pageSize: 20,  // 分页条数
  useColumnPermission: false,
  useFormPermission: false,
  useBtnPermission: false,
  useDataPermission: false,
  customBtnsList: [],
  thousands: false,
  thousandsField: [],
  btnsList: [
    { value: 'add', icon: 'icon-ym icon-ym-btn-add', label: '新增' }
  ],  // 按钮
  columnBtnsList: [
    { value: 'edit', icon: 'icon-ym icon-ym-btn-edit', label: '编辑' },
    { value: 'remove', icon: 'icon-ym icon-ym-btn-clearn', label: '删除' },
    { value: 'detail', icon: 'icon-ym icon-ym-generator-menu', label: '详情' }
  ], // 列按钮
  funcs: {
    afterOnload: "",
  },
  loading: false
}
export default {
  name: 'columnDesign',
  props: {
    conf: {
      type: Object,
      default: () => { },
    },
    webType: '',
    modelType: '',
    interfaceId: '',
    templateJson: {
      type: Array,
      default: () => []
    },
    viewFields: {
      type: Array,
      default: () => []
    },
  },
  components: { draggable, FormScript, CustomBtn, Condition, ExtraConfigModal, DefaultSortConfigModal },
  data() {
    return {
      searchConfigVisible: false,
      viewJnpfKeyOptions: [
        { id: 'input', fullName: '单行输入' },
        { id: 'inputNumber', fullName: '数字输入' },
        { id: 'select', fullName: '下拉选择' },
        { id: 'datePicker', fullName: '日期选择' },
        { id: 'timePicker', fullName: '时间选择' },
        { id: 'organizeSelect', fullName: '组织选择' },
        { id: 'depSelect', fullName: '部门选择' },
        { id: 'roleSelect', fullName: '角色选择' },
        { id: 'posSelect', fullName: '岗位选择' },
        { id: 'groupSelect', fullName: '分组选择' },
        { id: 'userSelect', fullName: '用户选择' },
      ],
      key: +new Date(),
      activeSearchRowIndex: 0,
      ExtraConfigModalVisible: false,
      textBoxList: ['input', 'billRule'],
      selectionBoxList: useSelectList,
      currentTab: 'attr',
      alignOptions: ['left', 'center', 'right'],
      list: [],
      columnData: JSON.parse(JSON.stringify(defaultColumnData)),
      btnsOption: [
        { value: 'add', icon: 'el-icon-plus', label: '新增' }
      ],
      columnBtnsOption: [
        { value: 'edit', icon: 'icon-ym icon-ym-btn-edit', label: '编辑' },
        { value: 'remove', icon: 'icon-ym icon-ym-btn-clearn', label: '删除' },
        { value: 'detail', icon: 'icon-ym icon-ym-generator-menu', label: '详情' }
      ],
      canSetKeyword: ['input', 'textarea', 'inputNumber', 'autoComplete'],
      columnOptions: [],
      searchOptions: [],
      groupFieldOptions: [],
      sortOptions: [],
      sortList: [],
      btnsList: [],
      columnBtnsList: [],
      formScriptVisible: false,
      customBtnVisible: false,
      activeItem: {},
      funcName: '',
      thousandsOptions: [],
      noGroupList
    }
  },
  filters: {
    btnText(key) {
      let text = ''
      switch (key) {
        case 'edit':
          text = '编辑'
          break;
        case 'remove':
          text = '删除'
          break;
        case 'detail':
          text = '详情'
          break;
        default:
          text = '新增'
          break;
      }
      return text
    }
  },
  computed: {
    getIsKeywordDisabled() {
      return this.columnData.searchList.filter(o => o.isKeyword).length >= 3
    }
  },
  watch: {
    btnsList(val) {
      let list = []
      outer: for (let i = 0; i < this.btnsOption.length; i++) {
        inter: for (let ii = 0; ii < val.length; ii++) {
          if (val[ii] === this.btnsOption[i].value) {
            list.push(this.btnsOption[i])
            break inter;
          }
        }
      }
      this.columnData.btnsList = list
    },
    columnBtnsList(val) {
      let list = []
      outer: for (let i = 0; i < this.columnBtnsOption.length; i++) {
        inter: for (let ii = 0; ii < val.length; ii++) {
          if (val[ii] === this.columnBtnsOption[i].value) {
            list.push(this.columnBtnsOption[i])
            break inter;
          }
        }
      }
      this.columnData.columnBtnsList = list
    },
  },
  created() {
    this.loading = false
    if (typeof this.conf === 'object' && this.conf !== null) this.columnData = Object.assign({}, defaultColumnData, this.conf)
    if (this.webType != 4) {
      let list = []
      let list1 = []
      const loop = (data, parent) => {
        if (!data) return
        if (data.__config__ && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data)
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent))
        if (data.__config__ && data.__config__.jnpfKey) {
          const visibility = !data.__config__.visibility || (Array.isArray(data.__config__.visibility) && data.__config__.visibility.includes('app'))
          if (data.__config__.layout === 'colFormItem' && data.__vModel__ && visibility) {
            const isTableChild = parent && parent.__config__ && parent.__config__.jnpfKey === 'table'
            const id = isTableChild ? parent.__vModel__ + '-' + data.__vModel__ : data.__vModel__
            const label = isTableChild ? parent.__config__.label + '-' + data.__config__.label : data.__config__.label
            data.id = id
            data.fullName = label
            list.push(data)
            if (data.__vModel__.indexOf('_jnpf_') < 0) list1.push(data)
          }
        }
      }
      loop(getDrawingList())
      this.list = list
      let options = list.filter(o => noColumnShowList.indexOf(o.__config__.jnpfKey) < 0 || o.isStorage == 1)
      let searchOptions = list.filter(o => noSearchList.indexOf(o.__config__.jnpfKey) < 0)
      this.thousandsOptions = list.filter(o => noSearchList.indexOf(o.__config__.jnpfKey) < 0 && (o.__config__.jnpfKey == 'inputNumber' || o.__config__.jnpfKey == 'calculate'))
      let sortOptions = list1.filter(o => noColumnShowList.indexOf(o.__config__.jnpfKey) < 0 || o.isStorage == 1)
      sortOptions = sortOptions.filter(o => o.id.indexOf('-') < 0)
      this.groupFieldOptions = list.filter(o => o.id.indexOf('-') < 0 && !noGroupList.includes(o.__config__.jnpfKey))
      this.columnOptions = options.map(o => ({
        label: o.fullName,
        prop: o.id,
        align: 'left',
        jnpfKey: o.__config__.jnpfKey,
        sortable: false,
        width: null,
        ...o
      }));

      this.searchOptions = searchOptions.map(o => ({
        label: o.fullName,
        prop: o.id,
        jnpfKey: o.__config__.jnpfKey,
        value: getDefaultValue(o),
        searchType: getSearchType(o),
        searchMultiple: getSearchMultiple(o),
        isKeyword: false,
        ...o
      }));
      this.sortOptions = sortOptions.map(o => ({
        label: o.fullName,
        prop: o.id,
        jnpfKey: o.__config__.jnpfKey,
        ...o
      }));
      this.columnData.columnOptions = options
      if (!this.columnOptions.length) this.columnData.columnList = []
      if (!this.searchOptions.length) this.columnData.searchList = []
      if (!this.sortOptions.length) this.columnData.sortList = []
      this.setBtnValue(this.columnData.btnsList, this.btnsOption)
      this.setBtnValue(this.columnData.columnBtnsList, this.columnBtnsOption)
    } else {
      if (!this.interfaceId) return
      const query = {
        paramList: this.templateJson || []
      }
      this.loading = true
      let fieldsList = this.viewFields
      this.columnOptions = fieldsList.map(o => ({
        label: o.field,
        prop: o.defaultValue,
        fixed: 'none',
        align: 'left',
        jnpfKey: 'input',
        sortable: false,
        width: null,
        __vModel__: o.defaultValue,
        id: o.defaultValue,
        fullName: o.field,
        __config__: {
          jnpfKey: 'input',
        }
      }));
      const interfaceParam = (this.templateJson || [])
        .filter(o => o.useSearch)
        .map(o => {
          let jnpfKey = 'input';
          if (o.dataType === 'int' || o.dataType === 'decimal') jnpfKey = 'inputNumber';
          if (o.dataType === 'datetime') jnpfKey = 'datePicker';
          return {
            id: o.field,
            fullName: o.fieldName,
            label: o.fieldName,
            prop: o.field,
            jnpfKey,
            __vModel__: o.field,
            __config__: { isFromParam: true, jnpfKey },
          };
        });
      this.searchOptions = fieldsList.map(o => ({
        label: o.field,
        prop: o.defaultValue,
        jnpfKey: 'input',
        value: '',
        searchType: 1,
        __vModel__: o.defaultValue,
        id: o.defaultValue,
        fullName: o.field,
        searchMultiple: false,
        __config__: {
          label: o.field,
          jnpfKey: 'input',
        }
      }));
      let searchOptions = this.searchOptions.filter(o => !interfaceParam.some(e => e.id === o.id));
      this.searchOptions = [...interfaceParam, ...searchOptions];
      this.sortOptions = fieldsList.map(o => ({
        label: o.field,
        prop: o.defaultValue,
        id: o.defaultValue,
        fullName: o.field,
        jnpfKey: o,
      }));
      this.groupFieldOptions = fieldsList.map(o => ({
        label: o.field,
        prop: o.defaultValue,
        jnpfKey: 'input',
        __vModel__: o.defaultValue,
        id: o.defaultValue,
        fullName: o.field,
        __config__: {
          label: o.field,
          jnpfKey: 'input',
        }
      }));
      if (!this.columnOptions.length) this.columnData.columnList = []
      if (!this.searchOptions.length) this.columnData.searchList = []
    }
    this.$nextTick(() => {
      this.columnData.columnList = this.setListValue(this.columnData.columnList, JSON.parse(JSON.stringify(this.columnOptions)), 'column')
      this.columnData.searchList = this.setListValue(this.columnData.searchList, JSON.parse(JSON.stringify(this.searchOptions)), "search")
      this.loading = false
    })
    this.btnsList = this.columnData.btnsList.map(o => o.value)
    this.columnBtnsList = this.columnData.columnBtnsList.map(o => o.value)
  },
  mounted() {
    this.setSort()
    this.buildOptions(this.columnData.searchList)
  },
  methods: {
    buildOptions(componentList) {
      if (this.webType == 4) return;
      componentList.forEach(cur => {
        const config = cur.__config__;
        if (dyOptionsList.includes(config.jnpfKey)) {
          if (config.dataType === 'dictionary' && config.dictionaryType) {
            cur.options = [];
            getDictionaryDataSelector(config.dictionaryType).then(res => {
              cur.options = res.data.list;
            });
          }
          if (config.dataType === 'dynamic' && config.propsUrl) {
            cur.options = [];
            const query = { paramList: config.templateJson || [] };
            getDataInterfaceRes(config.propsUrl, query).then(res => {
              cur.options = Array.isArray(res.data) ? res.data : [];
            });
          }
        }
      });
    },
    updateSearchRow(data) {
      this.ExtraConfigModalVisible = false
      this.columnData.searchList[this.activeSearchRowIndex] = data;
    },
    onSearchMultipleChange(record, index) {
      this.columnData.searchList[index].value = getDefaultValue(record);
      this.columnData.searchList[index].__config__.renderKey = +new Date();
    },
    filterPanelShow() {
      this.$refs.conditionpane.show(this.columnData.ruleListApp)
    },
    ruleConfig(data) {
      this.columnData.ruleListApp = data
    },
    setBtnValue(replacedData, data, key) {
      key = key ? key : 'value'
      outer: for (let i = 0; i < replacedData.length; i++) {
        inter: for (let ii = 0; ii < data.length; ii++) {
          if (replacedData[i][key] === data[ii][key]) {
            data[ii] = replacedData[i]
            break inter
          }
        }
      }
    },
    openExtraConfig(record, index) {
      this.ExtraConfigModalVisible = true;
      this.activeSearchRowIndex = index;
      this.$nextTick(() => {
        this.$refs.ExtraConfigModal.init(record, index);
      })
    },
    updateDefaultSortConfig(data) {
      this.columnData.defaultSortConfig = data;
    },
    onJnpfKeyChange(val, record, i) {
      record.__config__.jnpfKey = val;
      let defaultItem = {
        id: record.id,
        fullName: record.fullName,
        label: record.label,
        prop: record.prop,
        jnpfKey: val,
        value: getDefaultValue(record),
        searchType: getSearchType(record),
        __vModel__: record.__vModel__,
        searchMultiple: getSearchMultiple(record),
        isKeyword: false,
        __config__: {
          label: record.label,
          jnpfKey: val,
        },
      };
      if (val === 'datePicker') defaultItem.format = 'yyyy-MM-dd';
      if (val === 'timePicker') defaultItem.format = 'HH:mm:ss';
      if (val === 'select') {
        defaultItem.options = [];
        defaultItem.props = { label: 'fullName', value: 'id' };
        defaultItem.__config__ = {
          ...defaultItem.__config__,
          dataType: 'static',
          propsUrl: '',
          propsName: '',
          templateJson: [],
          dictionaryType: '',
        };
      }
      if (['organizeSelect', 'depSelect', 'userSelect'].includes(val)) {
        defaultItem.isIncludeSubordinate = false;
      }
      this.columnData.searchList[i] = JSON.parse(JSON.stringify(defaultItem));
      this.key = +new Date();
    },
    setListValue(data, defaultData, type) {
      data = data.filter(o => defaultData.some(e => o.prop == e.prop));
      outer: for (let i = 0; i < data.length; i++) {
        inter: for (let ii = 0; ii < defaultData.length; ii++) {
          if (data[i].prop === defaultData[ii].prop) {
            if (type === 'column') {
              defaultData[ii].fixed = data[i].fixed;
              defaultData[ii].align = data[i].align;
              defaultData[ii].width = data[i].width;
              defaultData[ii].sortable = data[i].sortable;
              if (this.webType == 4) defaultData[ii].label = data[i].label;
            }
            if (type === 'search') {
              if (data[i].jnpfKey === defaultData[ii].jnpfKey) {
                defaultData[ii].searchType = data[i].searchType;
                defaultData[ii].searchMultiple = data[i].searchMultiple;
                defaultData[ii].value = data[i].value;
              }
              defaultData[ii].label = data[i].label;
              defaultData[ii].isKeyword = data[i].isKeyword;
              if (this.webType == 4) defaultData[ii] = data[i];
            }
            data[i] = defaultData[ii];
            break inter;
          }
        }
      }
      for (let i = 0; i < data.length; i++) {
        for (let ii = 0; ii < this[type + 'Options'].length; ii++) {
          if (data[i].prop === this[type + 'Options'][ii].prop) {
            this.$refs[type + 'Table'].toggleRowSelection(this[type + 'Options'][ii], true)
          }
        }
      }
      return data;
    },
    editDefaultSortConfig() {
      this.searchConfigVisible = true
      this.$nextTick(() => {
        this.$refs.SearchConfig.init({ list: this.columnData.defaultSortConfig, columnOptions: this.groupFieldOptions })
      })
    },
    updateListValue(selectedRows, type) {
      if (!selectedRows.length) return (this.columnData[type + 'List'] = []);
      const selectedRowKeys = selectedRows.map(o => o.prop);
      this.columnData[type + 'List'] = this.columnData[type + 'List'].filter(o => selectedRowKeys.some(e => o.prop == e));
      for (let i = 0; i < selectedRows.length; i++) {
        if (!this.columnData[type + 'List'].some(o => o.prop === selectedRows[i].prop)) {
          this.columnData[type + 'List'].push(JSON.parse(JSON.stringify(selectedRows[i])));
          if (type == 'search') this.buildOptions([selectedRows[i]]);
        }
      }
    },
    /**
      * 供父组件使用 获取列表JSON
    */
    getData() {
      this.columnData.defaultColumnList = this.columnOptions.map(o => ({
        ...o,
        checked: this.columnData.columnList.some(i => i.prop === o.prop)
      }))
      return this.columnData
    },
    setSort() {
      const el = this.$refs.dragTable.$el.querySelectorAll('.el-table__body-wrapper > table > tbody')[0]
      this.sortable = Sortable.create(el, {
        ghostClass: 'sortable-ghost', // Class name for the drop placeholder,
        setData: function (dataTransfer) {
          dataTransfer.setData('Text', '')
        },
        onEnd: evt => {
          const targetRow = this.columnData.columnList.splice(evt.oldIndex, 1)[0]
          this.columnData.columnList.splice(evt.newIndex, 0, targetRow)
        },
        handle: '.drag-handler',
      })
      // const el1 = this.$refs.dragTableSort.$el.querySelectorAll('.el-table__body-wrapper > table > tbody')[0]
      // Sortable.create(el1, {
      //   ghostClass: 'sortable-ghost', // Class name for the drop placeholder,
      //   setData: function (dataTransfer) {
      //     dataTransfer.setData('Text', '')
      //   },
      //   onEnd: evt => {
      //     const targetRow = this.columnData.sortList.splice(evt.oldIndex, 1)[0]
      //     this.columnData.sortList.splice(evt.newIndex, 0, targetRow)
      //   },
      //   handle: '.drag-handler',
      // })
      const el2 = this.$refs.dragTableSearch.$el.querySelectorAll('.el-table__body-wrapper > table > tbody')[0]
      Sortable.create(el2, {
        ghostClass: 'sortable-ghost', // Class name for the drop placeholder,
        setData: function (dataTransfer) {
          dataTransfer.setData('Text', '')
        },
        onEnd: evt => {
          const targetRow = this.columnData.searchList.splice(evt.oldIndex, 1)[0]
          this.columnData.searchList.splice(evt.newIndex, 0, targetRow)
        },
        handle: '.drag-handler',
      })
    },
    sortSelectionChange(val) {
      this.updateListValue(val, 'sort')
    },
    searchSelectionChange(val) {
      this.updateListValue(val, 'search')
    },
    columnSelectionChange(val) {
      this.updateListValue(val, 'column')
    },
    addCustomBtn() {
      const id = this.jnpf.idGenerator()
      this.columnData.customBtnsList.push({
        value: 'btn_' + id,
        label: '按钮' + id,
        event: {
          func: ''
        }
      })
    },
    editFunc(item) {
      if (!item.event.func) item.event.func = defaultFunc
      this.activeItem = item
      this.customBtnVisible = true
      this.$nextTick(() => {
        this.$refs.customBtn.init('app', item.event, this.webType, this.columnOptions)
      })
    },
    updateScript(data) {
      this.columnData.funcs[this.funcName] = data;
    },
    updateCustomBtn(val) {
      this.columnData.customBtnsList.forEach((ele, index) => {
        if (ele.value == this.activeItem.value) this.$set(this.columnData.customBtnsList[index], 'event', val)
      })
    },
    addFunc(item, type) {
      if (!item) item = defaultFuncs
      this.activeItem = item
      this.funcName = type
      this.formScriptVisible = true
      this.$nextTick(() => {
        this.$refs.formScript.init()
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import './index.scss';
>>> .numRange {
  max-width: none;
}
</style>
