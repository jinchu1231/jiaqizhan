<template>
  <el-dialog title="组件属性配置" :close-on-click-modal="false" :visible.sync="dialogVisible"
    class="JNPF-dialog JNPF-dialog_center dialog_height" lock-scroll width="600px" append-to-body>
    <el-form ref="activeData" :model="activeData" label-width="80px" v-loading="loading">
      <template v-if="activeData.jnpfKey === 'select'">
        <el-form-item label="数据来源">
          <el-radio-group v-model="activeData.__config__.dataType" size="small"
            style="text-align:center" @change="dataTypeChange">
            <el-radio-button label="static">静态数据</el-radio-button>
            <el-radio-button label="dictionary">数据字典</el-radio-button>
            <el-radio-button label="dynamic">远端数据</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="activeData.__config__.dataType === 'static'">
          <draggable :list="activeData.options" :animation="340" group="selectItem"
            handle=".option-drag">
            <div v-for="(item, index) in activeData.options" :key="index" class="select-item">
              <div class="select-line-icon option-drag">
                <i class="icon-ym icon-ym-darg" />
              </div>
              <el-input v-model="item.fullName" placeholder="选项名" size="small" />
              <el-input v-model="item.id" placeholder="选项值" size="small" />
              <div class="close-btn select-line-icon" @click="activeData.options.splice(index, 1)">
                <i class="el-icon-remove-outline" />
              </div>
            </div>
          </draggable>
          <div style="margin-left: 29px;">
            <el-button style="padding-bottom: 0" icon="el-icon-circle-plus-outline" type="text"
              @click="addSelectItem">
              添加选项
            </el-button>
            <el-divider direction="vertical"></el-divider>
            <el-button style="padding-bottom: 0" type="text" @click="updateSelectItem">
              批量编辑
            </el-button>
          </div>
        </template>
        <template v-if="activeData.__config__.dataType === 'dictionary'">
          <el-form-item label="数据字典">
            <el-row class="jnpf-el-row">
              <el-col :span="18">
                <JnpfTreeSelect :options="dicOptions" v-model="activeData.__config__.dictionaryType"
                  placeholder="请选择数据字典" lastLevel clearable @change="dictionaryTypeChange"
                  @selectChange="selectChange">
                </JnpfTreeSelect>
              </el-col>
              <el-col :span="6">
                <el-button @click="goDictionary()">
                  添加</el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item label="存储字段">
            <el-select v-model="activeData.props.value" placeholder="请选择存储字段">
              <el-option label="id" value="id"></el-option>
              <el-option label="enCode" value="enCode"></el-option>
            </el-select>
          </el-form-item>
        </template>
        <template v-if="activeData.__config__.dataType === 'dynamic'">
          <el-form-item label="远端数据">
            <interface-dialog :value="activeData.__config__.propsUrl"
              :title="activeData.__config__.propsName" popupTitle="远端数据" @change="propsUrlChange" />
          </el-form-item>
          <el-form-item label="存储字段">
            <el-autocomplete class="inline-input" v-model="activeData.props.value"
              :fetch-suggestions="querySearch" placeholder="请输入存储字段" style="width:100%"
              @select="handleSelect($event,'value')">
              <template slot-scope="{ item }">
                <div class="sale-order-popper-item">
                  <span>{{ item.defaultValue}}</span>
                </div>
              </template>
            </el-autocomplete>
          </el-form-item>
          <el-form-item label="显示字段">
            <el-autocomplete class="inline-input" v-model="activeData.props.label"
              :fetch-suggestions="querySearch" placeholder="请输入显示字段" style="width:100%"
              @select="handleSelect($event,'label')">
              <template slot-scope="{ item }">
                <div class="sale-order-popper-item">
                  <span>{{ item.defaultValue}}</span>
                </div>
              </template>
            </el-autocomplete>
          </el-form-item>
        </template>
      </template>

      <el-form-item label="日期类型" v-if="activeData.jnpfKey === 'datePicker'">
        <jnpf-select v-model="activeData.format" placeholder="请选择日期类型" :options="dateFormatOptions"
          :props='props' />
      </el-form-item>
      <el-form-item label="时间类型" v-if="activeData.jnpfKey === 'timePicker'">
        <jnpf-select v-model="activeData.format" placeholder="请选择时间类型" :options="timeFormatOptions"
          :props='props' />
      </el-form-item>
      <el-form-item label=""
        v-if="['organizeSelect', 'depSelect', 'userSelect'].includes(activeData.jnpfKey)">
        <el-checkbox v-model="activeData.isIncludeSubordinate">
          查询当前{{ activeData.jnpfKey === 'organizeSelect' ? '组织及子组织' : activeData.jnpfKey === 'depSelect' ? '部门及子部门' : '用户及下属' }}
        </el-checkbox>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="handleSubmit">{{$t('common.confirmButton')}}</el-button>
    </span>
    <div>
      <el-dialog :visible.sync="dicVisible" append-to-body
        class="JNPF-dialog JNPF-dialog_center JNPF-dialog-tree-select" lock-scroll width="80%"
        @close="defaultValueChange">
        <dicIndex ref="dicIndex"></dicIndex>
      </el-dialog>
    </div>
    <BatchEditing v-if="updateVisible" ref="batchEditing" @change="handleSure" />
  </el-dialog>
