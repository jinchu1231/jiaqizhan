<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main order-form">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack" :content="title" />
        <div class="options">
          <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <div class="main">
        <JNPF-table v-loading="listLoading" :data="list" :hasNO="false">
          <el-table-column type="index" width="50" label="序号" align="center" />
          <el-table-column prop="fullName" label="事件名称" show-overflow-tooltip width="150" />
          <el-table-column prop="interfaceName" label="接口名称" show-overflow-tooltip width="150" />
          <el-table-column prop="interfaceCode" label="接口编码" width="150" />
          <el-table-column prop="creatorTime" label="执行时间" width="140"
            :formatter="jnpf.tableDateFormat" />
          <el-table-column label="执行结果" min-width="200" prop="result" />
        </JNPF-table>
      </div>
    </div>
  </transition>
</template>

<script>
import { eventLog } from '@/api/workFlow/FlowBefore'
export default {
  components: {},
  data() {
    return {
      title: '',
      taskNodeId: '',
      listLoading: false,
      list: []
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    init(id, fullName) {
      this.taskNodeId = id || ''
      this.title = fullName + '审批节点名称'
      this.initData()
    },
    initData() {
      this.listLoading = true
      eventLog(this.taskNodeId).then(res => {
        this.list = res.data.list || []
        this.listLoading = false
      })
    },
  }
}
</script>

.main {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0;
  >>> .el-table {
    flex: 1;
  }
  .JNPF-common-search-box-right {
    margin-right: 10px;
  }
  .item-on {
    position: absolute;
    top: 50%;
    left: 0;
    right: -1px;
    margin-top: -8px;
    height: 16px;
    background: #409eff;
    -webkit-transition: all 0.4s;
    transition: all 0.4s;
    &.item-start {
      left: 50%;
      // &:after {
      //   position: absolute;
      //   top: 16px;
      //   left: 0;
      //   z-index: 1;
      //   content: "";
      //   width: 0;
      //   height: 0;
      //   border-color: #409eff transparent transparent;
      //   border-width: 6px 6px 6px 0;
      //   border-style: solid;
      // }
    }
    &.item-end {
      right: 50%;
      // &:after {
      //   position: absolute;
      //   top: 16px;
      //   right: 0;
      //   z-index: 1;
      //   content: "";
      //   width: 0;
      //   height: 0;
      //   border-color: transparent #409eff;
      //   border-width: 0 6px 6px 0;
      //   border-style: solid;
      // }
    }
  }
}
</style>