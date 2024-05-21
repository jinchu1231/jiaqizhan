<template>
  <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll
    class="JNPF-dialog JNPF-dialog_center" width="630px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading">
      <el-form-item prop="enCode">
        <el-input v-model="dataForm.enCode" placeholder="请输入编码名称" />
      </el-form-item>
      <el-form-item prop="fullName">
        <el-input v-model="dataForm.fullName" placeholder="请输入方案名称" />
      </el-form-item>
      <div class="mb-10px" v-if="condition.length">
        <span style="font-size: 14px;">分组匹配逻辑</span>
        <el-select v-model="dataForm.matchLogic" placeholder="请选择"
          style="width:150px;margin-left: 10px;">
          <el-option v-for="item in logicOptions" :key="item.value" :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>
      <el-form-item v-for="(item, index) in condition" :key="index" style="margin-top: 10px;">
        <el-row :gutter="5">
          <el-col :span="7" class="mb-10">
            <el-select v-model="item.logic" placeholder="请选择">
              <el-option v-for="item in logicOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-col>
          <el-col :span="5" class="mb-10">
            <el-button icon="el-icon-plus" @click="addItem(index)">添加条件</el-button>
          </el-col>
          <el-col :span="12" class="mb-10" style="text-align:right">
            <el-button type="danger" icon="el-icon-close" @click="delGroup(index)">删除分组</el-button>
          </el-col>
          <el-col :span="24" v-for="(subItem, i) in item.groups" :key="index + i" class="mb-10">
            <el-row :gutter="5">
              <el-col :span="7">
                <el-select v-model="subItem.id" placeholder="选择字段" clearable
                  @change="changeField($event,subItem)">
                  <el-option v-for="item in fieldOptions" :key="item.id" :label="item.fullName"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-col>
              <el-col :span="7">
                <el-select v-model="subItem.op" placeholder="选择符号" clearable
                  @change="symbolChange($event, subItem)">
                  <el-option v-for="item in subItem.opOptions" :key="item.value" :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </el-col>
              <el-col :span="8">
                <template v-if="subItem.conditionText === 'inputNumber'">
                  <NumRange v-model="subItem.value" :disabled="subItem.disabled"
                    v-if="subItem.op == 'between'"></NumRange>
                  <el-input-number v-else v-model="subItem.value" placeholder="请输入"
                    :disabled="subItem.disabled" controls-position="right" />
                </template>
                <template v-else-if="['datePicker'].includes(subItem.conditionText)">
                  <template v-if="subItem.op == 'between'">
                    <JnpfDateRangePicker v-model="subItem.value" clearable
                      :disabled="subItem.disabled" key="year1" placeholder="请选择" separator="至"
                      :type="subItem.dateType" startPlaceholder="开始日期" endPlaceholder="结束日期"
                      @change="onDateChange($event, subItem)" style="width: 100%;"
                      :format="subItem.format" value-format='timestamp' />
                  </template>
                  <JnpfDatePicker v-else v-model="subItem.value" clearable :type="subItem.dateType"
                    :disabled="subItem.disabled" key="year2" placeholder="请选择"
                    @change="onDateChange($event, subItem)" valueFormat='timestamp'
                    :format="subItem.format" style="width: 100%;">
                  </JnpfDatePicker>
                </template>
                <template v-else-if="['organizeSelect'].includes(subItem.conditionText)">
                  <JnpfOrganizeSelect v-model="subItem.value" :disabled="subItem.disabled"
                    @change="(val, data) => onOrganizeChange(subItem, val, data)" placeholder="请选择"
                    clearable :multiple="subItem.multiple" />
                </template>
                <template v-else-if="['depSelect'].includes(subItem.conditionText)">
                  <JnpfDepSelect v-model="subItem.value" placeholder="请选择"
                    @change="(val, data) => onConditionObjChange(subItem, val, data)"
                    :multiple="subItem.multiple" clearable :disabled="subItem.disabled" />
                </template>
                <template v-else-if="['posSelect'].includes(subItem.conditionText)">
                  <JnpfPosSelect v-model="subItem.value" placeholder="请选择" clearable
                    @change="(val, data) => onConditionObjChange(subItem, val, data)"
                    :multiple="subItem.multiple" :disabled="subItem.disabled" />
                </template>
                <template v-else-if="subItem.conditionText === 'roleSelect'">
                  <JnpfRoleSelect v-model="subItem.value" :multiple="subItem.multiple"
                    @change="(val, data) => onConditionObjChange(subItem, val, data)"
                    placeholder="请选择" clearable :disabled="subItem.disabled" />
                </template>
                <template v-else-if="subItem.conditionText === 'groupSelect'">
                  <JnpfGroupSelect v-model="subItem.value" :multiple="subItem.multiple"
                    @change="(val, data) => onConditionObjChange(subItem, val, data)"
                    placeholder="请选择" clearable :disabled="subItem.disabled" />
                </template>
                <template v-else-if="['userSelect'].includes(subItem.conditionText)">
                  <JnpfUserSelect v-model="subItem.value" placeholder="请选择" clearable
                    @change="(val, data) => onConditionObjChange(subItem, val, data)"
                    :multiple="subItem.multiple" :disabled="subItem.disabled" />
                </template>
                <template v-else>
                  <el-input v-model="subItem.value" placeholder="输入字段值" :readonly="subItem.readonly"
                    :disabled="subItem.disabled" @input='forceUpdate' />
                </template>
              </el-col>
              <el-col :span="2" style="text-align:right">
                <el-button type="danger" icon="el-icon-close" @click="delItem(index,i)"
                  style="width:100%" />
              </el-col>
            </el-row>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item>
        <el-button @click="addGroup">添加分组</el-button>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t("common.cancelButton")}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{ $t("common.confirmButton")}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  createDataScheme,
  updateDataScheme,
  getDataSchemeInfo,
  getDataAuthorizeList,
} from "@/api/system/dataAuthorize"
import { deepClone } from '@/utils'

