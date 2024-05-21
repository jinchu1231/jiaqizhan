<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20">
        <el-form-item>
          <div slot="label" class="form-item-label">目标表单</div>
          <FlowFormModal v-model="formConf.formId" :title="formConf.formName"
            @change="onFormIdChange" placeholder="请选择表单" :clearable='false' />
          <el-select v-model="flowId" placeholder="请选择流程" filterable
            style="margin-top: 10px;width: 100%;" v-if="formConf.enableFlow">
            <el-option v-for="item in flowOptions" :key="item.id" :label="item.fullName"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <div slot="label" class="form-item-label">字段设置</div>
          <section class="condition-pane pd-10">
            <el-row :gutter="8">
              <el-col :span="7">选择目标表单字段</el-col>
              <el-col :span="5"></el-col>
              <el-col :span="12" style="margin-left:70px">设置触发表单字段或自定义值 </el-col>
            </el-row>
            <el-row :gutter="8" v-for="(item, index) in transferList" :key="index" class="mt-10px">
              <el-col :span="7">
                <el-select v-model="item.targetField" placeholder="请选择字段" filterable
                  style="width:100%" :disabled="item.required">
                  <el-option v-for="item in getTargetOptions(index)" :key="item.id"
                    :label="item.fullName" :value="item.id" />
                </el-select>
              </el-col>
              <el-col :span="3" class="leading-32px">的值设为</el-col>
              <el-col :span="5">
                <el-select v-model="item.sourceType" placeholder="请选择字段" filterable
                  style="width:100%" :disabled="item.required" @change="onChangeSourceValue(item)">
                  <el-option v-for="item in sourceTypeOptions" :key="item.id" :label="item.fullName"
                    :value="item.id" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-select v-model="item.sourceValue" placeholder="请选择字段" filterable
                  style="width:100%" v-if="item.sourceType === 1">
                  <el-option v-for="item in formFieldsOptions" :key="item.id" :label="item.fullName"
                    :value="item.id" />
                </el-select>
                <el-input v-model="item.sourceValue" placeholder="请输入值" allowClear
                  v-if="item.sourceType === 2" />
              </el-col>
              <el-col :span="1" class="text-center  del-icon" v-if="!item.required">
                <i class="icon-ym icon-ym-btn-clearn" @click="delTransferItem(index)" />
              </el-col>
            </el-row>
            <div class="link-text" @click="addTransferItem()"><i
                class="icon-ym icon-ym-btn-add add-text"></i>添加字段</div>
          </section>
        </el-form-item>
        <el-form-item v-if="integrateType != 1">
          <div slot="label" class="form-item-label">唯一性验证
            <el-tooltip content="判断目标表单与触发表单是否同一条数据" placement="top">
              <i class="el-icon-question tooltip-question"></i>
            </el-tooltip>
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
                <el-col :span="8">
                  <el-select v-model="child.field" placeholder="请选择目标表单字段" filterable
                    style="width:100%" @change="onFieldChange(child, $event, index, childIndex)">
                    <el-option v-for="item in getRuleOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="5">
                  <el-select v-model="child.symbol" placeholder="运算符号" filterable
                    :disabled=' child.symbol=="=="' style="width:100%">
                    <el-option v-for="item in symbolOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" />
                  </el-select>
                </el-col>
                <el-col :span="10">
                  <el-select v-model="child.fieldValue" placeholder="请选择触发表单字段" filterable
                    style="width:100%" clearable @change="onChangeUpdate">
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
        <el-form-item label="数据存在时" v-if="integrateType != 1">
          <el-radio-group v-model="addRule">
            <el-radio v-for="item in addRuleOptions" :key="item.id"
              :label="item.id">{{item.fullName}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-scrollbar>
  </section>
</template>
<script>
import FlowFormModal from './FormListModal';
import { sourceTypeOptions, logicOptions, symbolOptions } from '../FlowCard/define.js';
import MsgDialog from '@/components/Process/PropPanel/msgDialog'
const addRuleOptions = [
  { id: 0, fullName: '不新增数据' },
  { id: 1, fullName: '新增一条数据' },
];
const emptyChildItem = { field: '', symbol: '', fieldValueType: 1, fieldValue: undefined, jnpfKey: '', fieldValueJnpfKey: '', cellKey: +new Date() };
const emptyItem = { logic: 'and', groups: [emptyChildItem] };

export default {
  name: 'addDataNode',
  components: { FlowFormModal, MsgDialog },
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
    },
    flowOptions: {
      type: Array,
      default: () => []
    },
  },
  data() {
    return {
      addRuleOptions,
      emptyItem,
      sourceTypeOptions,
      logicOptions,
      symbolOptions,
      emptyChildItem,
      transferList: JSON.parse(JSON.stringify(this.formConf.transferList)),
      ruleList: JSON.parse(JSON.stringify(this.formConf.ruleList)),
      addRule: JSON.parse(JSON.stringify(this.formConf.addRule)),
      ruleMatchLogic: JSON.parse(JSON.stringify(this.formConf.ruleMatchLogic)),
      flowId: JSON.parse(JSON.stringify(this.formConf.flowId)),
    }
  },
  computed: {
    getRuleOptions() {
      let formFieldList = this.formConf.formFieldList.filter(o => o.id.indexOf('-') < 0)
      return formFieldList.filter(o => !['relationFormAttr', 'popupAttr', 'uploadFile', 'uploadImg', 'colorPicker', 'editor'].includes(o.__config__.jnpfKey))
    }
  },
  methods: {
    onChangeSourceValue(item) {
      if (item.sourceValue) item.sourceValue = undefined
    },
    getItem() {
      this.formConf.flowId = this.flowId
      let item = {
        transferList: this.transferList,
        ruleList: this.ruleList,
      }
      if (this.integrateType == 2) {
        item.addRule = this.addRule
        this.formConf.ruleMatchLogic = this.ruleMatchLogic
      }
      return item
    },
    onChangeUpdate() {
      this.$forceUpdate()
    },
    onFormIdChange(id, item) {
      this.ruleList = []
      this.formConf.ruleList = []
      if (this.integrateType != 1) {
        this.ruleMatchLogic = 'and'
        let emptyItem = JSON.parse(JSON.stringify(this.emptyItem))
        emptyItem.groups[0].symbol = '=='
        this.formConf.ruleList.push(emptyItem);
      }
      if (!id) return this.handleNull();
      this.formConf.formName = item.fullName;
      this.formConf.formId = id;
      this.formConf.enableFlow = item.enableFlow;
      this.formConf.flowId = ''
      this.transferList = [];
      this.getFormFieldList(id, 'addData');
    },
    handleNull() {
      this.formConf.formName = '';
      this.formConf.formId = '';
      this.formConf.enableFlow = 0;
      this.formConf.formFieldList = [];
      this.transferList = [];
    },
    addTransferItem() {
      this.transferList.push({ targetField: '', targetFieldLabel: '', sourceType: 1, sourceValue: '', required: false });
    },
    delTransferItem(index) {
      this.transferList.splice(index, 1);
    },
    addItem(index) {
      let emptyChildItem = JSON.parse(JSON.stringify(this.emptyChildItem))
      if (this.integrateType != 1) emptyChildItem.symbol = '=='
      this.ruleList[index].groups.push(emptyChildItem);
    },
    delItem(index, childIndex) {
      if (this.ruleList.length <= 1 && this.ruleList[0].groups.length <= 1) return this.$message.error('必须保留一个条件');
      this.ruleList[index].groups.splice(childIndex, 1);
    },
    addGroup() {
      let emptyItem = JSON.parse(JSON.stringify(this.emptyItem))
      if (this.integrateType != 1) emptyItem.groups[0].symbol = '=='
      this.ruleList.push(emptyItem);
    },
    delGroup(index) {
      if (this.ruleList.length <= 1) return this.$message.error('必须保留一个条件');
      this.ruleList.splice(index, 1);
    },
    onFieldChange(item, val, index, childIndex) {
      item.symbol = ''
      let data = this.getRuleOptions.filter(o => o.__vModel__ == val)[0];
      if (!val) {
        item.jnpfKey = '';
        if (item.fieldValueType == 2) {
          item.fieldValue = undefined;

          item.fieldValueJnpfKey = '';
        }
        return;
      }
      item.jnpfKey = data.__config__.jnpfKey;
      const newItem = JSON.parse(JSON.stringify(this.emptyChildItem));
      for (let key of Object.keys(newItem)) {
        newItem[key] = item[key];
      }
      item = { ...newItem, ...data };
      if (item.fieldValueType != 1) {
        item.fieldValue = undefined;
        item.fieldValueJnpfKey = '';
      }
      if (this.integrateType != 1) item.symbol = '=='
      this.ruleList[index].groups[childIndex] = item;
      this.onChangeUpdate()
    },
    getTargetOptions(index) {
      let ignoreOptions = [];
      for (let i = 0; i < this.transferList.length; i++) {
        const e = this.transferList[i];
        if (e.targetField && index !== i) ignoreOptions.push(e.targetField);
      }
      const list = this.formConf.formFieldList.filter(o => !ignoreOptions.includes(o.id));
      return list;
    }
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
    .add-text {
      font-size: 14px;
      margin-right: 4px;
    }
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
