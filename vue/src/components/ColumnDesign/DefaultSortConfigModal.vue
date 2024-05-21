
<template>
  <el-dialog title="默认排序配置" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center dialog_height" lock-scroll width="600px" append-to-body>
    <JNPF-table :data="list" ref="dragTable" :hasNO="false" row-key="id">
      <el-table-column align="center" label="拖动" width="50">
        <template>
          <i class="drag-handler icon-ym icon-ym-darg" style="cursor: move;font-size:20px"
            title='点击拖动' />
        </template>
      </el-table-column>
      <el-table-column prop="childColumns" label="字段">
        <template slot-scope="scope">
          <el-select v-model="scope.row.field" placeholder="请选择排序字段">
            <el-option v-for="(item,i) in getOptions(scope.$index)" :key="i" :label="item.fullName"
              :value="item.id">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="align" label="类型">
        <template slot-scope="scope">
          <el-radio-group v-model="scope.row.sort">
            <el-radio label="asc">升序</el-radio>
            <el-radio label="desc">降序</el-radio>
          </el-radio-group>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="50">
        <template slot-scope="scope">
          <el-button size="mini" type="text" class="JNPF-table-delBtn"
            @click="handleDel(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </JNPF-table>
    <div class="table-actions" @click="addHandle()">
      <el-button type="text" icon="el-icon-plus">添加</el-button>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="closeDialog(1)">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import Sortable from 'sortablejs'
export default {
  data() {
    return {
      visible: false,
      options: [],
      list: [],
      data: {},
      sortTypeOptions: [
        { id: 'asc', fullName: '升序' },
        { id: 'desc', fullName: '降序' },
      ],
    }
  },
  methods: {
    init(option) {
      this.data = option ? JSON.parse(JSON.stringify(option)) : {}
      this.list = this.data.list
      this.visible = true
      this.$nextTick(() => {
        this.setSort()
      });
    },
    setSort() {
      const el = this.$refs.dragTable.$el.querySelectorAll('.el-table__body-wrapper > table > tbody')[0]
      this.sortable = Sortable.create(el, {
        ghostClass: 'sortable-ghost', // Class name for the drop placeholder,
        setData: function (dataTransfer) {
          dataTransfer.setData('Text', '')
        },
        onEnd: evt => {
          const targetRow = this.list.splice(evt.oldIndex, 1)[0]
          this.list.splice(evt.newIndex, 0, targetRow)
        },
        handle: '.drag-handler',
      })
    },
    getOptions(index) {
      let options = this.data.columnOptions.filter(o => !['editor'].includes(o.__config__.jnpfKey));;
      for (let i = 0; i < this.list.length; i++) {
        const e = this.list[i];
        if (e.field && index !== i) options = options.filter(o => o.id !== e.field);
      }
      return options;
    },
    closeDialog() {
      let isOk = true;
      for (let i = 0; i < this.list.length; i++) {
        if (!this.list[i].field) {
          this.$message({ message: '字段不能为空', type: 'warning' })
          isOk = false;
          break;
        }
      }
      if (!isOk) return;
      this.$emit('confirm', this.list)
      this.visible = false
    },
    handleDel(index) {
      this.list.splice(index, 1);
    },
    addHandle() {
      let id = 'sort' + this.jnpf.idGenerator()
      let item = { field: "", sort: 'desc', id: id }
      this.list.push(item)
    }
  }
}
</script>
<style lang="scss" scoped>
.JNPF-dialog_center {
  >>> .el-dialog__body {
    height: 70vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding: 0px 3px 2px !important;
    // padding-bottom: 20px !important;
  }
}
</style>