</template>
<script>
import webViewMixin from './webViewMixin';
export default {
  mixins: [webViewMixin],
  data() {
    return {
      dialogVisible: false,
      activeData: {
        __config__: {
          dataType: 'static'
        }
      },
      radio1: '静态数据',
      loading: true,
      updateVisible: false,
      dicVisible: false,
      dateFormatOptions: [
        { id: 'yyyy', fullName: 'yyyy' },
        { id: 'yyyy-MM', fullName: 'yyyy-MM' },
        { id: 'yyyy-MM-dd', fullName: 'yyyy-MM-dd' },
        { id: 'yyyy-MM-dd HH:mm', fullName: 'yyyy-MM-dd HH:mm' },
        { id: 'yyyy-MM-dd HH:mm:ss', fullName: 'yyyy-MM-dd HH:mm:ss' },
      ],
      props: {
        label: "fullName",
        value: "id"
      },
      timeFormatOptions: [
        { id: 'HH:mm:ss', fullName: 'HH:mm:ss' },
        { id: 'HH:mm', fullName: 'HH:mm' },
      ],
    }
  },
  methods: {
    init(item) {
      this.activeData = JSON.parse(JSON.stringify(item))
      this.dialogVisible = true
      this.loading = false
      this.getDicOptions()
      console.log(item)
    },
    handleSubmit() {
      this.$emit('confirm', this.activeData)
    },
  }
}
</script>
<style lang="scss" scoped>
.jnpf-el-row {
  >>> .el-input__inner {
    border-radius: 4px 0 0 4px !important;
  }

  >>> .el-button {
    border-left: 0;
    background-color: #f5f7fa;
    font-size: 13px;
    color: #909399;
    border-radius: 0 4px 4px 0;
    line-height: 12px;
  }
  >>> .el-button:hover {
    border-color: #dcdfe6;
  }
}
>>> .select-item,
.select-item {
  display: flex;
  border: 1px dashed #fff;
  box-sizing: border-box;
  & .el-input + .el-input {
    margin-left: 4px;
  }
  & + .select-item {
    margin-top: 4px;
  }
  &.sortable-chosen {
    border: 1px dashed #409eff;
  }
  .select-line-icon {
    line-height: 32px;
    font-size: 22px;
    padding: 0 4px;
    color: #606266;
    .icon-ym-darg {
      font-size: 20px;
      line-height: 31px;
      display: inline-block;
    }
  }
  .close-btn {
    cursor: pointer;
    color: #f56c6c;
  }
  .option-drag {
    cursor: move;
  }
}
</style>