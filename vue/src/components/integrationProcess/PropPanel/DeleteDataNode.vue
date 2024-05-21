<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20">
        <el-form-item>
          <div slot="label" class="form-item-label">目标表单</div>
          <FlowFormModal :value="formConf.formId" :title="formConf.formName" :flowType="1"
            :clearable='false' :allowClear="false" @change="onFormIdChange" placeholder="请选择表单" />
        </el-form-item>
        <el-form-item v-if="integrateType == 1">
          <div slot="label" class="form-item-label">删除条件
          </div>
          <section class="condition-pane pd-10">
            <div class="mb-10px" v-if="ruleList.length">
              <span style="font-size: 14px;">分组匹配逻辑</span>
              <el-select v-model="ruleMatchLogic" placeholder="请选择"
                style="width:150px;margin-left: 10px;">
                <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
              </el-select>
            </div>
            <div v-for="(item, index) in ruleList" :key="index" class="mb-10px">
              <el-row :gutter="8" style="margin-bottom: -8px;">
                <el-col :span="7" class="mb-10">
                  <el-col :span="11" style="line-height:32px">条件逻辑</el-col>
                  <el-col :span="13">
                    <el-select v-model="item.logic" placeholder="请选择">
                      <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-col>
                </el-col>
                <el-col :span="3.5">
                  <el-button icon="icon-ym icon-ym-btn-add" class="condition-btn"
                    @click="addItem(index)">添加条件</el-button>
                </el-col>
                <el-col :span="5">
                  <el-button icon="icon-ym icon-ym-nav-close" class="condition-btn"
                    @click="delGroup(index)">删除分组</el-button>
                </el-col>
              </el-row>
              <el-row :gutter="8" v-for="(child, childIndex) in item.groups"
                :key="index + childIndex" class="mt-10px">
                <el-col :span="6">
                  <el-select v-model="child.field" placeholder="请选择字段" clearable filterable
                    style="width:100%" @change="onFieldChange(child, $event,index, childIndex)">
                    <el-option v-for="item in getRuleOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="5">
                  <el-select v-model="child.symbol" placeholder="运算符号" clearable filterable
                    style="width:100%" @change="onSymbolChange($event, child,index,childIndex)">
                    <el-option v-for="item in getSymbolOptions(child.jnpfKey)" :key="item.id"
                      :label="item.fullName" :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-select v-model="child.fieldValueType" placeholder="请选择字段"
                    :disabled="['null', 'notNull'].includes(child.symbol)" style="width:100%"
                    @change="onTypeChange(child)" @input="input()">
                    <el-option v-for="item in sourceTypeOptions" :key="item.id"
                      :label="item.fullName" :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select v-model="child.fieldValue" placeholder="请选择字段" clearable
                    :disabled="['null', 'notNull'].includes(child.symbol)" style="width:100%"
                    v-if="child.fieldValueType === 1" @change="input()">
                    <el-option v-for="item in usedFormItems" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                  <template v-if="child.fieldValueType === 2">
                    <template v-if="child.jnpfKey === 'inputNumber'">
                      <NumRange v-model="child.fieldValue" :disabled="child.disabled"
                        v-if="child.symbol == 'between'" style="width: 100%;"></NumRange>
                      <el-input-number v-else v-model="child.fieldValue" placeholder="请输入"
                        :disabled="child.disabled" :precision="child.precision" @change="input"
                        controls-position="right" style="width: 100%;" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'autoComplete'">
                      <JnpfAutoComplete v-model="child.fieldValue" placeholder="请选择"
                        :interfaceId="child.interfaceId" clearable :disabled="child.disabled"
                        :propsValue="child.propsValue" :relationField="child.relationField"
                        :templateJson="child.templateJson" :total="child.total" />
                    </template>
                    <template v-else-if="
                  ['radio', 'checkbox', 'select'].includes(child.jnpfKey)
                ">
                      <template v-if="['null','notNull'].includes(child.symbol)">
                        <el-input :disabled="child.disabled" v-model="child.fieldValue"
                          placeholder="请选择"></el-input>
                      </template>
                      <el-select v-else :disabled="child.disabled" v-model="child.fieldValue"
                        :key='child.cellKey' placeholder="请选择" :multiple="child.multiple" filterable
                        @change="input">
                        <el-option v-for="(option, index) in child.options" :key="index"
                          :disabled="option.disabled" :label="option[child.props.label]"
                          :value="option[child.props.value]"></el-option>
                      </el-select>
                    </template>
                    <template v-else-if="['cascader'].includes(child.jnpfKey)">
                      <el-cascader v-model="child.fieldValue" :options="child.options"
                        :props="child.props" :placeholder="child.placeholder" :key='child.cellKey'
                        :clearable="child.clearable" :show-all-levels="child.showAllLevels"
                        :separator="child.separator" :filterable="child.filterable"
                        :disabled="child.disabled" />
                    </template>
                    <template v-else-if="['treeSelect'].includes(child.jnpfKey)">
                      <JnpfTreeSelect v-model="child.fieldValue" conditionFilter
                        :options="child.options" :props="child.props" @change="input"
                        :placeholder="child.placeholder" :clearable="child.clearable"
                        :multiple="child.multiple" :filterable="child.filterable"
                        :disabled="child.disabled" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'calculate'">
                      <NumRange v-model="child.fieldValue" v-if="child.symbol == 'between'"
                        :precision="child.precision" :disabled="child.disabled"
                        style="width: 100%;"></NumRange>
                      <el-input-number v-else v-model="child.fieldValue" placeholder="请输入"
                        :precision="child.precision ||0" :disabled="child.disabled" @change="input"
                        controls-position="right" style="width: 100%;" />
                    </template>
                    <template v-else-if="['rate', 'slider'].includes(child.jnpfKey)">
                      <NumRange v-model="child.fieldValue" v-if="child.symbol == 'between'"
                        :precision="child.precision" :disabled="child.disabled"
                        style="width: 100%;"></NumRange>
                      <el-input-number v-else v-model="child.fieldValue" placeholder="请输入"
                        :precision="0" :disabled=" child.disabled" @change="input"
                        controls-position="right" style="width: 100%;" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'switch'">
                      <el-switch v-model="child.fieldValue" :active-value="1" @change="input"
                        :disabled="child.disabled" :inactive-value="0" style="margin-top: 5px;" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'timePicker'">
                      <el-time-picker v-if="child.symbol == 'between'" :disabled="child.disabled"
                        v-model="child.fieldValue" key="time1" style="width: 100%;"
                        range-separator="至" placeholder="请选择" clearable :is-range="true"
                        @input="input()" :value-format="child.format" :format="child.format">
                      </el-time-picker>
                      <el-time-picker v-else v-model="child.fieldValue" key="time2" @input="input()"
                        placeholder="请选择" clearable :disabled="child.disabled"
                        :value-format="child.format" :format="child.format" style="width: 100%;">
                      </el-time-picker>
                    </template>
                    <template v-else-if="['datePicker'].includes(child.jnpfKey)">
                      <template v-if="child.symbol == 'between'">
                        <JnpfDateRangePicker v-model="child.fieldValue" clearable
                          :disabled="child.disabled" key="year1" placeholder="请选择" separator="至"
                          @input="input()" startPlaceholder="开始日期" endPlaceholder="结束日期"
                          style="width: 100%;" :format="child.format" />
                      </template>
                      <JnpfDatePicker v-else v-model="child.fieldValue" clearable
                        :disabled="child.disabled" key="year2" placeholder="请选择" @input="input()"
                        :format="child.format" style="width: 100%;">
                      </JnpfDatePicker>
                    </template>
                    <template v-else-if="['createTime', 'modifyTime'].includes(child.jnpfKey)
                ">
                      <template v-if="child.symbol == 'between'">
                        <el-date-picker v-model="child.fieldValue" clearable
                          value-format="timestamp" :disabled="child.disabled" key="year1"
                          placeholder="请选择" range-separator="至" type="datetimerange"
                          @input="input()" startPlaceholder="开始日期" endPlaceholder="结束日期"
                          style="width: 100%;" />
                      </template>
                      <el-date-picker v-else v-model="child.fieldValue" clearable type="datetime"
                        value-format="timestamp" :disabled="child.disabled" key="year2"
                        placeholder="请选择" @input="input()" style="width: 100%;">
                      </el-date-picker>
                    </template>
                    <template
                      v-else-if="['organizeSelect', 'currOrganize'].includes(child.jnpfKey)">
                      <JnpfOrganizeSelect v-model="child.fieldValue" :disabled="child.disabled"
                        placeholder="请选择" ref="comselect" clearable :multiple="child.multiple"
                        :ableIds='child.ableIds' :selectType="child.selectType" @change="input" />
                    </template>
                    <template v-else-if="['depSelect'].includes(child.jnpfKey)">
                      <JnpfDepSelect v-model="child.fieldValue" placeholder="请选择"
                        :selectType="child.selectType" :ableIds="child.ableIds"
                        :multiple="child.multiple" clearable :disabled="child.disabled"
                        @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'popupTableSelect'">
                      <JnpfPopupTableSelect v-model="child.fieldValue"
                        :placeholder="child.placeholder" :interfaceId="child.interfaceId"
                        :multiple="child.multiple" :columnOptions="child.columnOptions"
                        :propsValue="child.propsValue" :relationField="child.relationField"
                        :hasPage="child.hasPage" :pageSize="child.pageSize"
                        :popupType="child.popupType" :popupTitle="child.popupTitle"
                        :popupWidth="child.popupWidth" :filterable="child.filterable"
                        :templateJson="child.templateJson" :disabled="child.disabled" clearable
                        @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'relationForm'">
                      <JnpfRelationForm v-model="child.fieldValue" placeholder="请选择"
                        :modelId="child.modelId" clearable :multiple="child.multiple"
                        :columnOptions="child.columnOptions" :relationField="child.relationField"
                        :hasPage="child.hasPage" :pageSize="child.pageSize"
                        :disabled="child.disabled" @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'popupSelect'">
                      <JnpfPopupSelect v-model="child.fieldValue" placeholder="请选择"
                        :interfaceId="child.interfaceId" clearable :multiple="child.multiple"
                        :columnOptions="child.columnOptions" :propsValue="child.propsValue"
                        :relationField="child.relationField" :hasPage="child.hasPage"
                        :pageSize="child.pageSize" :popupType="child.popupType"
                        :templateJson="item.templateJson" :popupTitle="child.popupTitle"
                        :popupWidth="child.popupWidth" :disabled="child.disabled" @change="input" />
                    </template>
                    <template v-else-if="['userSelect'].includes(child.jnpfKey)">
                      <JnpfUserSelect v-model="child.fieldValue" class="item" placeholder="请选择"
                        clearable :disabled="child.disabled"
                        :selectType="(child.selectType!='all' && child.selectType!='custom')?'all':child.selectType"
                        :ableIds="child.ableIds" :multiple="child.multiple" @change="input" />
                    </template>
                    <template v-else-if="['usersSelect'].includes(child.jnpfKey)">
                      <jnpf-users-select v-model="child.fieldValue" clearable
                        :selectType="child.selectType" :ableIds="child.ableIds"
                        :multiple="child.multiple" :disabled="child.disabled" @change="input" />
                    </template>
                    <template v-else-if="['createUser', 'modifyUser'].includes(child.jnpfKey)">
                      <JnpfUserSelect v-model="child.fieldValue" placeholder="请选择"
                        :multiple="child.multiple" clearable :disabled="child.disabled"
                        @change="input" />
                    </template>
                    <template v-else-if="['posSelect'].includes(child.jnpfKey)">
                      <JnpfPosSelect v-model="child.fieldValue" placeholder="请选择" clearable
                        class="child" :selectType="child.selectType" :ableIds="child.ableIds"
                        :multiple="child.multiple" :disabled="child.disabled" @change="input" />
                    </template>
                    <template v-else-if="[ 'currPosition'].includes(child.jnpfKey)">
                      <JnpfPosSelect v-model="child.fieldValue" placeholder="请选择" clearable
                        class="child" :multiple="child.multiple" :disabled="child.disabled"
                        :ableIds="child.ableIds" :selectType="child.selectType" @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'areaSelect'">
                      <JnpfAreaSelect v-model="child.fieldValue" placeholder="请选择"
                        :level="child.level" :multiple="child.multiple" clearable
                        :disabled="child.disabled" @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'groupSelect'">
                      <JnpfGroupSelect v-model="child.fieldValue" :multiple="child.multiple"
                        placeholder="请选择" clearable :disabled="child.disabled"
                        :ableIds="child.ableIds" :selectType="child.selectType" @change="input" />
                    </template>
                    <template v-else-if="child.jnpfKey === 'roleSelect'">
                      <JnpfRoleSelect v-model="child.fieldValue" :multiple="child.multiple"
                        placeholder="请选择" clearable :disabled="child.disabled"
                        :ableIds="child.ableIds" :selectType="child.selectType" @change="input" />
                    </template>
                    <!-- 其他情况 -->
                    <template v-else>
                      <el-input v-model="child.fieldValue" :disabled="child.disabled"
                        placeholder="请输入" @input="input"></el-input>
                    </template>
                  </template>
                </el-col>
                <el-col :span="1" class="text-center del-icon">
                  <i class="icon-ym icon-ym-btn-clearn" @click="delItem(index, childIndex)" />
                </el-col>
              </el-row>
            </div>
            <div class="link-text" @click="addGroup()"><i
                class="icon-ym icon-ym-btn-add add-text"></i>添加分组</div>
          </section>
        </el-form-item>
        <el-form-item v-if="integrateType != 1">
          <div slot="label" class="form-item-label">唯一性验证</div>
          <section class="condition-pane pd-10">
            <div class="mb-10px" v-if="ruleList.length">
              <span style="font-size: 14px;">分组匹配逻辑</span>
              <el-select v-model="ruleMatchLogic" placeholder="请选择"
                style="width:150px;margin-left: 10px;">
                <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
              </el-select>
            </div>
            <div v-for="(item, index) in ruleList" :key="index" class="mb-10px">
              <el-row :gutter="8" style="margin-bottom: -8px;">
                <el-col :span="7" class="mb-10">
                  <el-col :span="11" style="line-height:32px">条件逻辑</el-col>
                  <el-col :span="13">
                    <el-select v-model="item.logic" placeholder="请选择">
                      <el-option v-for="item in logicOptions" :key="item.id" :label="item.fullName"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-col>
                </el-col>
                <el-col :span="3.5">
                  <el-button icon="icon-ym icon-ym-btn-add" class="condition-btn"
                    @click="addItem(index)">添加条件</el-button>
                </el-col>
                <el-col :span="5">
                  <el-button icon="icon-ym icon-ym-nav-close" class="condition-btn"
                    @click="delGroup(index)">删除分组</el-button>
                </el-col>
              </el-row>
              <el-row :gutter="5" v-for="(child, childIndex) in item.groups"
                :key="index + childIndex" class="mt-10px">
                <el-col :span="8">
                  <el-select v-model="child.field" placeholder="请选择目标表单字段" filterable
                    style="width:100%"
                    @change="onFieldChange(child, child.field, index, childIndex)">
                    <el-option v-for="item in getRuleOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="5">
                  <el-select v-model="child.symbol" placeholder="运算符号" filterable style="width:100%"
                    disabled>
                    <el-option v-for="item in symbolOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="10">
                  <el-select v-model="child.fieldValue" placeholder="请选择触发表单字段" filterable
                    style="width:100%" clearable>
                    <el-option v-for="item in usedFormItems" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="1" class="text-center">
                  <i class="icon-ym icon-ym-btn-clearn del-icon"
                    @click="delItem(index, childIndex)" />
                </el-col>
              </el-row>
            </div>
            <div class="link-text" @click="addGroup()"><i
                class="icon-ym icon-ym-btn-add add-text"></i>添加分组</div>
          </section>
        </el-form-item>
        <el-form-item v-if="integrateType != 1">
          <div slot="label" class="form-item-label">删除内容</div>
          <el-radio-group v-model="deleteRule">
            <el-radio v-for="item in deleteRuleOptions" :key="item.id"
              :label="item.id">{{item.fullName}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-scrollbar>
  </section>
</template>
<script>
import FlowFormModal from './FormListModal';
import { sourceTypeOptions, logicOptions, symbolOptions, baseSymbolOptions, rangeSymbolOptions, selectSymbolOptions, switchSymbolOptions, relationFormSymbolOptions, useRangeSymbolList, useSelectSymbolList, useSwitchSymbolList, useRelationFormSymbolList } from '../FlowCard/define.js';
import { getDictionaryDataSelector } from '@/api/systemData/dictionary';
import { getDataInterfaceRes } from '@/api/systemData/dataInterface';
import { dyOptionsList } from '@/components/Generator/generator/comConfig'
const deleteRuleOptions = [
  { id: 0, fullName: '删除未找到的数据' },
  { id: 1, fullName: '删除已找到的数据' },
];
const emptyChildItem = { field: '', symbol: '', fieldValueType: 1, fieldValue: undefined, jnpfKey: '', fieldValueJnpfKey: '', cellKey: +new Date() };
const emptyItem = { logic: 'and', groups: [emptyChildItem] };

export default {
  name: 'DeleteDataNode',
  components: { FlowFormModal },
  props: {
    formConf: {
      type: Object,
      default: () => { }
    },
    integrateType: {
      type: Number,
      default: 1
    },
    formFieldsOptions: {
      type: Array,
      default: () => []
    },
    getFormFieldList: {
      type: Function,
      default: null
    },
    usedFormItems: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      deleteRuleOptions,
      emptyItem,
      sourceTypeOptions,
      logicOptions,
      symbolOptions,
      emptyChildItem,
      deleteRule: JSON.parse(JSON.stringify(this.formConf.deleteRule)),
      ruleList: JSON.parse(JSON.stringify(this.formConf.ruleList)),
      ruleMatchLogic: JSON.parse(JSON.stringify(this.formConf.ruleMatchLogic)),
      baseSymbolOptions,
      rangeSymbolOptions,
      selectSymbolOptions,
      switchSymbolOptions,
      relationFormSymbolOptions,
      useRangeSymbolList,
      useSelectSymbolList,
      useSwitchSymbolList,
      useRelationFormSymbolList
    }
  },
  computed: {
    getRuleOptions() {
      let formFieldList = this.formConf.formFieldList.filter(o => o.id.indexOf('-') < 0)
      return formFieldList.filter(o => !['relationFormAttr', 'popupAttr', 'uploadFile', 'uploadImg', 'colorPicker', 'editor'].includes(o.__config__.jnpfKey))
    }
  },
  watch: {
    getRuleOptions: {
      handler(val) {
        this.buildOptions(val)
      },
      deep: true
    }
  },
  methods: {
    getSymbolOptions(jnpfKey) {
      if (this.useSwitchSymbolList.includes(jnpfKey)) return this.switchSymbolOptions;
      if (this.useRelationFormSymbolList.includes(jnpfKey)) return this.relationFormSymbolOptions;
      if (this.useRangeSymbolList.includes(jnpfKey)) return this.rangeSymbolOptions;
      if (this.useSelectSymbolList.includes(jnpfKey)) return this.selectSymbolOptions;
      return this.baseSymbolOptions;
    },
    input() {
      this.$forceUpdate();
    },
    getRuleList() {
      this.formConf.deleteRule = this.deleteRule
      this.formConf.ruleMatchLogic = this.ruleMatchLogic
      return this.ruleList
    },
    onFormIdChange(id, item) {
      if (!id) return this.handleNull();
      this.formConf.formName = item.fullName;
      this.formConf.formId = id;
      this.getFormFieldList(id, 'deleteData');
      this.ruleList = []
      this.ruleMatchLogic = 'and'
      this.formConf.ruleList = []
      if (this.integrateType != 1) {
        this.ruleMatchLogic = 'and'
        let emptyItem = JSON.parse(JSON.stringify(this.emptyItem))
        emptyItem.groups[0].symbol = '=='
        this.ruleList.push(emptyItem);
        this.formConf.ruleList = this.ruleList
      }
    },
    handleNull() {
      this.formConf.formName = '';
      this.formConf.formId = '';
      this.formConf.formFieldList = [];
    },
    addItem(index) {
      let emptyChildItem = JSON.parse(JSON.stringify(this.emptyChildItem))
      if (this.integrateType != 1) emptyChildItem.symbol = '=='
      this.ruleList[index].groups.push(emptyChildItem);
    },
    delItem(index, childIndex) {
      if (this.integrateType != 1 && this.ruleList.length <= 1 && this.ruleList[0].groups.length <= 1) {
        this.$message.error('必须保留一个条件');
        return;
      }
      this.ruleList[index].groups.splice(childIndex, 1);
    },
    addGroup() {
      let emptyItem = JSON.parse(JSON.stringify(this.emptyItem))
      if (this.integrateType != 1) emptyItem.groups[0].symbol = '=='
      this.ruleList.push(emptyItem);
    },
    delGroup(index) {
      if (this.integrateType != 1 && this.ruleList.length <= 1) return this.$message.error('必须保留一个条件');
      this.ruleList.splice(index, 1);
    },
    onTypeChange(item) {
      if (item.fieldValueType == 1) {
        if (item.fieldValue) item.fieldValue = undefined
      } else {
        if (item.fieldValue) item.fieldValue = ["in", "notIn"].includes(item.symbol) ? [] : undefined
        if (item.jnpfKey == 'switch') item.fieldValue = false
      }
    },
    onFieldChange(item, val, index, childIndex) {
      item.cellKey = +new Date()
      item.symbol = ''
      if (!val) {
        item.jnpfKey = '';
        if (item.fieldValueType == 2) {
          item.fieldValue = undefined;
          item.fieldValueJnpfKey = '';
        }
        return;
      }
      let data = this.getRuleOptions.filter(o => o.id == val)[0];
      item.jnpfKey = data.__config__.jnpfKey;
      const newItem = JSON.parse(JSON.stringify(this.emptyChildItem));
      for (let key of Object.keys(newItem)) {
        newItem[key] = item[key];
      }
      item = { ...newItem, ...data };
      if (item.fieldValueType == 2) {
        item.fieldValue = undefined;
        item.fieldValueJnpfKey = '';
      }
      if (this.integrateType != 1) item.symbol = '=='
      this.$set(this.ruleList[index].groups, childIndex, item);
    },
    onSymbolChange(val, item, i, childIndex) {
      item.cellKey = +new Date()
      item.fieldValueType = 1;
      item.fieldValue = undefined;
      item.fieldValueJnpfKey = '';
      item.disabled = ['null', 'notNull'].includes(val);
      item.multiple = ["in", "notIn"].includes(val);
      if (['select', 'radio', 'checkbox'].includes(item.jnpfKey)) {
        if (["in", "notIn"].includes(val)) {
          item.fieldValue = []
        } else {
          item.fieldValue = ''
        }
      }
      this.$set(this.ruleList[i].groups, childIndex, item);
    },
    buildOptions(componentList) {
      componentList.forEach(cur => {
        cur.disabled = false;
        const config = cur.__config__;
        if (dyOptionsList.includes(config.jnpfKey)) {
          if (config.dataType === 'dictionary' && config.dictionaryType) {
            cur.options = []
            getDictionaryDataSelector(config.dictionaryType).then(res => {
              cur.options = res.data.list;
            });
          }
          if (config.dataType === 'dynamic' && config.propsUrl) {
            cur.options = []
            getDataInterfaceRes(config.propsUrl).then(res => {
              cur.options = Array.isArray(res.data) ? res.data : [];
            });
          }
        }
      });
      return componentList;
    },
  }
}

</script>
<style lang="scss" scoped>
.approver-pane {
  overflow: hidden;
  height: 100%;
  .config-scrollbar {
    height: 100%;
    overflow: hidden;
    .el-row {
      font-size: 14px;
      color: #606266;
      // height: 32px;
      line-height: 32px;
    }
  }
  .condition-btn {
    >>> .icon-ym {
      font-size: 12px !important;
    }
  }
  .del-icon {
    cursor: pointer;
    color: #ff3a3a;
  }
  .text-center {
    text-align: center;
  }
  .leading-32px {
    line-height: 32px;
  }
  .mt-10px {
    margin-top: 10px;
  }
  .mb-10px {
    margin-bottom: 10px;
  }
  .link-text {
    color: #1890ff;
    cursor: pointer;
    margin-top: 10px;
    display: block;
  }
  .add-text {
    font-size: 14px;
    margin-right: 4px;
  }
}
.form-item-label {
  font-weight: 600;
  font-size: 14px;
  line-height: 32px;
}
.condition-pane {
  height: 100%;
  overflow: auto;
  overflow-x: hidden;
}
</style> 

