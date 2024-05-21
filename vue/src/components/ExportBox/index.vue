<template>
  <el-dialog title="导出" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center JNPF-dialog-export" lock-scroll width="600px">
    <el-form label-position="top">
      <el-form-item label='导出方式'>
        <p slot="label" class="export-label">导出方式</p>
        <el-radio-group v-model="type">
          <el-radio :label="0">当前页面数据</el-radio>
          <el-radio :label="1">全部页面数据</el-radio>
          <el-radio :label="2" :disabled="!selectIds || !selectIds.length">当前选择数据</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item class="export-line">
        <p slot="label" class="export-label">列表数据<span>请选择导出字段</span></p>
        <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll"
          @change="handleCheckAllChange">全选</el-checkbox>
        <el-checkbox-group v-model="columns" @change="handleCheckedChange">
          <el-checkbox v-for="item in columnList" :label="item.prop" :key="item.prop"
            class="column-item" :title="item.label">
            {{item.label}}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <p class="footer-tip">提示:系统将导出列表中选中的数据</p>
      <el-button @click="visible=false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="downLoad">导 出</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { noGroupList } from '@/components/Generator/generator/comConfig'
export default {
  data() {
    return {
      visible: false,
      btnLoading: false,
      type: 0,
      columns: [],
      checkAll: true,
      isIndeterminate: false,
      columnList: [],
      selectIds: []
    }
  },
  methods: {
    init(columnList, selectIds) {
      this.visible = true
      this.checkAll = true
      this.isIndeterminate = false
      columnList = JSON.parse(JSON.stringify(columnList))
      this.columnList = columnList.filter(o => !noGroupList.includes(o.__config__.jnpfKey))
      this.selectIds = selectIds || []
      this.columns = columnList.map(o => o.prop)
    },
    handleCheckAllChange(val) {
      this.columns = val ? this.columnList.map(o => o.prop) : [];
      this.isIndeterminate = false;
    },
    handleCheckedChange(value) {
      let checkedCount = value.length;
      this.checkAll = checkedCount === this.columnList.length;
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.columnList.length;
    },
    downLoad() {
      if (!this.columns.length) return this.$message.warning(`请至少选择一个导出字段`)
      this.$emit('download', { dataType: this.type, selectKey: this.columns, selectIds: this.selectIds })
    }
  }
}
</script>
