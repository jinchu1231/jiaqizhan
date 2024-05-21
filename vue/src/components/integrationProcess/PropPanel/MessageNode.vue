<template>
  <section class="approver-pane">
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20">
        <el-form-item label="接收人">
          <div slot="label" class="form-item-label">接收人</div>
          <JnpfUsersSelect v-model="dataForm.msgUserIds" multiple placeholder="请选择接收人" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-form-item label="发送配置">
            <msg-dialog v-model="dataForm.msgId" :title="dataForm.msgName"
              @change="onMsgChange($event,arguments)" :messageSource='3' />
          </el-form-item>
          <div class="form-sub-title">参数设置</div>
          <el-table :data="dataForm.templateJson">
            <el-table-column type="index" width="50" label="序号" align="center" />
            <el-table-column prop="field" label="模板名称" width="150">
              <template slot-scope="scope">
                <el-link @click='goDetail(scope.row.templateId)'>{{scope.row.msgTemplateName}}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="field" label="参数名称" width="150">
              <template slot-scope="scope">
                <div class="parameter-box" v-for="(item,index) in scope.row.paramJson" :key="index">
                  {{item.field}}({{item.fieldName}})
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="value" label="参数值">
              <template slot-scope="scope">
                <el-select v-model="it.relationField" placeholder="请配置字段" clearable filterable
                  @change="onRelationFieldChange($event,it)"
                  v-for="(it,index) in scope.row.paramJson" :key="index"
                  :class="{ 'mt-8px': index > 0 }">
                  <el-option v-for="item in formFieldsOptions" :key="item.id"
                    :label="item.fullName?item.id+'('+item.fullName+')':item.id" :value="item.id" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
    </el-scrollbar>
    <Detail v-if="viewVisible" ref="View" @close="viewVisible = false" />
  </section>
</template>
<script>
import MsgDialog from '@/components/Process/PropPanel/msgDialog'
import Detail from '@/components/Process/PropPanel/TemplateDetail'
export default {
  name: 'DeleteDataNode',
  components: { MsgDialog, Detail },
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
      dataForm: JSON.parse(JSON.stringify(this.formConf)),
      viewVisible: false
    }
  },
  methods: {
    getDataForm() {
      return this.dataForm
    },
    onMsgChange(val, params) {
      const [id, item] = params
      if (!id) {
        this.dataForm.msgId = ''
        this.dataForm.msgName = ''
        this.dataForm.templateJson = []
        return
      }
      //if (this.dataForm.msgId === val) return
      this.dataForm.msgId = id
      this.dataForm.msgName = item.fullName
      this.dataForm.templateJson = item.templateJson
    },
    onRelationFieldChange(val, row) {
      if (!val) return;
      let list = this.formFieldsOptions.filter(o => o.id === val);
      if (!list.length) return;
      let item = list[0];
      row.isSubTable = item.__config__ && item.__config__.isSubTable ? item.__config__.isSubTable : false;
    },
    goDetail(id) {
      this.viewVisible = true
      this.$nextTick(() => {
        this.$refs.View.init(id)
      })
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
  .mt-8px {
    margin-top: 10px;
  }
  .mt-10px {
    margin-top: 10px;
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
