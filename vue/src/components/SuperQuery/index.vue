<template>
  <el-dialog title="高级查询" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center superQuery-dialog" lock-scroll width="800px">
    <div class="superQuery-main" v-loading="loading">
      <template v-if="conditionList.length">
        <Condition :value="conditionList" ref="base" :columnOptions="columnOptions"
          :matchLogic="matchLogic" :key="key2">
        </Condition>
      </template>
      <div class="query-noData" v-else>
        <img src="@/assets/images/query-noData.png" alt="" class="noData-img">
        <div class="noData-txt">
          <span>没有任何查询条件</span>
          <el-divider direction="vertical"></el-divider>
          <el-link type="primary" :underline="false" @click="addCondition">点击新增</el-link>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <div class="footer-options">
        <el-button @click="addPlan" class="add-btn">保存方案</el-button>
        <el-popover width="240" trigger="click" popper-class="plan-popper" ref="planPopper">
          <el-button slot="reference">方案选择<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <div class="plan-list" v-if="planList.length">
            <div class="plan-list-item" v-for="(item,i) in planList" :key="i"
              @click="selectPlan(item)">
              <el-link type="primary" :underline="false" class="plan-list-name">{{item.fullName}}
              </el-link>
              <i class="el-icon-close" @click.stop="delPlan(item.id,i)"></i>
            </div>
          </div>
          <div class="noData-txt" v-else>暂无数据</div>
        </el-popover>
      </div>
      <el-button @click="close">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="query()">查 询</el-button>
    </span>
    <el-dialog title="保存方案" :visible.sync="addPlanVisible" width="600px" append-to-body lock-scroll
      class="JNPF-dialog JNPF-dialog_center">
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="80px">
        <el-form-item label="方案名称" prop="fullName">
          <el-input v-model="dataForm.fullName" placeholder="请输入保存的方案名称" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addPlanVisible = false">{{$t('common.cancelButton')}}</el-button>
        <el-button type="primary" :loading="saveBtnLoading" @click="savePlan()">
          {{$t('common.confirmButton')}}</el-button>
      </span>
    </el-dialog>
  </el-dialog>
</template>
<script>

