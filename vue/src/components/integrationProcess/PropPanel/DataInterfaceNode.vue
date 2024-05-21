<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20">
        <el-form-item>
          <div slot="label" class="form-item-label">执行数据接口</div>
          <InterfaceDialog :value="formConf.formId" :title="formConf.formName"
            @change="onFormIdChange" placeholder="请选择接口" />
          <div class="ant-form-item-label mt-12px"><label
              class="ant-form-item-no-colon">参数设置</label></div>
          <el-table :data="formConf.templateJson">
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
                  @change="scope.row.relationField = ''">
                  <template v-if="scope.row.required">
                    <el-option v-for="item in noNullOptions" :key="item.id" :label="item.fullName"
                      :value="item.id" :disabled="item.disabled">
                    </el-option>
                  </template>
                  <template v-else>
                    <el-option v-for="item in interfaceSourceTypeOptions" :key="item.id"
                      :label="item.fullName" :value="item.id" :disabled="item.disabled" />
                  </template>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="value" label="参数值">
              <template slot-scope="scope">
                <el-select v-model="scope.row.relationField" placeholder="请选择参数值" clearable
                  filterable @change="onRelationFieldChange($event,scope.row)"
                  v-if="scope.row.sourceType == 1" style="width:100%">
                  <el-option v-for="item in formFieldsOptions" :key="item.id"
                    :label="item.fullName?item.id+'('+item.fullName+')':item.id" :value="item.id" />
                </el-select>
                <template v-else-if="scope.row.sourceType == 2">
                  <JnpfInputNumber v-if="['int', 'decimal'].includes(scope.row.dataType)"
                    v-model="scope.row.relationField" placeholder="请输入参数值" clearable controls
                    controlsPosition='right' class="input-number" />
                  <JnpfDatePicker v-else-if="scope.row.dataType == 'datetime'" style="width:100%"
                    v-model="scope.row.relationField" placeholder="请选择参数值"
                    format="yyyy-MM-dd HH:mm:ss" clearable />
                  <el-input v-model="scope.row.relationField" placeholder="请输入参数值" clearable
                    style="width:100%" v-else />
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
    </el-scrollbar>>
  </section>
</template>
<script>
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'
import { interfaceSourceTypeOptions } from '../FlowCard/define.js';
const noNullOptions = interfaceSourceTypeOptions.filter(o => o.id != 3);
export default {
  name: 'DeleteDataNode',
  components: { InterfaceDialog },
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
  },
  data() {
    return {
      interfaceSourceTypeOptions,
      noNullOptions
    }
  },
  computed: {
    getRuleOptions() {
      return this.formConf.formFieldList.filter(o => o.id.indexOf('-') < 0)
    }
  },
  methods: {
    getRuleList() {
      return this.formConf
    },
    onFormIdChange(id, item) {
      if (!id) return this.handleNull();
      this.formConf.formName = item.fullName;
      this.formConf.formId = id;
      this.formConf.templateJson = (item.templateJson || []).map(o => ({ ...o, sourceType: 1, relationField: '' }));
      this.$forceUpdate()
    },
    handleNull() {
      this.formConf.formName = '';
      this.formConf.formId = '';
      this.formConf.templateJson = [];
    },
    onRelationFieldChange(val, row) {
      if (!val) return;
      let list = this.formFieldsOptions.filter(o => o.id === val);
      if (!list.length) return;
      let item = list[0];
      row.isSubTable = item.__config__ && item.__config__.isSubTable ? item.__config__.isSubTable : false;
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
      height: 32px;
      line-height: 32px;
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
