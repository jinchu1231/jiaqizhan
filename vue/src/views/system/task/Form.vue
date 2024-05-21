<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main flow-form-main">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack" :content="!dataForm.id ? '新建' : '编辑'" />
        <div class="options">
          <el-button type="primary" @click="dataFormSubmit()" :loading="btnLoading">
            {{$t('common.confirmButton')}}</el-button>
          <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <el-row class="main">
        <el-col :span="14" :offset="5">
          <el-form :model="dataForm" :rules="dataRule" ref="dataForm" class="mt-20"
            label-width="130px" @submit.native.prevent>
            <jnpf-form-tip-item label="任务名称" prop="fullName">
              <el-input v-model="dataForm.fullName" placeholder="任务名称" maxlength="100">
              </el-input>
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="任务编码" prop="enCode">
              <el-input v-model="dataForm.enCode" placeholder="任务编码"></el-input>
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="Cron表达式" prop="executeContent.cron">
              <el-input v-model="dataForm.executeContent.cron" placeholder="Cron表达式" readonly>
                <el-button slot="append" icon="el-icon-edit-outline" @click="showDialog">
                </el-button>
              </el-input>
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="任务开始时间" prop="executeContent.startTime">
              <el-date-picker v-model="dataForm.executeContent.startTime" type="datetime"
                format="yyyy-MM-dd HH:mm:ss" value-format="timestamp" placeholder="选择任务开始时间">
              </el-date-picker>
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="任务结束时间" prop="executeContent.endTime"
              tip-label="未选结束时间即为永久期限">
              <el-date-picker v-model="dataForm.executeContent.endTime" type="datetime"
                format="yyyy-MM-dd HH:mm:ss" value-format="timestamp" placeholder="选择任务结束时间">
              </el-date-picker>
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="任务类型" prop="executeType">
              <el-radio-group v-model="dataForm.executeType" @change="onExecuteTypeChange">
                <el-radio label="1">数据接口</el-radio>
                <el-radio label="3">本地任务
                  <el-tooltip content="获取在程序中事先写好的本地方法" placement="top">
                    <i class="el-icon-question tooltip-question"></i>
                  </el-tooltip>
                </el-radio>
              </el-radio-group>
            </jnpf-form-tip-item>
            <template v-if="dataForm.executeType=='1'">
              <jnpf-form-tip-item label="接口选择" prop="executeContent.interfaceId">
                <interface-dialog :value="dataForm.executeContent.interfaceId"
                  :title="dataForm.executeContent.interfaceName" @change="onFuncChange" />
              </jnpf-form-tip-item>
              <jnpf-form-tip-item label="请求参数" prop="executeContent.parameter"
                v-if="dataForm.executeContent.parameter.length">
                <el-row v-for="(item,i) in dataForm.executeContent.parameter" :key="i"
                  class="mb-10">
                  <el-col :span="11">
                    <span
                      class="required-sign">{{item.required?'*':''}}</span>{{item.fieldName?item.field+'('+item.fieldName+')':item.field}}
                  </el-col>
                  <el-col :span="12" :offset="1">
                    <el-input v-model="item.value" placeholder="请求参数值" clearable />
                  </el-col>
                </el-row>
              </jnpf-form-tip-item>
            </template>
            <template v-if="dataForm.executeType=='3'">
              <jnpf-form-tip-item label="方法选择" prop="executeContent.localHostTaskId">
                <el-select v-model="dataForm.executeContent.localHostTaskId" placeholder="请选择"
                  filterable @change="onLocalHostTaskIdChange">
                  <el-option v-for="item in taskOptions" :key="item.id" :label="item.fullName"
                    :value="item.id" />
                </el-select>
              </jnpf-form-tip-item>
            </template>
            <jnpf-form-tip-item label="任务状态" prop="enabledMark">
              <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
            </jnpf-form-tip-item>
            <jnpf-form-tip-item label="任务说明" prop="description">
              <el-input v-model="dataForm.description" placeholder="任务说明" type="textarea"
                :rows="3" />
            </jnpf-form-tip-item>
          </el-form>
        </el-col>
      </el-row>
      <el-dialog title="Cron表达式" :visible.sync="showCron" :close-on-click-modal="false"
        class="JNPF-dialog JNPF-dialog_center" lock-scroll append-to-body width="700px"
        @closed="showCrontab = false">
        <vcrontab ref="vcrontab" @hide="showCron=false" @fill="crontabFill"
          :expression="dataForm.executeContent.cron" v-if="showCrontab" />
        <span slot="footer" class="dialog-footer">
          <el-button @click="showCron=false">{{$t('common.cancelButton')}}
          </el-button>
          <el-button type="warning" @click="resetCrontab">重 置</el-button>
          <el-button type="primary" @click="getCrontabValue">{{$t('common.confirmButton')}}
          </el-button>
        </span>
      </el-dialog>
    </div>
  </transition>
</template>

<script>
import { TimeTaskInfo, TimeTaskUpdate, TimeTaskCreate, getTaskMethods } from '@/api/system/timeTask'
import vcrontab from "vcrontab"
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'

