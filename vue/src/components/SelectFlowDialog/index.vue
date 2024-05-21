<template>
  <el-dialog title="请选择流程" :close-on-click-modal="false" append-to-body :visible.sync="visible"
    class="JNPF-dialog template-dialog JNPF-dialog_center" lock-scroll width="400px">
    <div class="template-search">
      <el-input placeholder="输入关键字搜索" v-model="keyword" suffix-icon="el-icon-search" clearable />
    </div>
    <el-scrollbar class="template-list" v-if="getFlowList.length">
      <div class="template-item" v-for="item in getFlowList" :key="item.id"
        @click="selectFlow(item)">
        {{item.fullName}}
      </div>
    </el-scrollbar>
    <el-empty class="template-description" v-else description="暂无数据"></el-empty>
  </el-dialog>
</template>

<script>
export default {
  name: 'SelectFlowDialog',
  data() {
    return {
      visible: false,
      flowList: [],
      keyword: ''
    }
  },
  computed: {
    getFlowList() {
      let list = JSON.parse(JSON.stringify(this.flowList)) || []
      if (this.keyword) return list.filter(o => o.fullName.indexOf(this.keyword) !== -1)
      return list
    }
  },
  methods: {
    init(flowList) {
      this.visible = true
      this.flowList = flowList || []
    },
    selectFlow(item) {
      this.$emit('selectFlow', item)
      this.visible = false
    }
  }
}
</script>