<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="dataForm" class="pd-10-20">
        <el-form-item label="获取方式">
          <div slot="label" class="form-item-label">获取方式</div>
          <el-radio-group v-model="dataForm.formType" class="common-radio formType-radio"
            @change="onFormTypeChange">
            <el-radio :label="1">从普通表单中获取</el-radio>
            <el-radio :label="2">从流程表单中获取
              <el-tooltip content="只获取已完成的流程数据" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </el-radio>
            <el-radio :label="3">从数据接口中获取</el-radio>
          </el-radio-group>
          <FlowFormModal :value="dataForm.formId" :title="dataForm.formName"
            :enableFlow="dataForm.formType == 1 ? 0 : 1" @change="onFormIdChange" :clearable='false'
            placeholder="请选择表单" v-if="dataForm.formType == 1 || dataForm.formType == 2" />
          <template v-if="dataForm.formType == 3">
            <InterfaceDialog :value="dataForm.formId" :title="dataForm.formName"
              @change="onInterfaceChange" placeholder="请选择接口" />
            <div class="ant-form-item-label mt-12px"><label
                class="ant-form-item-no-colon">参数设置</label></div>
            <el-table :data="dataForm.interfaceTemplateJson">
              <el-table-column type="index" width="50" label="序号" align="center" />
              <el-table-column prop="field" label="参数名称" width="150">
                <template slot-scope="scope">
                  <span class="required-sign">{{scope.row.required?'*':''}}</span>
                  {{scope.row.fieldName?scope.row.field+'('+scope.row.fieldName+')':scope.row.field}}
                </template>
              </el-table-column>
              <el-table-column prop="field" label="参数来源" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.sourceType" placeholder="请选择参数来源"
                    @change="scope.row.relationField = ''" style="width:100%">
                    <el-option v-for="item in interfaceSourceTypeOptions" :key="item.id"
                      :label="item.fullName" :value="item.id" :disabled="item.disabled" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="value" label="参数值">
                <template slot-scope="scope">
                  <template v-if="scope.row.sourceType == 2">
                    <JnpfInputNumber v-if="['int', 'decimal'].includes(scope.row.dataType)"
                      v-model="scope.row.relationField" placeholder="请输入参数值" clearable controls
                      controlsPosition='right' class="input-number" style="width:100%" />
                    <JnpfDatePicker v-else-if="scope.row.dataType == 'datetime'" style="width:100%"
                      v-model="scope.row.relationField" placeholder="请选择参数值"
                      format="yyyy-MM-dd HH:mm:ss" clearable />
                    <el-input v-model="scope.row.relationField" placeholder="请输入参数值" clearable
                      v-else style="width:100%" />
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-form-item>
        <el-form-item v-if="dataForm.formType == 1 || dataForm.formType == 2">
          <div slot="label" class="form-item-label">获取条件
            <el-tooltip content="满足以下条件，触发执行动作。" placement="top">
              <i class="el-icon-question tooltip-question"></i>
            </el-tooltip>
          </div>
          <Condition :value="dataForm.ruleList" ref="base" :matchLogic="dataForm.ruleMatchLogic"
            :columnOptions="dataForm.formFieldList" :type='1'>
          </Condition>
        </el-form-item>
      </el-form>
    </el-scrollbar>
  </section>
</template>
<script>
import Condition from "@/components/ColumnDesign/condition";
import FlowFormModal from './FormListModal';
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'
import { getConfigData } from '@/api/onlineDev/visualDev'
const interfaceSourceTypeOptions = [
  { id: 2, fullName: '自定义' },
  { id: 3, fullName: '为空' },
];
const emptyChildItem = { field: '', symbol: '==', fieldValueType: 1, fieldValue: undefined, jnpfKey: '', fieldValueJnpfKey: '', cellKey: +new Date() };
const emptyItem = { logic: 'and', groups: [emptyChildItem] };

