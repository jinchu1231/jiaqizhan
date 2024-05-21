<template>
  <el-drawer title="执行队列" :visible.sync="drawerVisible" :wrapperClosable="false" size="450px"
    append-to-body class="JNPF-common-drawer columnSettings-drawer">

    <template>
      <JNPF-table v-loading="listLoading" :data="list">
        <el-table-column prop="fullName" label="名称" />
        <el-table-column prop="executionTime" label="执行时间" :formatter="jnpf.tableDateFormat"
          width="140" />
        <el-table-column prop="state" label="状态" width="70" align="center">
          <template slot-scope="scope">
            <span
              :style="{color:scope.row.state==1?'#67C23A':''}">{{scope.row.state==1?'执行中':'等待'}}</span>
          </template>
        </el-table-column>
      </JNPF-table>
    </template>
  </el-drawer>
</template>
<script>
import { getQueueList } from '@/api/onlineDev/integrate'
export default ({
  data() {
    return {
      drawerVisible: false,
      list: [],
      listLoading: false
    }
  },
  methods: {
    open() {
      this.drawerVisible = true
      this.listLoading = false
      this.initData()
    },
    initData() {
      this.list = []
      this.listLoading = true
      getQueueList().then(res => {
        this.list = res.data || []
        this.listLoading = false
      }).catch()
    }
  }
})
</script>
<style lang="scss" scoped>
.columnSettings-drawer {
  height: 100%;
  .JNPF-flex-main {
    overflow: hidden;
  }
  .menu-tab {
    height: 100%;
    >>> .el-tabs__content {
      height: calc(100% - 40px);
    }
  }
}
</style>
