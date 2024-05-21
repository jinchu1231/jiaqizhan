<template>
  <div class="list">
    <el-table :data="data" ref="dragTable" row-key="id" size='mini' height="100%">
      <el-table-column align="center" label="拖动" width="50" :row-key="row=>row.id">
        <template>
          <i class="drag-handler icon-ym icon-ym-darg" style="cursor: move;font-size:20px"
            title='点击拖动' />
        </template>
      </el-table-column>
      <el-table-column prop="field" label="参数名称" v-if="tableType == 1">
        <template slot-scope="scope">
          <p @click="handleItemClick(scope.row)" class="cursor_pointer">
            <span class="required-sign">{{scope.row.required?'*':''}}</span>
            {{scope.row.field}}{{scope.row.fieldName?'('+scope.row.fieldName+')':''}}
          </p>
        </template>
      </el-table-column>
      <el-table-column prop="dataType" label="参数类型" width="70" v-if="tableType == 1">
        <template slot-scope="scope">
          <span v-if="scope.row.dataType === 'varchar'">字符串</span>
          <span v-if="scope.row.dataType === 'int'">整型</span>
          <span v-if="scope.row.dataType === 'datetime'">日期时间</span>
          <span v-if="scope.row.dataType === 'decimal'">浮点</span>
          <span v-if="scope.row.dataType === 'bigint'">长整型</span>
          <span v-if="scope.row.dataType === 'text'">文本</span>
        </template>
      </el-table-column>
      <el-table-column prop="field" label="字段名称" v-if="tableType != 1">
        <template slot-scope="scope">
          <p class="cursor_pointer">
            <span class="required-sign">{{scope.row.required?'*':''}}</span>
            {{scope.row.field}}
          </p>
        </template>
      </el-table-column>
      <el-table-column prop="defaultValue" label="映射字段" width="70" v-if="tableType != 1">
        <template slot-scope="scope">
          <p class="cursor_pointer">
            {{scope.row.defaultValue}}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70">
        <template slot-scope="scope">
          <el-button type="text" @click="addOrUpdateHandle(scope.row)"
            icon="el-icon-edit-outline"></el-button>
          <el-button type="text" class="JNPF-table-delBtn" icon="el-icon-delete"
            @click="removeParameter(scope.$index)"></el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="table-actions" @click="addOrUpdateHandle()">
      <el-button type="text" icon="el-icon-plus">新建</el-button>
    </div>
  </div>
</template>
<script>
import Sortable from 'sortablejs'
export default {
  props: {
    data: {
      type: Array,
      default: () => []
    },
    tableType: {
      type: String,
      default: '1'
    }
  },
  mounted() {
    this.setSort()
  },
  methods: {
    addOrUpdateHandle(item) {
      this.$emit('addOrUpdateHandle', item)
    },
    removeParameter(item) {
      this.$emit('removeParameter', item)
    },
    handleItemClick(item) {
      this.$emit('handleItemClick', item)
    },
    setSort() {
      const el = this.$refs.dragTable.$el.querySelectorAll('.el-table__body-wrapper > table > tbody')[0]
      this.sortable = Sortable.create(el, {
        ghostClass: 'sortable-ghost', // Class name for the drop placeholder,
        setData: function (dataTransfer) {
          dataTransfer.setData('Text', '')
        },
        onEnd: evt => {
          const targetRow = this.data.splice(evt.oldIndex, 1)[0]
          this.data.splice(evt.newIndex, 0, targetRow)
        },
        handle: '.drag-handler',
      })
    },
  }
}
</script>
<style lang="scss" scoped>
.list {
}
.cursor_pointer {
  cursor: pointer;
}
</style>