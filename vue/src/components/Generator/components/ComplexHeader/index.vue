
<template>
  <el-dialog title="复杂表头配置" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center dialog_height" lock-scroll width="1000px" append-to-body>
    <JNPF-table :data="list" ref="dragTable" :hasNO="false" row-key="id">
      <el-table-column align="center" label="拖动" width="50">
        <template>
          <i class="drag-handler icon-ym icon-ym-darg" style="cursor: move;font-size:20px"
            title='点击拖动' />
        </template>
      </el-table-column>
      <el-table-column prop="fullName" label="表头列名">
        <template slot-scope="scope">
          <el-input v-model="scope.row.fullName" placeholder="请输入名称" />
        </template>
      </el-table-column>
      <el-table-column prop="childColumns" label="子列">
        <template slot-scope="scope">
          <el-select v-model="scope.row.childColumns" multiple placeholder="请选择子列" v-if="type==1">
            <el-option v-for="(item,i) in getColumnOptions(scope.$index)" :key="i"
              :label="item.fullName" :value="item.id">
            </el-option>
          </el-select>
          <el-select v-model="scope.row.childColumns" multiple placeholder="请选择子列" v-else>
            <el-option v-for="(item,i) in getColumnOptions(scope.$index)" :key="i"
              :label="item.__config__.label" :value="item.__vModel__">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="align" label="对齐方式">
        <template slot-scope="scope">
          <el-select v-model="scope.row.align" placeholder="请选择" filterable>
            <el-option v-for="item in alignOptions" :key="item.value" :label="item.fullName"
              :value="item.value">
            </el-option>
          </el-select>
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
  props: ['type'],
  components: {},
  data() {
    return {
      visible: false,
      list: [],
      options: [],
      alignOptions: [
        {
          value: 'left',
          fullName: '左对齐'
        }, {
          value: 'center',
          fullName: '居中对齐'
        }, {
          value: 'right',
          fullName: '右对齐'
        }],
    }
  },
  methods: {
    init(data, option) {
      this.list = data ? JSON.parse(JSON.stringify(data)) : []
      this.options = option ? JSON.parse(JSON.stringify(option)) : []
      this.visible = true
      this.$nextTick(() => {
        this.setSort()
      });
    },
    closeDialog() {
      if (this.type == 1) this.$store.commit("generator/SET_HEADER_DATA", JSON.parse(JSON.stringify(this.list)))
      this.$emit('closeForm', this.list)
      this.visible = false
    },
    getColumnOptions(index) {
      let childColumns = []
      for (let i = 0; i < this.list.length; i++) {
        const e = this.list[i];
        for (let j = 0; j < e.childColumns.length; j++) {
          const element = e.childColumns[j];
          if (i != index) childColumns.push(element)
        }
      }
      return this.options.filter(o => !childColumns.includes(o.__vModel__))
    },
    handleDel(index) {
      this.list.splice(index, 1);
    },
    addHandle() {
      let id = 'complex' + this.jnpf.idGenerator()
      let item = { fullName: "表头列名" + id, childColumns: [], align: 'center', id: id }
      this.list.push(item)
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
