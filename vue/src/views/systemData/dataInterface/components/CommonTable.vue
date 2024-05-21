<template>
  <div class="list" style="margin-top:18px">
    <el-table :data="data" ref="dragTable" row-key="id" size='mini' height="100%" width="100%">
      <el-table-column prop="field" label="参数名" min-width="200">
        <template slot-scope="scope">
          <el-input v-model="scope.row.field" placeholder="请输入参数名"></el-input>
        </template>
      </el-table-column>
      <el-table-column prop="dataType" label="类型" width="285">
        <template slot-scope="scope">
          <el-select v-model="scope.row.dataType" placeholder="请选择类型">
            <el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="field" label="来源" width="285">
        <template slot-scope="scope">
          <el-select v-model="scope.row.source" placeholder="请选择" :disabled="isPostPosition == 1"
            @change="scope.row.defaultValue = ''">
            <el-option v-for="item in sourceOptions" :key="item.value" :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="field" label="参数值" width="285">
        <template slot-scope="scope">
          <el-input v-model="scope.row.defaultValue" placeholder="请输入参数值"
            v-if="scope.row.source == 3"></el-input>
          <el-select v-model="scope.row.defaultValue" placeholder="请选择" v-if="scope.row.source == 1"
            :disabled="!scope.row.source" style="width: 100%;">
            <el-option v-for="item in parameterJson" :key="item.id" :label="item.field"
              :value="item.field">
            </el-option>
          </el-select>
          <el-select v-model="scope.row.defaultValue" placeholder="请选择"
            v-if="scope.row.source == 4 || scope.row.source == 5" :disabled="!scope.row.source"
            style="width: 100%;">
            <el-option v-for="item in pagingParameters" :key="item.value" :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <JnpfTreeSelect :options="sourceData" :isDataInterface="1" v-if="scope.row.source == 2"
            v-model="scope.row.defaultValue" placeholder="请选择数据接口" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="75">
        <template slot-scope="scope">
          <el-button type="text" class="JNPF-table-delBtn" icon="el-icon-delete"
            @click="removeParameter(scope.$index)"></el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="table-actions" @click="addHandle()">
      <el-button type="text" icon="el-icon-plus">{{ addTableName }}</el-button>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    data: {
      type: Array,
      default: () => []
    },
    sourceData: {
      type: Array,
      default: () => []
    },
    sourceOptions: {
      type: Array,
      default: () => []
    },
    pagingParameters: {
      type: Array,
      default: () => []
    },
    parameterJson: {
      type: Array,
      default: () => []
    },
    isPostPosition: {
      type: Number,
      default: 0
    },
  },
  data() {
    return {
      defaultProps: {
        children: 'children',
        label: 'label'
      },

      dataTypeOptions: [{ label: '字符串', value: 'varchar' }, { label: '整型', value: 'int' }, { label: '日期时间', value: 'datetime' }, { label: '浮点', value: 'decimal' }],
      addTableName: '添加Header参数',
    }
  },
  methods: {
    changTable(e) {
      this.addTableName = e.addTableName
    },
    addHandle() {
      this.$emit('addHandle')
    },
    removeParameter(index) {
      this.$emit('removeCommonTable', index)
    }
  }
}
</script>