export default {
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      logicOptions: [
        {
          value: "and",
          label: "and",
        },
        {
          value: "or",
          label: "or",
        },
      ],
      fieldOptions: [],
      opOptions: [{
        label: "大于等于",
        value: ">="
      },
      {
        label: "大于",
        value: ">"
      },
      {
        label: "等于",
        value: "=="
      },
      {
        label: "小于等于",
        value: "<="
      },
      {
        label: "小于",
        value: "<"
      },
      {
        label: "不等于",
        value: "<>"
      },
      {
        label: "介于",
        value: "between"
      },
      {
        label: "包含任意一个",
        value: "in"
      },
      {
        label: "不包含任意一个",
        value: "notIn"
      },
      {
        label: "包含",
        value: "like"
      },
      {
        label: "不包含",
        value: "notLike"
      },
      {
        label: "为空",
        value: "null"
      },
      {
        label: "不为空",
        value: "notNull"
      },],
      dataForm: {
        id: "",
        moduleId: "",
        enCode: "",
        fullName: "",
        conditionJson: "",
        conditionText: "",
        childTableKey: '',
        matchLogic: 'and'
      },
      condition: [],
      dataRule: {
        fullName: [
          { required: true, message: "方案名称不能为空", trigger: "blur" }
        ],
        enCode: [
          { required: true, message: '请输入编码', trigger: 'blur' },
          { max: 50, message: '字典编码最多为50个字符！', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    init(moduleId, id) {
      this.dataForm.id = id || ""
      this.dataForm.moduleId = moduleId
      this.dataForm.fullName = ""
      this.dataForm.enCode = ""
      this.dataForm.conditionJson = ""
      this.dataForm.conditionText = ""
      this.condition = [{
        logic: "and",
        groups: [{
          id: "",
          field: "",
          type: "",
          op: "",
          value: "",
          opOptions: []
        }]
      }]
      this.visible = true
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        this.formLoading = true
        getDataAuthorizeList(this.dataForm.moduleId).then(res => {
          this.fieldOptions = res.data.list
          this.fieldOptions.map(o => {
            o.enCode = o.enCode.replace('_jnpf_', '.')
            o.enCode = o.enCode.replace('jnpf_', '')
          });
          if (this.dataForm.id) {
            getDataSchemeInfo(this.dataForm.id).then(res => {
              this.dataForm = res.data
              if (res.data.conditionJson) this.condition = JSON.parse(res.data.conditionJson)
              for (let i = 0; i < this.condition.length; i++) {
                let groups = this.condition[i].groups
                for (let j = 0; j < groups.length; j++) {
                  let e = groups[j]
                  let list = this.fieldOptions.filter(o => o.id === groups[j].id)
                  list.length ? e.opOptions = this.getOptions(list[0]) : e.opOptions = []
                  if (['@userId', '@userAraSubordinates', '@organizeId', '@organizationAndSuborganization', '@branchManageOrganize', '@branchManageOrganizeAndSub'].includes(e.conditionText)) {
                    e.readonly = true
                  } else {
                    e.readonly = false
                  }
                }
              }
              this.formLoading = false
            })
          } else {
            this.formLoading = false
          }
        })
      })
    },
    changeField(val, item) {
      if (!val) {
        item.id = ''
        item.field = ''
        item.type = ''
        item.op = ''
        item.value = ''
        item.valueLabel = '';
        item.conditionText = ''
        item.childTableKey = ''
        item.opOptions = []
        item.readonly = false
        item.disabled = false
        item.multiple = false
        item.format = ''
      } else {
        item.op = ''
        item.value = undefined
        item.valueLabel = '';
        item.readonly = false
        item.disabled = false;
        item.multiple = false;
        let fieldItem = this.fieldOptions.filter(o => o.id === val)[0]
        item.type = fieldItem.type
        item.field = fieldItem.enCode
        item.childTableKey = fieldItem.childTableKey
        item.conditionText = fieldItem.conditionText
        item.fieldRule = fieldItem.fieldRule || 0
        if (item.conditionText === 'datePicker') {
          item.format = fieldItem.format
          item.dateType = fieldItem.format === 'yyyy' ? 'year' : fieldItem.format === 'yyyy-MM' ? 'month' : fieldItem.format === 'yyyy-MM-dd' ? 'date' : 'datetime'
        }
        item.opOptions = this.getOptions(fieldItem)
        if (['@userId', '@userAraSubordinates', '@organizeId', '@organizationAndSuborganization', '@branchManageOrganize', '@branchManageOrganizeAndSub'].includes(item.conditionText)) {
          item.readonly = true
          item.value = fieldItem.conditionText
          item.valueLabel = fieldItem.conditionText;
        }
      }
    },
    symbolChange(val, item) {
      const systemFieldOptions = ['@userId', '@userAraSubordinates', '@organizeId', '@organizationAndSuborganization', '@branchManageOrganize'];
      item.value = systemFieldOptions.includes(item.conditionText) && !['null', 'notNull'].includes(val) ? item.conditionText : undefined;
      item.valueLabel = '';
      item.disabled = ['null', 'notNull'].includes(val);
      item.multiple = ['in', 'notIn'].includes(val);
    },
    getOptions(fieldItem) {
      let opOptions = []
      let options = fieldItem.conditionSymbol ? fieldItem.conditionSymbol.split(',') : []
      outer: for (let i = 0; i < options.length; i++) {
        inner: for (let j = 0; j < this.opOptions.length; j++) {
          if (options[i] === this.opOptions[j].value) {
            opOptions.push(this.opOptions[j])
            break inner
          }
        }
      }
      return opOptions
    },
    addItem(index) {
      this.condition[index].groups.push({
        id: "",
        field: "",
        type: "",
        op: "",
        value: "",
        opOptions: []
      })
    },
    delItem(index, childIndex) {
      this.condition[index].groups.splice(childIndex, 1)
    },
    delGroup(index) {
      this.condition.splice(index, 1)
    },
    addGroup() {
      this.condition.push({
        logic: "and",
        groups: [{
          id: "",
          field: "",
          type: "",
          op: "",
          value: "",
          opOptions: []
        }]
      })
    },
    getOpText(val) {
      if (!val) return val
      let list = this.opOptions.filter(o => o.value == val)
      if (!list.length) return val
      return list[0].label || val
    },
    getFieldText(val) {
      if (!val) return val
      let list = this.fieldOptions.filter(o => o.id == val)
      if (!list.length) return val
      return list[0].fullName || val
    },
    onDateChange(val, item) {
      if (!val || (!val.length && item.op == 'between')) return (item.valueLabel = '');
      const format = item.format || 'yyyy-MM-dd HH:mm:ss';
      if (item.op == 'between') {
        const labelList = val.map(o => this.jnpf.toDate(o, format));
        item.valueLabel = labelList.join();
      } else {
        item.valueLabel = this.jnpf.toDate(val, format);
      }
    },
    onOrganizeChange(item, val, data) {
      if (!val || !val.length) return (item.valueLabel = '');
      if (item.multiple) {
        let valueLabelList = [];
        for (let i = 0; i < data.length; i++) {
          const labelList = data[i].map(o => o.fullName);
          valueLabelList.push(labelList.join('/'));
        }
        item.valueLabel = valueLabelList.join();
      } else {
        const labelList = data.map(o => o.fullName);
        item.valueLabel = labelList.join('/');
      }
    },
    onConditionObjChange(item, val, data) {
      if (!val || !val.length) return (item.valueLabel = '');
      if (item.multiple) {
        const labelList = data.map(o => o.fullName);
        item.valueLabel = labelList.join();
      } else {
        item.valueLabel = data.fullName || '';
      }
    },
    forceUpdate() {
      this.$forceUpdate()
    },
    dataFormSubmit() {
      this.$refs["dataForm"].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          let conditionText = "",
            conditionValid = true,
            condition = deepClone(this.condition)
          outer: for (let i = 0; i < condition.length; i++) {
            let e = condition[i]
            let text = ''
            if (i > 0) text += this.dataForm.matchLogic || 'and'
            text += '【'
            for (let j = 0; j < e.groups.length; j++) {
              let ee = e.groups[j]
              let item = this.fieldOptions.filter(o => o.id === ee.id)[0]
              if (!item) {
                this.$message.warning("方案内条件配置不完整，请检查条件内容")
                conditionValid = false
                break outer
              }
              ee.bindTable = item.bindTable
              if (!ee.field || !ee.id || !ee.op || (!['null', 'notNull'].includes(ee.op) && ((!ee.value && ee.value !== 0) || this.jnpf.isEmpty(ee.value)))) {
                this.$message.warning("方案内条件配置不完整，请检查条件内容")
                conditionValid = false
                break outer
              }
              delete ee.readonly
              delete ee.opOptions
              const valueLabel = ['null', 'notNull'].includes(ee.op) ? '' : ` {${ee.valueLabel || ee.value}}`;
              text += `${j == 0 ? '' : ' and '}{${this.getFieldText(ee.id)}} {${this.getOpText(ee.op)}}${valueLabel}`
            }
            text += '】'
            conditionText += text
          }
          if (!conditionValid) return this.btnLoading = false
          this.dataForm.conditionText = conditionText
          this.dataForm.conditionJson = JSON.stringify(condition)
          const formMethod = this.dataForm.id ? updateDataScheme : createDataScheme
          formMethod(this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: "success",
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit("refreshDataList")
              },
            });
          }).catch(() => {
            this.btnLoading = false
          })
        } else {
          this.btnLoading = false
        }
      })
    }
  }
}
</script>