export default {
  name: 'addDataNode',
  components: { Condition, FlowFormModal, InterfaceDialog },
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
    usedFormItems: {
      type: Array,
      default: () => []
    },
  },
  data() {
    return {
      dataForm: JSON.parse(JSON.stringify(this.formConf)),
      interfaceSourceTypeOptions
    }
  },
  methods: {
    getItem() {
      let item = this.$refs.base && this.$refs.base.getData() || ''
      this.dataForm.ruleList = item ? item.conditionList : []
      if (!this.validData()) {
        return '';
      }
      return this.dataForm
    },
    onFormTypeChange() {
      this.handleNull();
    },
    handleNull() {
      this.dataForm.formName = '';
      this.dataForm.formId = '';
      this.dataForm.formFieldList = [];
      this.dataForm.ruleList = [];
      this.dataForm.interfaceTemplateJson = [];
    },
    validData() {
      let isOk = true;
      for (let i = 0; i < this.dataForm.ruleList.length; i++) {
        const e = this.dataForm.ruleList[i];
        for (let j = 0; j < e.groups.length; j++) {
          const child = e.groups[j];
          if (!child.field) {
            this.$message.warning('条件字段不能为空');
            isOk = false;
            return;
          }
          if (!child.symbol) {
            this.$message.warning('条件符号不能为空');
            isOk = false;
            return;
          }
          if (
            !['null', 'notNull'].includes(child.symbol) &&
            (!child.fieldValue || this.jnpf.isEmpty(child.fieldValue)) &&
            (!['inputNumber', 'calculate'].includes(child.jnpfKey) || (['inputNumber', 'calculate'].includes(child.jnpfKey) && child.fieldValue !== 0))
          ) {
            this.$message.warning('数据值不能为空');
            isOk = false;
            return;
          }
        }
      }
      return isOk;
    },
    onInterfaceChange(id, item) {
      if (!id) return this.handleNull();
      if (this.dataForm.formId === id) return;
      this.dataForm.formName = item.fullName;
      this.dataForm.formId = id;
      this.dataForm.ruleList = [];
      const formFieldList = item.fieldJson ? JSON.parse(item.fieldJson) : [];
      this.dataForm.formFieldList = formFieldList.map(o => ({
        ...o,
        id: o.defaultValue,
        fullName: o.field,
        label: o.field ? o.defaultValue + '(' + o.field + ')' : o.defaultValue,
      }));
      this.dataForm.interfaceTemplateJson = (item.templateJson || []).map(o => ({ ...o, sourceType: 2, relationField: '' }));
    },
    onFormIdChange(id, item) {
      if (!id) return this.handleNull();
      this.dataForm.formName = item.fullName;
      this.dataForm.formId = id;
      this.dataForm.ruleList = [];
      this.getFormFieldList(id);
    },
    getFormFieldList(id) {
      getConfigData(id).then(res => {
        const { formData, enableFlow, flowId } = res.data;
        let formJson = {},
          fieldList = [];
        if (formData) formJson = JSON.parse(formData);
        fieldList = formJson.fields || [];
        let list = this.transformFieldList(fieldList);
        this.dataForm.formFieldList = list.map(o => ({ ...o, label: o.fullName ? o.id + '(' + o.fullName + ')' : o.id }));
      });
    },
    transformFormJson(list) {
      const fieldList = list.map(o => ({
        __config__: {
          label: o.filedName,
          jnpfKey: o.jnpfKey || '',
          required: o.required || false,
        },
        __vModel__: o.filedId,
        multiple: o.multiple || false,
      }));
      return fieldList;
    },
    transformFieldList(formFieldList) {
      let list = [];
      const loop = (data, parent) => {
        if (!data) return;
        if (data.__vModel__ && data.__config__.jnpfKey !== 'table') {
          const isTableChild = parent && parent.__config__ && parent.__config__.jnpfKey === 'table';
          list.push({
            id: isTableChild ? parent.__vModel__ + '-' + data.__vModel__ : data.__vModel__,
            fullName: isTableChild ? parent.__config__.label + '-' + data.__config__.label : data.__config__.label,
            ...data,
          });
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent));
        if (data.__config__ && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data);
        }
      };
      loop(formFieldList);
      return list;
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
      height: 32px;
      line-height: 32px;
    }
  }
  .condition-btn {
    >>> .icon-ym {
      font-size: 12px !important;
    }
  }

  .formType-radio {
    margin-bottom: 10px;
    .el-radio {
      width: 100%;
      line-height: 32px;
      margin-right: 0;
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
>>> .clearn-color {
  margin: 0 -14px !important;
}
</style> 