import { getAdvancedQueryList, Delete, Create, Update } from "@/api/system/advancedQuery";
import Condition from "@/components/ColumnDesign/condition";
import { deepClone } from '@/utils';
const condition = [{
  logic: "and",
  groups: [{
    fieldValue: '',
    field: '',
    symbol: '',
    jnpfKey: '',
    cellKey: +new Date()
  }]
}]
export default {
  components: {
    Condition
  },
  props: {
    columnOptions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      visible: false,
      addPlanVisible: false,
      loading: false,
      btnLoading: false,
      saveBtnLoading: false,
      conditionList: deepClone(condition),
      staging: {},
      key2: +new Date(),
      planList: [],
      fieldOptions: [],
      dataForm: {
        fullName: ''
      },
      dataRule: {
        fullName: [
          { required: true, message: '请输入保存的方案名称', trigger: 'blur' }
        ]
      },
      matchLogic: 'and'
    }
  },
  computed: {
    currMenuId() {
      return this.$route.meta.modelId || ''
    }
  },
  methods: {
    init() {
      this.visible = true
      this.loading = true
      let componentList = JSON.parse(JSON.stringify(this.columnOptions))
      this.fieldOptions = componentList
      this.getAdvancedQueryList()
      this.$nextTick(() => {
        this.loading = false
      })
    },
    close() {
      this.conditionList = this.staging && this.staging.conditionList || deepClone(condition)
      this.matchLogic = this.staging && this.staging.matchLogic || 'and'
      this.visible = false
    },
    getAdvancedQueryList() {
      if (!this.currMenuId) return
      getAdvancedQueryList(this.currMenuId).then(res => {
        this.planList = res.data.list
      })
    },
    addCondition() {
      let item = {
        logic: "and",
        groups: [{
          field: '', symbol: '', jnpfKey: '', fieldValueType: 2, fieldValue: undefined, fieldValueJnpfKey: '', cellKey: +new Date()
        }]
      }

      this.conditionList.push(item)
    },
    delPlan(id, i) {
      Delete(id).then(res => {
        this.$message({
          type: 'success',
          message: res.msg,
          duration: 1500,
          onClose: () => {
            this.planList.splice(i, 1)
          }
        })
      })
    },
    selectPlan(item) {
      this.conditionList = item.conditionJson ? JSON.parse(item.conditionJson) : []
      this.matchLogic = item.matchLogic
      this.key2 = +new Date()
      this.$refs.planPopper.doClose()
    },
    addPlan() {
      if (!this.exist()) return
      this.addPlanVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
    },
    savePlan() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          let boo = this.planList.some(o => o.fullName === this.dataForm.fullName)
          if (!boo) return this.submit()
          let list = this.planList.filter(o => o.fullName === this.dataForm.fullName)
          this.$confirm(`${list[0].fullName}已存在, 是否覆盖方案?`, '', {
            type: 'warning'
          }).then(() => {
            this.submit(list[0].id)
          }).catch(() => { });
        }
      })
    },
    submit(id) {
      this.saveBtnLoading = true
      let query = {
        id: id || '',
        ...this.dataForm,
        matchLogic: this.matchLogic,
        moduleId: this.currMenuId,
        conditionJson: JSON.stringify(this.conditionList)
      }
      const formMethod = query.id ? Update : Create
      formMethod(query).then(res => {
        this.getAdvancedQueryList()
        this.addPlanVisible = false
        this.saveBtnLoading = false
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500
        })
      }).catch(() => {
        this.saveBtnLoading = false
      })
    },
    getDataType(jnpfKey) {
      if (!jnpfKey) return ''
      if (['inputNumber', 'datePicker', 'rate', 'slider'].includes(jnpfKey)) {
        return 'number'
      } else if (['uploadFile', 'uploadImg', 'cascader', 'organizeSelect', 'areaSelect'].includes(jnpfKey)) {
        return 'array'
      }
      return ''
    },
    exist(type) {
      const mes = type == 'query' ? '查询' : '保存'
      let item = this.$refs.base && this.$refs.base.getData() || ''
      if (type == 'query' && !item) return true
      if (!item) return this.$message.warning(`请添加${mes}条件`)
      this.conditionList = item.conditionList;
      this.matchLogic = item.matchLogic
      let isOk = true
      for (let i = 0; i < this.conditionList.length; i++) {
        const e = this.conditionList[i];
        for (let j = 0; j < e.groups.length; j++) {
          const child = e.groups[j];
          if (!child.field) {
            this.$message.warning(`${mes}字段不能为空`);
            isOk = false;
            return;
          }
          if (!child.symbol) {
            this.$message.warning(`${mes}符号不能为空`);
            isOk = false;
            return;
          }
          if (!['null', 'notNull'].includes(child.symbol) && !['switch'].includes(child.jnpfKey) && ((!child.fieldValue && child.fieldValue !== 0) || ((typeof child.fieldValue === "object" && child.fieldValue.length == 0)) || this.jnpf.isEmpty(child.fieldValue))) {
            this.$message.warning(`数据值不能为空`);
            isOk = false;
            return;
          }
        }
      }
      return isOk
    },
    query() {
      if (!this.exist('query')) return
      let query = {
        matchLogic: this.matchLogic,
        conditionList: this.conditionList
      }
      this.staging = JSON.parse(JSON.stringify(query))
      query = JSON.stringify(query)
      if (!this.conditionList.length) query = ""
      this.$emit('superQuery', query)
      this.visible = false
    }
  }
}
</script>
<style lang="scss" scoped>
.superQuery-dialog {
  >>> .el-dialog {
    .el-dialog__body {
      padding: 20px 20px 10px 24px;
    }
    .footer-options {
      float: left;
      .add-btn {
        margin-right: 10px;
      }
    }
  }
  .superQuery-main {
    .query-noData {
      text-align: center;
      padding: 20px 0;
      .noData-img {
        width: 160px;
        margin-bottom: 10px;
      }
      .noData-txt {
        color: #909399;
      }
    }
    .matchLogic {
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      line-height: 32px;
      .el-select {
        width: 220px;
      }
    }
    .condition-list {
      margin-bottom: 10px;
    }
  }
}
</style>
<style lang="scss">
.plan-popper {
  padding: 0 !important;
  .plan-list {
    padding: 6px 0;
    max-height: 182px;
    overflow: auto;
    &-item {
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: #606266;
      font-size: 14px;
      cursor: pointer;
      padding: 0 20px;
      &:hover {
        background-color: #f5f7fa;
      }
    }
    .plan-list-name {
      .el-link--inner {
        width: 160px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .el-icon-close:hover {
      color: #f56c6c;
    }
  }
  .noData-txt {
    height: 34px;
    color: #999;
    font-size: 14px;
    line-height: 34px;
    text-align: center;
  }
}
</style>