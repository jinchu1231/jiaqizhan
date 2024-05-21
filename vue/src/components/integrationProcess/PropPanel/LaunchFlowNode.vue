<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20">
        <el-form-item>
          <div slot="label" class="form-item-label">选择流程</div>
          <FlowModal :value="flowId" :title="formConf.flowName" @change="onFlowIdChange"
            placeholder="请选择流程" :allowClear="false" />
        </el-form-item>
        <el-form-item>
          <div slot="label" class="form-item-label">自动发起审批
            <el-tooltip content="流程发起的下一节点设置选择分支或候选审批人时无法自动发起审批" placement="top">
              <a class="el-icon-question tooltip-question"></a>
            </el-tooltip>
          </div>
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
        <el-form-item class="form-item">
          <div slot="label" class="form-item-label">发起人
            <el-tooltip content="多个审批人时产生多条流程实例" placement="top">
              <a class="el-icon-question tooltip-question"></a>
            </el-tooltip>
          </div>
          <JnpfUserSelect class="ml-10" v-model="initiator" placeholder="请选择发起人" multiple />
        </el-form-item>
      </el-form>
    </el-scrollbar>
  </section>
</template>
<script>
import FlowModal from './FlowModal';
import { sourceTypeOptions } from '../FlowCard/define.js';
const emptyChildItem = { field: '', symbol: '==', fieldValueType: 1, fieldValue: undefined, jnpfKey: '', fieldValueJnpfKey: '', cellKey: +new Date() };
const emptyItem = { logic: 'and', groups: [emptyChildItem] };
export default {
  name: 'addDataNode',
  components: { FlowModal },
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
    getFlowFormFieldList: {
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
      sourceTypeOptions,
      transferList: JSON.parse(JSON.stringify(this.formConf.transferList)),
      flowId: JSON.parse(JSON.stringify(this.formConf.flowId)),
      initiator: JSON.parse(JSON.stringify(this.formConf.initiator || []))
    }
  },
  computed: {
    getRuleOptions() {
      let formFieldList = this.formConf.formFieldList.filter(o => o.id.indexOf('-') < 0)
      return formFieldList.filter(o => !['relationFormAttr', 'popupAttr', 'uploadFile', 'uploadImg', 'colorPicker', 'editor'].includes(o.__config__.jnpfKey))
    }
  },
  methods: {
    onChangeFlow() {
      if (!this.flowId) return (this.formConf.flowName = '');
      let item = this.flowOptions.filter(o => o.id === this.flowId)[0]
      this.formConf.flowName = item.fullName
    },
    onChangeSourceValue(item) {
      if (item.sourceValue) item.sourceValue = undefined
    },
    getItem() {
      let item = {
        transferList: this.transferList,
        flowId: this.flowId,
        initiator: this.initiator || []
      }
      return item
    },
    onChangeUpdate() {
      this.$forceUpdate()
    },
    onFlowIdChange(id, item) {
      this.formConf.transferList = [JSON.parse(JSON.stringify(emptyItem))];
      if (!id) return handleNull();
      this.formConf.flowName = item.fullName;
      this.getFlowFormFieldList(id, 'launchFlow');
      this.flowId = id;
      this.transferList = [];
      this.formConf.transferList = [];
      this.formConf.flowId = id
    },
    handleNull() {
      this.formConf.flowName = '';
      this.formConf.flowId = '';
      this.flowId = '';
      this.formConf.formFieldList = [];
      this.transferList = [];
    },
    addTransferItem() {
      this.transferList.push({ targetField: '', targetFieldLabel: '', sourceType: 1, sourceValue: '', required: false });
    },
    delTransferItem(index) {
      this.transferList.splice(index, 1);
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
  .ml-10 {
    margin-left: 10px;
  }
}
.condition-pane {
  height: 100%;
  overflow: auto;
  overflow-x: hidden;
}
.form-item {
  >>> .el-form-item__label {
    width: 100% !important;
    margin-right: 10px;
  }
}
</style> 
