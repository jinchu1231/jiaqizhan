<template>
  <div>
    <el-dialog title="填充规则配置" class="JNPF-dialog JNPF-dialog_center rule-dialog"
      :closeOnClickModal='false' :visible.sync="visible" append-to-body width="600px">
      <section class="condition-pane pd-10">
        <el-row :gutter="8">
          <el-col :span="5">{{type==1?'关联表单字段':'远端数据字段'}}</el-col>
          <el-col :span="2"></el-col>
          <el-col :span="5" class="ml-180px">当前表单字段</el-col>
        </el-row>
        <el-row :gutter="8" v-for="(item, index) in transferList" :key="index"
          class="mt-10px mb-10px">
          <el-col :span="9">
            <el-autocomplete class="inline-input" v-model="item.targetField"
              :fetch-suggestions="querySearch" placeholder="请输入显示字段" style="width:100%"
              @select="handleSelect($event,index)" v-if="type==2">
              <template slot-scope="{ item }">
                <div class="sale-order-popper-item">
                  <span>{{ item.defaultValue}}</span>
                </div>
              </template>
            </el-autocomplete>
            <el-select v-model="item.targetField" placeholder="请选择" filterable style="width:100%"
              v-else>
              <el-option v-for="item in fieldList" :key="item.vmodel" :label="item.label"
                :value="item.vmodel" />
            </el-select>
          </el-col>
          <el-col :span="4" class="leading-32px">的值填充到</el-col>
          <el-col :span="9">
            <el-select v-model="item.sourceValue " placeholder="请选择" filterable style="width:100%">
              <el-option v-for="item in getTargetOptions(index)" :key="item.id"
                :label="item.fullName" :value="item.id" />
            </el-select>
          </el-col>
          <el-col :span="2" class="text-center  del-icon">
            <i class="icon-ym icon-ym-btn-clearn" @click="delTransferItem(index)" />
          </el-col>
        </el-row>
        <span class="link-text" @click="addTransferItem()"><i
            class="icon-ym icon-ym-btn-add add-text"></i>添加字段</span>
      </section>
      <span slot="footer" class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handleGenerate()">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { noAllowRelationList } from '@/components/Generator/generator/comConfig'
export default {
  name: 'ruleDialog',
  props: {
    type: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      visible: false,
      transferList: [],
      fieldList: [],
      formFieldsList: [],
    }
  },
  methods: {
    init(data) {
      this.visible = true
      this.transferList = JSON.parse(JSON.stringify(data.transferList)) || []
      if (!this.transferList.length) this.transferList.push({ targetField: '', sourceValue: '' })
      this.fieldList = data.fieldList || []
      this.formFieldsList = data.formFieldsList.filter(o => ['input', 'textarea'].includes(o.__config__.jnpfKey)) || []
    },
    getTargetOptions(index) {
      let ignoreOptions = [];
      for (let i = 0; i < this.transferList.length; i++) {
        const e = this.transferList[i];
        if (e.sourceValue && index !== i) ignoreOptions.push(e.sourceValue);
      }
      const list = this.formFieldsList.filter(o => !ignoreOptions.includes(o.id));
      return list;
    },
    addTransferItem() {
      this.transferList.push({ targetField: '', sourceValue: '' });
    },
    delTransferItem(index) {
      this.transferList.splice(index, 1);
    },
    handleGenerate() {
      if (!this.formFieldListExist()) return;
      this.visible = false
      this.$emit('confirm', this.transferList)
    },
    close() {
      this.visible = false
    },
    formFieldListExist() {
      const list = this.transferList;
      let isOk = true;
      for (let i = 0; i < list.length; i++) {
        const e = list[i];
        if (!e.targetField) {
          this.$message.warning(`第${i + 1}行${this.type == 1 ? '关联表单字段' : '远端数据字段'}不能为空`);
          isOk = false;
          break;
        }
        if (!e.sourceValue) {
          this.$message.warning(`第${i + 1} 行当前表单字段不能为空`);
          isOk = false;
          break;
        }
      }
      return isOk;
    },
    querySearch(queryString, cb) {
      var restaurants = this.fieldList || [];
      var results = queryString ? restaurants.filter(this.createStateFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createStateFilter(queryString) {
      return (state) => {
        return (state.field.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
      };
    },
    handleSelect(item, index) {
      this.transferList[index].targetField = item.defaultValue
    }
  }
}
</script>
<style lang="scss" scoped>
.rule-dialog {
  >>> .el-dialog__body {
    overflow: hidden;
    height: 320px;
    overflow: auto;
    overflow-x: hidden;
    padding: 23px 14px 2px !important;
  }
  .del-icon {
    line-height: 32px;

    cursor: pointer;
    color: #ff3a3a;
    >>> .icon-ym {
      font-size: 24px !important;
    }
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
    // margin-top: 10px;
    //  display: flex;
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
    font-size: 14px;
  }
  .ml-180px {
    margin-left: 180px;
  }
  .font-size {
    font-size: 14px;
  }
}
</style>