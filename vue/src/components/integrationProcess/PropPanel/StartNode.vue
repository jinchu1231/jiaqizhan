<template>
  <section class="approver-pane">
    <el-tabs v-model="activeKey" class="pane-tabs">
      <el-tab-pane name="1"
        :label="integrateType == 1 ? '表单配置' : integrateType == 2 ? '触发配置' : 'webhook配置'"></el-tab-pane>
      <el-tab-pane name="2" label="通知配置"></el-tab-pane>
    </el-tabs>
    <el-scrollbar class="config-scrollbar">
      <el-form label-position="top" :model="formConf" class="pd-10-20" v-show="activeKey === '1'">
        <template v-if="integrateType == 1">
          <el-form-item>
            <div slot="label" class="form-item-label">触发表单</div>
            <FlowFormModal v-model="formId" :title="formConf.formName" :enableFlow="0"
              :clearable='false' @change="onFormIdChange" placeholder="请选择表单" />
          </el-form-item>
          <el-form-item>
            <div slot="label" class="form-item-label">触发事件</div>
            <el-radio-group v-model="formConf.triggerEvent">
              <el-radio v-for="item in triggerEventOptions" :key="item.id"
                :label="item.id">{{item.fullName}}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <div slot="label" class="form-item-label">触发条件
              <el-tooltip content="满足以下条件，触发执行动作。" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </div>
            <Condition :value="formConf.ruleList" ref="base" :matchLogic="formConf.ruleMatchLogic"
              :columnOptions="formFieldList" :type='1'>
            </Condition>
          </el-form-item>
        </template>
        <template v-if="integrateType == 2">
          <el-form-item>
            <div slot="label" class="form-item-label">触发开始时间</div>
            <el-date-picker v-model="formConf.startTime" type="datetime" style="width:100%"
              value-format="timestamp" format="yyyy-MM-dd HH:mm:ss" placeholder="请选择触发开始时间" />
          </el-form-item>
          <el-form-item label="触发规则">
            <el-form-item>
              <div class="ant-form-item-label"><label class="ant-form-item-no-colon">Cron表达式</label>
              </div>
              <el-input v-model="formConf.cron" placeholder="Cron表达式" readonly>
                <el-button slot="append" icon="el-icon-edit-outline" @click="showDialog">
                </el-button>
              </el-input>
            </el-form-item>
          </el-form-item>
          <el-form-item label="触发结束时间">
            <div slot="label" class="form-item-label">触发结束时间</div>
            <el-select v-model="formConf.endTimeType" style="width:100%">
              <el-option v-for="item in endTimeTypeOptions" :key="item.id" :label="item.fullName"
                :value="item.id">
              </el-option>
            </el-select>
            <el-input-number v-model="formConf.endLimit" placeholder="次数" :min="1" :precision="0"
              addonAfter="次后结束" class="mt-10px" @change="oneEndLimitChange"
              v-if="formConf.endTimeType === 1" />
            <el-date-picker v-model="formConf.endTime" type="datetime" style="width:100%"
              placeholder="请触发结束时间" class="mt-10px" format="yyyy-MM-dd HH:mm:ss"
              value-format='timestamp' v-else-if="formConf.endTimeType === 2" />
          </el-form-item>
        </template>
        <template v-if="integrateType == 3">
          <el-form-item label="webhook URL">
            <p slot="label" class="form-item-label color">webhook URL</p>
            <p class="form-item-color mb-10px">系统生成的URL，用来接收请求字段！</p>
            <el-input v-model="getWebhookUrl" readonly>
              <template slot="append">
                <span class="cursor-pointer" @click="handleCopy($event)">复制链接</span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="添加接口字段">
            <div class="mb-10px">
              <span class="link-text mr-20px inline-block" @click="addItem"><i
                  class="icon-ym icon-ym-btn-add text-14px mr-4px"></i>自定义添加</span>
              <span class="link-text mr-20px inline-block" @click="openBatch">
                <i class="icon-ym icon-ym-btn-add text-14px mr-4px"></i>从JSON格式添加
              </span>
              <span class="link-text mr-20px inline-block" @click="openWebhookRequest">
                <i class="icon-ym icon-ym-btn-add text-14px mr-4px"></i>从请求接口添加
              </span>
            </div>
            <div class="mb-10px">
              <el-row :gutter="8" v-for="(item, index) in formConf.formFieldList" :key="index"
                class="mt-10px">
                <el-col :span="10">
                  <el-input v-model="item.id" placeholder="字段" @change="onItemChange(item)" />
                </el-col>
                <el-col :span="13">
                  <el-input v-model="item.fullName" placeholder="字段说明"
                    @change="onItemChange(item)" />
                </el-col>
                <el-col :span="1" class="text-center">
                  <i class="icon-ym icon-ym-btn-clearn clearn" @click="delItem(index)" />
                </el-col>
              </el-row>
            </div>
          </el-form-item>
        </template>
      </el-form>
      <div v-show="activeKey === '2'">
        <el-form label-position="top" :model="formConf" class="pd-10-20">
          <el-form-item>
            <div slot="label" class="form-item-label">通知人</div>
            <JnpfSelect v-model="formConf.msgUserType" :options="msgUserOptions" :props="props"
              placeholder="请选择" multiple style="width:100%" />
          </el-form-item>
          <el-form-item v-if="formConf.msgUserType.includes(3)">
            <div class="ant-form-item-label">
              <label class="ant-form-item-no-colon">选择用户</label>
            </div>
            <JnpfUsersSelect v-model="formConf.msgUserIds" placeholder="请选择通知人" multiple />
          </el-form-item>
          <el-form-item label="执行失败">
            <div slot="label" class="form-item-label">执行失败
              <el-tooltip content="执行失败时发送通知！" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </div>
            <el-select v-model="formConf.failMsgConfig.on" placeholder="请选择" filterable
              style="width:100%">
              <el-option v-for="item in noticeOptions" :key="item.id" :label="item.fullName"
                :value="item.id" />
            </el-select>
          </el-form-item>
          <div v-if="formConf.failMsgConfig.on===1">
            <el-form-item label="发送配置">
              <msg-dialog :value="formConf.failMsgConfig.msgId"
                :title="formConf.failMsgConfig.msgName" :messageSource='3'
                @change="onMsgChange('formConf','failMsgConfig',arguments)" />
            </el-form-item>
            <template v-if="integrateType==1">
              <div class="form-sub-title">参数设置</div>
              <el-table :data="formConf.failMsgConfig.templateJson">
                <el-table-column type="index" width="50" label="序号" align="center" />
                <el-table-column prop="field" label="模板名称" width="150">
                  <template slot-scope="scope">
                    <el-link @click='goDetail(scope.row.templateId)'>{{scope.row.msgTemplateName}}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="field" label="参数名称" width="150">
                  <template slot-scope="scope">
                    <div class="parameter-box" v-for="(item,index) in scope.row.paramJson"
                      :key="index">
                      {{item.field}}({{item.fieldName}})
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="value" label="表单字段">
                  <template slot-scope="scope">
                    <el-select class="variable-box" v-model="it.relationField" placeholder="请选择表单字段"
                      clearable filterable @change="onRelationFieldChange($event,it)"
                      v-for="(it,index) in scope.row.paramJson" :key="index">
                      <el-option-group>
                        <el-option v-for="item in formFieldsOptions" :key="item.__vModel__"
                          :label="item.__config__.label?item.__vModel__+'('+item.__config__.label+')':item.__vModel__"
                          :value="item.__vModel__" />
                      </el-option-group>
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
          <el-form-item label="开始执行">
            <div slot="label" class="form-item-label">开始执行
              <el-tooltip content="开始执行时发送通知！" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </div>
            <el-select v-model="formConf.startMsgConfig.on" placeholder="请选择" filterable
              style="width:100%">
              <el-option v-for="item in noticeOptions" :key="item.id" :label="item.fullName"
                :value="item.id" />
            </el-select>
          </el-form-item>
          <div v-if="formConf.startMsgConfig.on===1">
            <el-form-item label="发送配置">
              <msg-dialog :value="formConf.startMsgConfig.msgId"
                :title="formConf.startMsgConfig.msgName" :messageSource='3'
                @change="onMsgChange('formConf','startMsgConfig',arguments)" />
            </el-form-item>
            <template v-if="integrateType==1">
              <div class="form-sub-title">参数设置</div>
              <el-table :data="formConf.startMsgConfig.templateJson">
                <el-table-column type="index" width="50" label="序号" align="center" />
                <el-table-column prop="field" label="模板名称" width="150">
                  <template slot-scope="scope">
                    <el-link @click='goDetail(scope.row.templateId)'>{{scope.row.msgTemplateName}}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="field" label="参数名称" width="150">
                  <template slot-scope="scope">
                    <div class="parameter-box" v-for="(item,index) in scope.row.paramJson"
                      :key="index">
                      {{item.field}}({{item.fieldName}})
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="value" label="数据值">
                  <template slot-scope="scope">
                    <el-select class="variable-box" v-model="it.relationField" placeholder="请配置字段"
                      clearable filterable @change="onRelationFieldChange($event,it)"
                      v-for="(it,index) in scope.row.paramJson" :key="index">
                      <el-option-group>
                        <el-option v-for="item in formFieldsOptions" :key="item.__vModel__"
                          :label="item.__config__.label?item.__vModel__+'('+item.__config__.label+')':item.__vModel__"
                          :value="item.__vModel__" />
                      </el-option-group>
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
        </el-form>
      </div>
    </el-scrollbar>
    <Detail v-if="viewVisible" ref="View" @close="viewVisible = false" />
    <BatchDialog v-if="jsonVisible" ref="jsonBatch" @confirm="addItemForOther" />
    <WebhookRequestDialog v-if="webhookRequestVisible" ref="webhookRequest"
      @confirm="addItemForOther" />
    <el-dialog title="Cron表达式" :visible.sync="showCron" :close-on-click-modal="false"
      class="JNPF-dialog JNPF-dialog_center" lock-scroll append-to-body width="700px"
      @closed="showCrontab = false">
      <vcrontab ref="vcrontab" @hide="showCron=false" @fill="crontabFill"
        :expression="formConf.cron" v-if="showCrontab" />
      <span slot="footer" class="dialog-footer">
        <el-button @click="showCron=false">{{$t('common.cancelButton')}}
        </el-button>
        <el-button type="warning" @click="resetCrontab">重 置</el-button>
        <el-button type="primary" @click="getCrontabValue">{{$t('common.confirmButton')}}
        </el-button>
      </span>
    </el-dialog>
  </section>
