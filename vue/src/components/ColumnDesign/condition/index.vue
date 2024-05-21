<template>
  <section class="condition-pane pd-10">
    <div class="mb-10px" v-if="conditionList.length">
      <span style="font-size: 14px;">分组匹配逻辑</span>
      <el-select v-model="matchLog" placeholder="请选择" style="width:150px;margin-left: 10px;">
        <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
          :value="item.id">
        </el-option>
      </el-select>
    </div>
    <div v-for="(item, index) in conditionList" style="margin-top: 10px;" :key="index">
      <el-row :gutter="5">
        <el-col :span="7" class="mb-10">
          <el-col :span="10" style="line-height:32px">条件逻辑</el-col>
          <el-col :span="14">
            <el-select v-model="item.logic" placeholder="请选择">
              <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
                :value="item.id">
              </el-option>
            </el-select>
          </el-col>
        </el-col>
        <el-col :span="3.5" class="mb-10">
          <el-button icon="el-icon-plus" @click="addItem(index)">添加条件</el-button>
        </el-col>
        <el-col :span="5" class="mb-10 ">
          <el-button icon="icon-ym icon-ym-btn-clearn" class="btn-del"
            @click="delGroup(index)">删除分组</el-button>
        </el-col>
        <el-col :span="24" v-for="(subItem, i) in item.groups" :key="index + i" class="mb-10">
          <el-row :gutter="5">
            <el-col :span="7">
              <el-select v-model="subItem.field" placeholder="请选择" filterable
                @change="fieldNameChange($event, subItem, index,i)">
                <el-option v-for="item in usedFormItems" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="7">
              <el-select v-model="subItem.symbol" placeholder="运算符号" class="condition-select"
                @change="symbolChange($event, subItem, index,i)" filterable>
                <!-- 联动符号下拉框 -->
                <el-option v-for="item in getSymbolOptions(subItem.jnpfKey)" :key="item.id"
                  :label="item.fullName" :value="item.id">
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <template v-if="subItem.jnpfKey === 'inputNumber'">
                <NumRange v-model="subItem.fieldValue" :disabled="subItem.disabled"
                  v-if="subItem.symbol == 'between'"></NumRange>
                <JnpfInputNumber v-else v-model="subItem.fieldValue" placeholder="请输入"
                  :disabled="subItem.disabled" :precision="subItem.precision" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'autoComplete'">
                <JnpfAutoComplete v-model="subItem.fieldValue" placeholder="请选择"
                  :interfaceId="subItem.interfaceId" clearable :disabled="subItem.disabled"
                  :propsValue="subItem.propsValue" :relationField="subItem.relationField"
                  :templateJson="subItem.templateJson" :total="subItem.total" />
              </template>
              <template v-else-if="['radio', 'checkbox', 'select'].includes(subItem.jnpfKey)">
                <template v-if="['null','notNull'].includes(subItem.symbol)">
                  <el-input :disabled="subItem.disabled" v-model="subItem.fieldValue"
                    placeholder="请选择"></el-input>
                </template>
                <el-select v-else :disabled="subItem.disabled" v-model="subItem.fieldValue"
                  :key="subItem.cellKey" placeholder="请选择" :multiple="subItem.multiple" filterable
                  @change="input()">
                  <el-option v-for="(child, index) in subItem.options" :key="index"
                    :disabled="subItem.disabled" :label="child[subItem.props.label]"
                    :value="child[subItem.props.value]"></el-option>
                </el-select>
              </template>
              <template v-else-if="['cascader'].includes(subItem.jnpfKey)">
                <JnpfCascader v-model="subItem.fieldValue" :options="subItem.options"
                  :props="subItem.props" :multiple="subItem.multiple"
                  :placeholder="subItem.placeholder" :clearable="subItem.clearable"
                  :separator="subItem.separator" :filterable="subItem.filterable"
                  :disabled="subItem.disabled" :show-all-levels="subItem.showAllLevels"
                  :key='subItem.cellKey' />
              </template>
              <template v-else-if="['treeSelect'].includes(subItem.jnpfKey)">
                <JnpfTreeSelect v-model="subItem.fieldValue" conditionFilter
                  :options="subItem.options" :props="subItem.props" :key="subItem.cellKey"
                  :placeholder="subItem.placeholder" :clearable="subItem.clearable"
                  :multiple="subItem.multiple" :filterable="subItem.filterable"
                  :disabled="subItem.disabled" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'calculate'">
                <NumRange v-model="subItem.fieldValue" v-if="subItem.symbol == 'between'"
                  :precision="subItem.precision" :disabled="subItem.disabled"></NumRange>
                <el-input-number v-else v-model="subItem.fieldValue" placeholder="请输入"
                  :precision="subItem.precision ||0" :disabled="subItem.disabled" :controls="false"
                  controls-position="right" />
              </template>
              <template v-else-if="['rate', 'slider'].includes(subItem.jnpfKey)">
                <NumRange v-model="subItem.fieldValue" v-if="subItem.symbol == 'between'"
                  :precision="precision" :disabled="subItem.disabled"></NumRange>
                <el-input-number v-else v-model="subItem.fieldValue" placeholder="请输入"
                  :precision="precision" :disabled=" subItem.disabled" controls-position="right" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'switch'">
                <el-switch v-model="subItem.fieldValue" :active-value="1"
                  :disabled="subItem.disabled" :inactive-value="0" style="margin-top: 5px;" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'timePicker'">
                <el-time-picker v-if="subItem.symbol == 'between'" :disabled="subItem.disabled"
                  v-model="subItem.fieldValue" key="time1" style="width: 100%;" range-separator="至"
                  placeholder="请选择" clearable :is-range="true" @input="input()"
                  :value-format="subItem.format" :format="subItem.format">
                </el-time-picker>
                <el-time-picker v-else v-model="subItem.fieldValue" key="time2" @input="input()"
                  placeholder="请选择" clearable :disabled="subItem.disabled" style="width: 100%;"
                  :value-format="subItem.format" :format="subItem.format">
                </el-time-picker>
              </template>
              <template v-else-if="['datePicker'].includes(subItem.jnpfKey)">
                <template v-if="subItem.symbol == 'between'">
                  <JnpfDateRangePicker v-model="subItem.fieldValue" clearable
                    :disabled="subItem.disabled" key="year1" placeholder="请选择" separator="至"
                    @input="input()" startPlaceholder="开始日期" endPlaceholder="结束日期"
                    style="width: 100%;" :format="subItem.format" />
                </template>
                <JnpfDatePicker v-else v-model="subItem.fieldValue" clearable
                  :disabled="subItem.disabled" key="year2" placeholder="请选择" @input="input()"
                  style="width: 100%;" :format="subItem.format">
                </JnpfDatePicker>
              </template>
              <template v-else-if="['createTime', 'modifyTime'].includes(subItem.jnpfKey)">
                <template v-if="subItem.symbol == 'between'">
                  <el-date-picker v-model="subItem.fieldValue" clearable value-format="timestamp"
                    :disabled="subItem.disabled" key="year1" placeholder="请选择" range-separator="至"
                    type="datetimerange" @input="input()" startPlaceholder="开始日期"
                    endPlaceholder="结束日期" style="width: 100%;" />
                </template>
                <el-date-picker v-else v-model="subItem.fieldValue" clearable type="datetime"
                  value-format="timestamp" :disabled="subItem.disabled" key="year2"
                  placeholder="请选择" @input="input()">
                </el-date-picker>
              </template>
              <template v-else-if="['organizeSelect', 'currOrganize'].includes(subItem.jnpfKey)">
                <JnpfOrganizeSelect v-model="subItem.fieldValue" :disabled="subItem.disabled"
                  placeholder="请选择" ref="comselect" clearable :multiple="subItem.multiple"
                  :ableIds='subItem.ableIds' :selectType="subItem.selectType" />
              </template>
              <template v-else-if="['depSelect'].includes(subItem.jnpfKey)">
                <JnpfDepSelect v-model="subItem.fieldValue" placeholder="请选择"
                  :selectType="subItem.selectType" :ableIds="subItem.ableIds"
                  :multiple="subItem.multiple" clearable :disabled="subItem.disabled" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'popupTableSelect'">
                <JnpfPopupTableSelect v-model="subItem.fieldValue"
                  :placeholder="subItem.placeholder" :interfaceId="subItem.interfaceId"
                  :multiple="subItem.multiple" :columnOptions="subItem.columnOptions"
                  :propsValue="subItem.propsValue" :relationField="subItem.relationField"
                  :hasPage="subItem.hasPage" :pageSize="subItem.pageSize"
                  :popupType="subItem.popupType" :popupTitle="subItem.popupTitle"
                  :popupWidth="subItem.popupWidth" :filterable="subItem.filterable"
                  :disabled="subItem.disabled" clearable :templateJson="subItem.templateJson" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'relationForm'">
                <JnpfRelationForm v-model="subItem.fieldValue" placeholder="请选择"
                  :modelId="subItem.modelId" clearable :multiple="subItem.multiple"
                  :columnOptions="subItem.columnOptions" :relationField="subItem.relationField"
                  :hasPage="subItem.hasPage" :pageSize="subItem.pageSize"
                  :disabled="subItem.disabled" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'popupSelect'">
                <JnpfPopupSelect v-model="subItem.fieldValue" placeholder="请选择"
                  :interfaceId="subItem.interfaceId" clearable :multiple="subItem.multiple"
                  :columnOptions="subItem.columnOptions" :propsValue="subItem.propsValue"
                  :relationField="subItem.relationField" :hasPage="subItem.hasPage"
                  :pageSize="subItem.pageSize" :popupType="subItem.popupType"
                  :templateJson="item.templateJson" :popupTitle="subItem.popupTitle"
                  :popupWidth="subItem.popupWidth" :disabled="subItem.disabled" />
              </template>
              <template v-else-if="['userSelect'].includes(subItem.jnpfKey)">
                <JnpfUserSelect v-model="subItem.fieldValue" class="item" placeholder="请选择"
                  clearable :disabled="subItem.disabled"
                  :selectType="(subItem.selectType!='all' && subItem.selectType!='custom')?'all':subItem.selectType"
                  :ableIds="subItem.ableIds" :multiple="subItem.multiple" />
              </template>
              <template v-else-if="['usersSelect'].includes(subItem.jnpfKey)">
                <jnpf-users-select v-model="subItem.fieldValue" clearable
                  :selectType="subItem.selectType" :ableIds="subItem.ableIds" :key="subItem.cellKey"
                  :multiple="subItem.multiple" :disabled="subItem.disabled" />
              </template>
              <template v-else-if="['createUser', 'modifyUser'].includes(subItem.jnpfKey)">
                <JnpfUserSelect v-model="subItem.fieldValue" placeholder="请选择"
                  :key="subItem.cellKey" :multiple="subItem.multiple" clearable
                  :disabled="subItem.disabled" />
              </template>
              <template v-else-if="['posSelect'].includes(subItem.jnpfKey)">
                <JnpfPosSelect v-model="subItem.fieldValue" placeholder="请选择" clearable
                  class="subItem" :selectType="subItem.selectType" :ableIds="subItem.ableIds"
                  :key="subItem.cellKey" :multiple="subItem.multiple"
                  :disabled="subItem.disabled" />
              </template>
              <template v-else-if="[ 'currPosition'].includes(subItem.jnpfKey)">
                <JnpfPosSelect v-model="subItem.fieldValue" placeholder="请选择" clearable
                  class="subItem" :multiple="subItem.multiple" :disabled="subItem.disabled"
                  :ableIds="subItem.ableIds" :selectType="subItem.selectType" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'areaSelect'">
                <JnpfAreaSelect v-model="subItem.fieldValue" placeholder="请选择"
                  :level="subItem.level" :multiple="subItem.multiple" clearable
                  :key="subItem.cellKey" :disabled="subItem.disabled" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'groupSelect'">
                <JnpfGroupSelect v-model="subItem.fieldValue" :multiple="subItem.multiple"
                  placeholder="请选择" clearable :disabled="subItem.disabled"
                  :ableIds="subItem.ableIds" :selectType="subItem.selectType" />
              </template>
              <template v-else-if="subItem.jnpfKey === 'roleSelect'">
                <JnpfRoleSelect v-model="subItem.fieldValue" :multiple="subItem.multiple"
                  placeholder="请选择" clearable :disabled="subItem.disabled" :key="subItem.cellKey"
                  :ableIds="subItem.ableIds" :selectType="subItem.selectType" />
              </template>
              <!-- 其他情况 -->
              <template v-else>
                <el-input v-model="subItem.fieldValue" :disabled="subItem.disabled"
                  placeholder="请输入" @input="input"></el-input>
              </template>
            </el-col>
            <el-col :span="2" style="text-align:right">
              <div class="icon-ym icon-ym-btn-clearn clearn-color" @click="delItem(index,i)"
                style="width:100%; margin: 3px -29px;" />
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </div>
    <template>
      <div class="link-text" @click="addGroup()" v-if="type"><i
          class="icon-ym icon-ym-btn-add add-text"></i>添加分组</div>
      <el-button @click="addGroup" v-else>添加分组</el-button>
    </template>
  </section>
