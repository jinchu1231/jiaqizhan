<template>
  <el-dialog :title="title" :close-on-click-modal="false" :close-on-press-escape="false"
    :visible.sync="visible" lock-scroll width="600px" append-to-body
    class="JNPF-dialog JNPF-dialog_center">
    <el-form ref="fieldForm" :model="dataForm" :rules="dataRule" label-width="80px">
      <div v-if="tableType == 1">
        <el-form-item label="参数名称" prop="parameter">
          <el-input v-model="dataForm.parameter" placeholder="参数名称" />
        </el-form-item>
        <el-form-item label="参数类型" prop="dataType">
          <el-select v-model="dataForm.dataType" placeholder="请选择参数类型" filterable
            @change="dataForm.defaultValue = ''">
            <el-option v-for="item in options" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认值">
          <el-input-number v-if="['int', 'decimal'].includes(dataForm.dataType)"
            v-model="dataForm.defaultValue" placeholder="请输入默认值" clearable controlsPosition='right'
            style="width:100%" />
          <JnpfDatePicker v-else-if="dataForm.dataType == 'datetime'" style="width:100%"
            v-model="dataForm.defaultValue" placeholder="请选择默认值" format="yyyy-MM-dd HH:mm:ss"
            clearable />
          <el-input v-model="dataForm.defaultValue" placeholder="默认值" clearable v-else />
        </el-form-item>
        <el-form-item label="必填" prop="required">
          <el-switch v-model="dataForm.required" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="参数说明" prop="fieldName">
          <el-input v-model="dataForm.fieldName" placeholder="参数说明" />
        </el-form-item>
      </div>
      <div v-if="tableType !=1">
        <el-form-item label="字段名称" prop="field">
          <el-input v-model="dataForm.field" placeholder="请输入字段名称" />
        </el-form-item>
        <el-form-item label="映射字段" prop="defaultValue">
          <el-input v-model="dataForm.defaultValue" placeholder="请输入映射字段" />
        </el-form-item>
      </div>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
export default {
  data() {
    var checkName = (rule, value, callback) => {
      let checkName = rule.field === 'parameter' ? '参数名称' : rule.field === 'field' ? '字段名称' : '映射字段'
      let boo = true
      let name = this.tableType == 1 ? 'parameter' : 'field'
      for (let i = 0; i < this.list.length; i++) {
        if (this.dataForm[name] === this.list[i].field && this.dataForm.id !== this.list[i].id) {
          boo = false
          break
        }
      }
      if (boo) {
        callback()
      } else {
        callback(new Error(`${checkName}重复`));
      }
    }
    return {
      visible: false,
      type: 'add',
      tableType: 1,
      dataForm: {
        id: '',
        defaultValue: '',
        field: '',
        dataType: 'varchar',
        required: 0,
        fieldName: '',
        parameter: ''
      },
      list: [],
      options: [
        { label: '字符串', value: 'varchar' },
        { label: '整型', value: 'int' },
        { label: '日期时间', value: 'datetime' },
        { label: '浮点', value: 'decimal' },
      ],
      dataRule: {
        field: [
          { required: true, message: '请输入字段名称', trigger: 'blur' },
          { validator: checkName, trigger: 'blur' }
        ],
        parameter: [
          { required: true, message: '请输入参数名称', trigger: 'blur' },
          { validator: checkName, trigger: 'blur' }
        ],
        defaultValue: [
          { required: true, message: '请输入映射字段', trigger: 'blur' },
          { validator: checkName, trigger: 'blur' }
        ],
        dataType: [
          { required: true, message: '请选择参数类型', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    title() {
      if (this.type === 'add') {
        return '新建'
      } else {
        return '编辑'
      }
    }
  },
  methods: {
    init(dataForm, list, type) {
      this.visible = true
      this.tableType = type
      this.list = list || []
      if (this.tableType == 1) this.dataForm.parameter = this.dataForm.field
      this.$nextTick(() => {
        this.$refs['fieldForm'].clearValidate()
        if (dataForm) {
          this.type = 'edit'
          this.dataForm = {
            ...this.dataForm,
            ...JSON.parse(JSON.stringify(dataForm))
          }
          if (type == 1) this.dataForm.parameter = this.dataForm.field
        } else {
          this.$refs['fieldForm'].resetFields()
          this.dataForm.id = this.jnpf.idGenerator()
          this.dataForm.defaultValue = ''
          this.dataForm.fieldName = ''
          this.type = 'add'
        }
      })
    },
    dataFormSubmit() {
      this.$refs['fieldForm'].validate((valid) => {
        if (valid) {
          if (this.tableType == 1) this.dataForm.field = this.dataForm.parameter
          this.$emit('addParameter', this.type, this.dataForm, this.tableType)
          this.visible = false
        }
      })
    }
  }
}
</script>