</template>
<script>
import FlowFormModal from './FormListModal';
import Condition from "@/components/ColumnDesign/condition";
import { noticeOptions, msgUserOptions } from '../FlowCard/define.js';
import MsgDialog from '@/components/Process/PropPanel/msgDialog'
import Detail from '@/components/Process/PropPanel/TemplateDetail'
import vcrontab from "vcrontab"
import clipboard from '@/utils/clipboard'
import BatchDialog from './model/BatchDialog'
import WebhookRequestDialog from './model/WebhookRequestDialog'
import store from '@/store'
const triggerEventOptions = [
  { id: 1, fullName: '新增数据' },
  { id: 2, fullName: '修改数据' },
  { id: 3, fullName: '删除数据' },
];
const endTimeTypeOptions = [
  { id: 1, fullName: '触发次数' },
  { id: 2, fullName: '指定时间' },
  { id: 3, fullName: '不结束' },
];
export default {
  name: 'StartNode',
  components: { FlowFormModal, Condition, MsgDialog, Detail, vcrontab, BatchDialog, WebhookRequestDialog },
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
    formInfo: {
      type: Object,
      default: () => { }
    }
  },
  data() {
    return {
      activeKey: '1',
      triggerEventOptions,
      columnOptions: [],
      msgUserOptions,
      props: {
        label: "fullName",
        value: "id"
      },
      noticeOptions,
      viewVisible: false,
      showCron: false,
      showCrontab: true,
      endTimeTypeOptions,
      jsonVisible: false,
      webhookRequestVisible: false,
      formId: JSON.parse(JSON.stringify(this.formConf.formId))
    }
  },
  created() {
    if (this.integrateType == 3) this.formConf.formFieldList = this.formConf.formFieldList.filter(o => o.id !== '@formId')
  },
  computed: {
    formFieldList() {
      return this.formConf.formFieldList.filter(o => o.id.indexOf('-') < 0)
    },
    getWebhookUrl() {
      const sysConfig = store.getters && store.getters.sysConfig
      return sysConfig.jnpfDomain + this.formConf.webhookUrl
    }
  },
  methods: {
    getFormConf() {
      let isOk = false
      if (this.integrateType == 1) {
        if (!this.formConf.formId) {
          this.$message.error('请选择触发表单');
          return isOk
        }
        let item = this.$refs.base && this.$refs.base.getData() || ''
        if (!item) return isOk;
        this.formConf.ruleMatchLogic = item.matchLogic
        this.formConf.ruleList = item.conditionList
        if (!this.formConf.ruleList || !this.validData()) {
          return isOk;
        }
      } else if (this.integrateType == 2) {
        if (!this.formConf.startTime) {
          this.$message.error('请选择触发开始时间');
          return isOk
        }
        if (!this.formConf.cron) {
          this.$message.error('请设置Cron表达式');
          return isOk
        }
        if (this.formConf.endTimeType == 2 && !this.formConf.endTime) {
          this.$message.error('请选择触发结束时间');
          return isOk
        }
        if (this.formConf.startMsgConfig.on == 1 && !this.formConf.startMsgConfig.msgId) {
          this.$message.error('请选择开始执行发送配置');
          return isOk
        }
        if (this.formConf.failMsgConfig.on == 1 && !this.formConf.failMsgConfig.msgId) {
          this.$message.error('请选择执行失败发送配置');
          return isOk
        }
      } else {
        if (!this.formConf.formFieldList.length) {
          this.$message.error('请设置接口字段');
          return isOk
        }
        if (!this.formFieldListExist()) return;
        if (this.formConf.failMsgConfig.on === 1 && !this.formConf.failMsgConfig.msgId) {
          this.$message.error('请选择执行失败发送配置');

        }
        if (this.formConf.startMsgConfig.on === 1 && !this.formConf.startMsgConfig.msgId) {
          this.$message.error('请选择开始执行发送配置');
          return isOk
        }
      }
      return this.formConf;
    },
    getCrontabValue() {
      this.$refs.vcrontab && this.$refs.vcrontab.submitFill()
    },
    resetCrontab() {
      this.$refs.vcrontab && this.$refs.vcrontab.clearCron()
    },
    crontabFill(value) {
      this.formConf.cron = value
    },
    showDialog() {
      this.showCron = true
      this.showCrontab = true
    },
    validData() {
      let isOk = true;
      for (let i = 0; i < this.formConf.ruleList.length; i++) {
        const e = this.formConf.ruleList[i];
        for (let j = 0; j < e.groups.length; j++) {
          const child = e.groups[j];
          if (!child.field) {
            this.$message.warning('条件字段不能为空');
            isOk = false;
            break;
          }
          if (!child.symbol) {
            this.$message.warning('条件符号不能为空');
            isOk = false;
            break;
          }
          if (
            !['null', 'notNull'].includes(child.symbol) && !['switch'].includes(child.jnpfKey) &&
            (!child.fieldValue || this.jnpf.isEmpty(child.fieldValue)) &&
            (!['inputNumber', 'calculate', 'rate'].includes(child.jnpfKey) || (['inputNumber', 'calculate', 'rate'].includes(child.jnpfKey) && child.fieldValue !== 0))
          ) {
            this.$message.warning('数据值不能为空');
            isOk = false;
            break;
          }
        }
      }
      return isOk;
    },
    onFormIdChange(id, item) {
      if (!id) return this.handleNull();
      this.formConf.ruleList = []
      this.getFormFieldList(id, 'start');
      if (this.formConf.formId == id) return
      this.formConf.formName = item.fullName;
      this.formConf.formId = id;
    },
    handleNull() {
      this.formConf.formName = '';
      this.formConf.formId = '';
      this.formConf.formFieldList = [];
      this.formConf.ruleList = []
    },
    onRelationFieldChange(val, item) {
      if (!val) return
      let list = this.formFieldsOptions.filter(o => val === o.__vModel__)
      if (!list.length) return
      let obj = list[0]
      this.$set(item, 'isSubTable', obj.__config__ && obj.__config__.isSubTable ? obj.__config__.isSubTable : false)
    },
    onMsgChange(obj, key, params) {
      const [id, item] = params
      if (!id) {
        this[obj][key].msgId = ''
        this[obj][key].msgName = ''
        this[obj][key].templateJson = []
        return
      }
      if (this[obj][key].msgId === id) return
      this[obj][key].msgId = id
      this[obj][key].msgName = item.fullName
      this[obj][key].templateJson = item.templateJson
    },
    goDetail(id) {
      this.viewVisible = true
      this.$nextTick(() => {
        this.$refs.View.init(id)
      })
    },
    oneEndLimitChange(val) {
      this.$nextTick(() => {
        if (!val) this.formConf.endLimit = 1;
      })
    },
    addItem() {
      this.formConf.formFieldList.push({ id: '', fullName: '', label: '' });
    },
    delItem(index) {
      this.formConf.formFieldList.splice(index, 1);
    },
    onItemChange(item) {
      item.label = item.fullName ? item.id + '(' + item.fullName + ')' : item.id;
    },
    handleCopy(event) {
      clipboard(this.getWebhookUrl, event)
    },
    openBatch() {
      this.jsonVisible = true
      this.$nextTick(() => {
        this.$refs.jsonBatch.init()
      })
    },
    openWebhookRequest() {
      this.webhookRequestVisible = true
      this.$nextTick(() => {
        this.$refs.webhookRequest.init({ url: this.formConf.webhookGetFieldsUrl, randomStr: this.formConf.webhookRandomStr, id: this.formInfo.id })
      })
    },
    addItemForOther(data) {
      this.webhookRequestVisible = false
      this.jsonVisible = false
      const list = data.map(o => ({ ...o, label: o.fullName ? o.id + '(' + o.fullName + ')' : o.id }));
      if (!this.formConf.formFieldList.length) return (this.formConf.formFieldList = list);
      for (let i = 0; i < list.length; i++) {
        const e = list[i];
        if (!e.id) {
          this.formConf.formFieldList.push(e);
        } else {
          const findIndex = this.formConf.formFieldList.findIndex(o => o.id === e.id);
          findIndex < 0 ? this.formConf.formFieldList.push(e) : (this.formConf.formFieldList[findIndex] = e);
        }
      }
    },
    // webhook接口字段验证
    formFieldListExist() {
      const list = this.formConf.formFieldList;
      let isOk = true;
      for (let i = 0; i < list.length; i++) {
        const e = list[i];
        if (!e.id) {
          this.$message.warning(`第${i + 1}行字段不能为空`);
          isOk = false;
          break;
        }
        if (!e.fullName) {
          this.$message.warning(`第${i + 1}行字段说明不能为空`);
          isOk = false;
          break;
        }
      }
      return isOk;
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

  .per-cell {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    .el-checkbox {
      margin-right: 40px;
    }
    &:last-child {
      margin-bottom: unset;
    }
  }
}
.form-item-label {
  font-weight: 600;
  font-size: 14px;
  line-height: 32px;
  &.color {
    color: rgba(0, 0, 0, 0.85);
  }
}
.clearn {
  cursor: pointer;
  font-size: 18px;
  line-height: 32px;
  color: #ed6f6f;
}
>>> .clearn-color {
  margin: 0 -18px !important;
}
>>> .el-dialog__body {
  padding: 10px 10px 0 !important;
}
>>> .popup-main {
  .pop_btn {
    display: none !important;
  }
  .popup-result:nth-child(2) {
    margin-bottom: 2px;
  }
}
.delBtn {
  text-align: right;
}
.mt-10px {
  margin-top: 10px;
}
.mb-10px {
  margin-bottom: 10px;
}
.form-item-color {
  color: #aaaaaa;
}
.condition-pane {
  height: 100%;
  overflow: auto;
  overflow-x: hidden;
}
.link-text {
  color: #1890ff;
  cursor: pointer;
  user-select: none;
}
.mr-20px {
  margin-right: 20px;
}
.inline-block {
  display: inline-block;
}
.mr-4px {
  margin-right: 4px;
}
.cursor-pointer {
  cursor: pointer;
}
</style>