</template>

<script>
import { getDictionaryDataSelector } from '@/api/systemData/dictionary';
import { getDataInterfaceRes } from '@/api/systemData/dataInterface';
import { dyOptionsList } from '@/components/Generator/generator/comConfig'
const logicOptions = [
  { id: 'and', fullName: 'and' },
  { id: 'or', fullName: 'or' },
];
const baseSymbolOptions = [
  { id: 'like', fullName: '包含' },
  { id: 'notLike', fullName: '不包含' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const rangeSymbolOptions = [
  { id: '>=', fullName: '大于等于' },
  { id: '>', fullName: '大于' },
  { id: '==', fullName: '等于' },
  { id: '<=', fullName: '小于等于' },
  { id: '<', fullName: '小于' },
  { id: '<>', fullName: '不等于' },
  { id: 'between', fullName: '介于' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const switchSymbolOptions = [
  { id: '==', fullName: '等于' },
  { id: '<>', fullName: '不等于' },
];
const selectSymbolOptions = [
  ...switchSymbolOptions,
  { id: 'in', fullName: '包含任意一个' },
  { id: 'notIn', fullName: '不包含任意一个' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const relationFormSymbolOptions = [...switchSymbolOptions, { id: 'null', fullName: '为空' }, { id: 'notNull', fullName: '不为空' }];
const useRangeSymbolList = ['calculate', 'inputNumber', 'rate', 'slider', 'datePicker', 'timePicker', 'createTime', 'modifyTime'];
const useSelectSymbolList = [
  'radio',
  'checkbox',
  'select',
  'treeSelect',
  'cascader',
  'areaSelect',
  'organizeSelect',
  'depSelect',
  'posSelect',
  'userSelect',
  'usersSelect',
  'roleSelect',
  'groupSelect',
  'createUser',
  'modifyUser',
  'currOrganize',
  'currPosition',
  'popupTableSelect',
];
const useSwitchSymbolList = ['switch'];
const useRelationFormSymbolList = ['relationForm', 'popupSelect'];
export default {
  props: {
    modelType: {
      type: [String, Number],
      default: ''
    },
    columnOptions: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    },
    options: {
      type: Array,
      default: () => []
    },
    type: {
      type: Number,
      default: 0
    },
    matchLogic: {
      type: String,
      default: "and"
    }
  },
  data() {
    return {
      precision: 0,
      nowJnpfKey: undefined,
      dialogVisible: false,
      conditionList: [],
      matchLog: this.matchLogic,
      logicOptions,
      baseSymbolOptions,
      rangeSymbolOptions,
      selectSymbolOptions,
      switchSymbolOptions,
      useRangeSymbolList,
      useSelectSymbolList,
      useSwitchSymbolList,
      relationFormSymbolOptions,
      useRelationFormSymbolList
    };
  },
  computed: {
    usedFormItems() {
      let list = [];
      const loop = (data, parent) => {
        if (!data) return;
        if (
          data.__config__ &&
          !["table"].includes(data.__config__.jnpfKey) &&
          data.__config__.children &&
          Array.isArray(data.__config__.children)
        ) {
          loop(data.__config__.children, data);
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent));
        if (
          //下拉树形,关联表单 关联表单属性 弹窗选择 弹窗选择属性 下拉表格
          //不支持控件：开关、文件上传、图片上传、颜色选择、评分、滑块、富文本、链接、
          //按钮、文本、提示、二维码、条形码、用户组件、设计子表。
          data.__vModel__ &&
          !['relationFormAttr', 'popupAttr', 'uploadFile', 'uploadImg', 'colorPicker', 'editor', 'link', 'button', 'text', 'alert', 'table', 'sign'].includes(data.__config__.jnpfKey)
        )
          list.push(data);
      };
      loop(this.columnOptions);
      const formItems = list;
      this.buildOptions(formItems)
      return formItems;
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          this.conditionList = val;
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    getSymbolOptions(jnpfKey) {
      if (this.useSwitchSymbolList.includes(jnpfKey)) return this.switchSymbolOptions;
      if (this.useRelationFormSymbolList.includes(jnpfKey)) return this.relationFormSymbolOptions;
      if (this.useRangeSymbolList.includes(jnpfKey)) return this.rangeSymbolOptions;
      if (this.useSelectSymbolList.includes(jnpfKey)) return this.selectSymbolOptions;
      if (['location'].includes(jnpfKey)) return this.baseSymbolOptions;
      return [...switchSymbolOptions, ...this.baseSymbolOptions];
    },
    input() {
      this.$forceUpdate();
    },
    buildOptions(componentList) {
      componentList.forEach(cur => {
        cur.disabled = false;
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
      return componentList;
    },
    getData() {
      return {
        matchLogic: JSON.parse(JSON.stringify(this.matchLog)),
        conditionList: JSON.parse(JSON.stringify(this.conditionList)),
      };
    },
    handleClose() {
      this.dialogVisible = false;
    },
    show(data) {
      this.dialogVisible = true;
      if (data != null) {
        this.init(data);
      }
    },
    addGroup() {
      let item = {
        logic: "and",
        groups: [{
          field: '',
          symbol: '',
          jnpfKey: '',
          fieldValueType: 2,
          fieldValue: undefined,
          fieldValueJnpfKey: '',
          cellKey: +new Date(),
        }]
      }
      this.conditionList.push(item)
    },
    addItem(index) {
      this.conditionList[index].groups.push({
        field: '',
        symbol: '',
        jnpfKey: '',
        fieldValueType: 2,
        fieldValue: undefined,
        fieldValueJnpfKey: '',
        cellKey: +new Date(),
      })
    },
    delGroup(index) {
      this.conditionList.splice(index, 1)
    },
    delItem(index, childIndex) {
      this.conditionList[index].groups.splice(childIndex, 1)
    },
    fieldNameChange(val, item, i, childIndex) {
      item.cellKey = +new Date()
      let obj = this.usedFormItems.filter(o => o.id == val)[0];
      item.fieldName = obj.__config__.label;
      item.jnpfKey = obj.__config__.jnpfKey;
      obj.jnpfKey = obj.__config__.jnpfKey;
      item = { ...item, ...obj };
      if (item.jnpfKey == 'rate' && item.allowHalf) this.precision = 1
      if (item.jnpfKey != this.nowJnpfKey) {
        item.symbol = undefined;
      }
      item.disabled = ['null', 'notNull'].includes(item.symbol);
      item.multiple = ['in', 'notIn'].includes(item.symbol);
      // 清空数据
      item.symbol = undefined;
      item.fieldValue = undefined;
      if (item.jnpfKey == 'switch') item.fieldValue = 0
      this.$set(this.conditionList[i].groups, childIndex, item);
      this.nowJnpfKey = item.jnpfKey;
    },
    // 比较符号改变事件
    symbolChange(val, item, i, childIndex) {
      item.symbol = val
      item.cellKey = +new Date()
      item.disabled = ['null', 'notNull'].includes(val)
      item.multiple = ["in", "notIn"].includes(val);
      if (item.jnpfKey == 'cascader') {
        item.props.multiple = false
        if (["in", "notIn"].includes(val)) item.props.multiple = true
      }
      item.fieldValue = undefined
      if (item.jnpfKey == 'switch') item.fieldValue = 0
      if (["in", "notIn"].includes(val)) item.fieldValue = []
      this.$set(this.conditionList[i].groups, childIndex, item);
    },
  },
};
</script>

<style lang="scss" scoped>
.clearn-color {
  color: #ff3a3a !important;
  font-size: 24px;
  cursor: pointer;
}
.btn-del {
  >>> .icon-ym {
    font-size: 12px !important;
  }
}
.fieldValue {
  >>> .el-input__inner,
  .el-date-editor.el-input {
    width: 100%;
  }
}
.wrap {
  padding: 0 4px;
}

.title {
  font-size: 15px;
  color: black;
  font-weight: 400;
}
.el-select {
  width: 100%;
}
.el-cascader {
  width: 100%;
}
.el-input-number {
  width: 100%;
}
.el-icon-delete {
  line-height: 32px;
}
.numRange {
  max-width: inherit;
}
>>> .JNPF-selectTree {
  width: 100%;
}

>>> .popupSelect-container {
  width: 100%;
}
.condition-list-header {
  >>> .el-col {
    padding: 0 4px;
  }
}

.link-text {
  color: #1890ff;
  cursor: pointer;
  margin-top: 10px;
  display: block;
  .add-text {
    font-size: 14px;
    margin-right: 4px;
  }
}
</style>
