<template>
  <div class="popupSelect-container">
    <el-dialog title="日志详情" :close-on-click-modal="false" :visible.sync="visible"
      class="JNPF-dialog JNPF-dialog_center log-detail-modal" lock-scroll append-to-body
      width="800px">
      <div class="log-detail-board" v-loading="listLoading">
        <div class="log-left-board">
          <div class="item-box" :class="{ active: activeItem.id == item.id }"
            v-for="(item, index) in list" @click="handleClick(item,index)" :key='item.id'>
            <div class="top">
              <span class="fullName">{{ item.nodeName }}</span>
              <i class="icon icon-ym icon-ym-fail" v-if="!item.resultType" />
              <i class="icon icon-ym icon-ym-success" v-else />
            </div>
            <div class="bottom">
              <i class="icon-ym icon-ym-clock pr-4px" v-if="item.type == 1" />
              <i class="icon-ym icon-ym-btn-refresh pr-4px" v-else />
              {{ item.endTime|toDate('yyyy-MM-dd HH:mm') }}
            </div>
          </div>
        </div>
        <div class="log-center-board">
          <div class="top">
            <span>{{ activeItem.nodeName }}</span>
            <div v-if="!activeItem.resultType">
              <el-button @click="handleUpdateNodes">去修改节点</el-button>
              <el-button @click="handleRedoNodes" :disabled="!activeItem.isRetry"
                :loading="redoNodesLoading">
                {{ redoNodesLoading ? '正在修复中...' : '节点重试' }}</el-button>
            </div>
          </div>
          <div class="time-box">
            <span>开始时间：{{ activeItem.startTime|toDate('yyyy-MM-dd HH:mm') }}</span>
            <span>结束时间：{{ activeItem.endTime|toDate('yyyy-MM-dd HH:mm') }}</span>
          </div>
          <el-radio-group v-model="activeKey" class="mb-10px">
            <el-radio-button :label="1">输入</el-radio-button>
            <el-radio-button :label="2" v-if="!activeItem.resultType">错误</el-radio-button>
          </el-radio-group>
          <el-input v-model="getTextareaValue" type="textarea"
            :autosize="{ minRows: 19, maxRows: 19}" readonly />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTaskInfo, nodeRetryTask } from '@/api/onlineDev/integrate'
export default {
  name: 'LogDetail',
  model: {
    prop: 'value',
    event: 'input'
  },
  data() {
    return {
      list: [],
      activeKey: 1,
      activeItem: {},
      visible: false,
      msgInfo: '',
      id: '',
      listLoading: false,
      redoNodesLoading: false,
      activeIndex: 0
    }
  },
  computed: {
    getTextareaValue() {
      return this.activeKey === 1 ? this.msgInfo : this.activeItem.errorMsg
    }
  },
  methods: {
    openDialog(id) {
      this.visible = true
      this.id = id || ''
      this.isInit = false
      this.activeKey = 1
      this.initData(true)
    },
    handleClick(item, index) {
      this.activeItem = item;
      this.activeIndex = index;
      this.activeKey = 1;
    },
    handleUpdateNodes() {
      this.$emit('updateNodes')
    },
    handleRedoNodes() {
      this.$confirm('确定将本节点进行重试?', this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        const query = {
          id: this.id,
          nodeId: this.activeItem.id,
        };
        this.redoNodesLoading = true;
        nodeRetryTask(query).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1000,
            onClose: () => {
              this.initData()
              this.redoNodesLoading = false;
            }
          });
        })
      }).catch(() => {
        this.redoNodesLoading = false;
      });
    },
    initData(isInit) {
      this.listLoading = true
      getTaskInfo(this.id).then(res => {
        this.msgInfo = res.data.data
        this.list = res.data.list || []
        if (this.list.length) this.activeItem = this.list[isInit ? 0 : this.activeIndex]
        this.listLoading = false
      }).catch()
    }
  }
}
</script>
<style lang="scss" scoped>
.log-detail-modal {
  >>> .el-dialog__body {
    padding: 0 !important;
  }
  .log-detail-board {
    display: flex;
    height: 550px;
    .log-left-board {
      width: 270px;
      height: 100%;
      overflow-y: auto;
      border-right: 1px solid #d9d9d9;
      .active {
        background-color: #bae7ff !important;
      }
      .item-box {
        padding: 8px 20px;
        cursor: pointer;

        &:hover {
          background-color: #f5f5f5;
        }
        .top {
          display: flex;
          align-items: center;
          .fullName {
            flex: 1;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 16px;
          }
          .icon {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            padding: 5px;
            text-align: center;
            color: #fff;
            transform: scale(0.65);
          }
          .icon-ym-fail {
            background-color: #ff4d4d;
          }
          .icon-ym-success {
            background-color: #55d187;
          }
        }
        .bottom {
          display: flex;
          align-items: center;
          .icon-ym-btn-refresh {
            color: #1890ff;
          }
        }
        .pr-4px {
          padding-right: 4px;
        }
      }
    }
    .log-center-board {
      width: 100%;
      padding: 15px 20px;
      .top {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .time-box {
        padding: 20px 0;
        display: flex;
        span {
          flex: 1;
        }
      }
      .mb-10px {
        margin-bottom: 10px !important;
      }
    }
  }
}
</style>