export default {
  components: { vcrontab, InterfaceDialog },
  data() {
    const checkStartTime = (rule, value, callback) => {
      if (!this.dataForm.executeContent.endTime) {
        callback()
      } else {
        if (this.dataForm.executeContent.endTime <= value) {
          callback(new Error('任务结束时间不能早于任务开始时间'));
        } else {
          this.$refs.dataForm.validateField('executeContent.endTime');
          callback()
        }
      }
    }
    const checkEndTime = (rule, value, callback) => {
      if (!this.dataForm.executeContent.startTime || !value) {
        callback()
      } else {
        if (this.dataForm.executeContent.startTime >= value) {
          callback(new Error('任务结束时间不能早于任务开始时间'));
        } else {
          callback()
        }
      }
    }
    return {
      showCron: false,
      showCrontab: true,
      dataForm: {
        id: '',
        fullName: '',
        enCode: '',
        executeType: '1',
        enabledMark: 1,
        description: '',
        executeContent: {
          cron: '',
          interfaceId: '',
          interfaceName: '',
          parameter: [],
          localHostTaskId: '',
          startTime: null,
          endTime: null
        }
      },
      dataRule: {
        fullName: [
          { required: true, message: '任务名称不能为空', trigger: 'blur' },
        ],
        enCode: [
          { required: true, message: '任务编码不能为空', trigger: 'blur' },
          { validator: this.formValidate('enCode', '任务编码只能输入英文、数字和小数点且小数点不能放在首尾'), trigger: 'blur' },
        ],
        'executeContent.cron': [
          { required: true, message: 'Cron表达式不能为空', trigger: 'click' }
        ],
        'executeContent.interfaceId': [
          { required: true, message: '接口选择不能为空', trigger: 'click' }
        ],
        'executeContent.localHostTaskId': [
          { required: true, message: '方法选择不能为空', trigger: 'change' }
        ],
        'executeContent.startTime': [
          { required: true, message: '任务开始时间不能为空', trigger: 'change' },
          { validator: checkStartTime, trigger: 'change' }
        ],
        'executeContent.endTime': [
          { validator: checkEndTime, trigger: 'change' }
        ]
      },
      formVisible: false,
      btnLoading: false,
      taskOptions: []
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    init(id) {
      this.dataForm = {
        id: '',
        fullName: '',
        enCode: '',
        executeType: '1',
        enabledMark: 1,
        description: '',
        executeContent: {
          cron: '',
          interfaceId: '',
          interfaceName: '',
          parameter: [],
          localHostTaskId: '',
          startTime: null,
          endTime: null
        }
      }
      this.dataForm.id = id || ''
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        getTaskMethods().then(res => {
          this.taskOptions = res.data || []
          if (this.dataForm.id) {
            TimeTaskInfo(this.dataForm.id).then(res => {
              this.dataForm = res.data
              this.dataForm.executeContent = JSON.parse(res.data.executeContent)
            })
          }
        })
      })
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          if (this.dataForm.executeType === '1' || this.dataForm.executeType === '2') {
            let boo = false
            for (let i = 0; i < this.dataForm.executeContent.parameter.length; i++) {
              const item = this.dataForm.executeContent.parameter[i]
              if (item.required && !item.value) {
                this.$message.error(`${item.field}的参数值为必填`)
                boo = true
                break
              }
            }
            if (boo) return
          }
          this.btnLoading = true
          let executeContent = JSON.stringify(this.dataForm.executeContent)
          let query = { ...this.dataForm, executeContent }
          const formMethod = this.dataForm.id ? TimeTaskUpdate : TimeTaskCreate
          formMethod(query).then((res) => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('close', true)
              }
            })
          }).catch(() => { this.btnLoading = false })
        }
      })
    },
    onFuncChange(id, row) {
      if (!id) {
        this.dataForm.executeContent.interfaceId = ''
        this.dataForm.executeContent.interfaceName = ''
        this.dataForm.executeContent.parameter = []
        return
      }
      if (this.dataForm.executeContent.interfaceId === id) return
      this.dataForm.executeContent.interfaceId = id
      this.dataForm.executeContent.interfaceName = row.fullName
      this.dataForm.executeContent.parameter = row.templateJson.map(o => ({
        ...o,
        value: ''
      }))
    },
    onExecuteTypeChange() {
      this.dataForm.executeContent.interfaceId = ''
      this.dataForm.executeContent.interfaceName = ''
      this.dataForm.executeContent.parameter = []
      this.dataForm.executeContent.localHostTaskId = ''
    },
    onLocalHostTaskIdChange(val) {
      if (!val) return
      let list = this.taskOptions.filter(o => o.id === val) || []
      if (!list.length) return
      let item = list[0]
      if (!item.cron) return
      this.dataForm.executeContent.cron = item.cron
    },
    getCrontabValue() {
      this.$refs.vcrontab && this.$refs.vcrontab.submitFill()
    },
    resetCrontab() {
      this.$refs.vcrontab && this.$refs.vcrontab.clearCron()
    },
    crontabFill(value) {
      this.dataForm.executeContent.cron = value
    },
    showDialog() {
      this.showCron = true
      this.showCrontab = true
    }
  }
}
</script>
<style lang="scss" scoped>
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
</style>