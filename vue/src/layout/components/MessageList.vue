<template>
  <div>
    <el-drawer :visible.sync="drawer" direction="rtl" size="280px"
      class="JNPF-messageList JNPF-common-drawer" :show-close="false" :before-close="handleClose">
      <div slot="title" class="title-box">
        <div class="title">站内消息</div>
        <el-link type="primary" :underline="false" @click="gotoCenter">更多</el-link>
      </div>
      <el-input v-model="listQuery.keyword" placeholder="搜索：请输入关键词" clearable
        @keyup.enter.native="search()" class="search-input">
        <i slot="suffix" class="el-input__icon el-icon-search" @click="search" title="搜索" />
      </el-input>
      <div class="tool">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            {{type}}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item :command="item.enCode" v-for="item in messageList" :key="item.enCode">
              {{ item.fullName }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="is-read-box">仅显示未读
          <el-switch v-model="isNoRead"></el-switch>
        </div>
      </div>
      <div class="JNPF-messageList-box" v-loading="loading && listQuery.currentPage==1"
        ref="messageListBody">
        <div v-if="list.length">
          <div v-for="(item,i) in list" :key="i" class="JNPF-messageList-item"
            @click="readInfo(item)" :title="item.title">
            <el-badge is-dot :hidden="item.isRead=='1'" type="warning">
              <i class="icon-ym icon-ym-xitong JNPF-messageList-item-icon"
                v-if="item.type == 1"></i>
              <i class="icon-ym icon-ym-generator-notice JNPF-messageList-item-icon notice-icon"
                v-else-if="item.type == 3"></i>
              <i class="icon-ym icon-ym-portal-schedule JNPF-messageList-item-icon schedule-icon"
                v-else-if="item.type == 4"></i>
              <i class="icon-ym icon-ym icon-ym-generator-flow JNPF-messageList-item-icon flow-icon"
                v-else></i>
            </el-badge>
            <div class="JNPF-messageList-txt">
              <p class="title">{{item.title}}</p>
              <p class="name">
                <span class="user">{{item.releaseUser}}</span>
                <span class="time">{{item.releaseTime| toDateValue()}}</span>
              </p>
            </div>
          </div>
        </div>
        <p class="noData-txt" v-else>{{$t('common.noData')}}</p>
      </div>
      <div class="bottom-box" @click="readAll">全部已读</div>
    </el-drawer>
    <el-dialog title="详情" :close-on-click-modal="false" :visible.sync="visible"
      class="JNPF-dialog JNPF-dialog_center JNPF-dialog-notice" lock-scroll width="80%">
      <div class="notice-wrapper" v-loading="loading">
        <div class="notice-hd">
          <h1 class="title">{{info.title}}</h1>
          <div class="info">
            <span>{{info.releaseTime|toDate()}}</span><span>{{info.releaseUser}}</span>
          </div>
        </div>
        <p class="excerpt">{{ info.excerpt }}</p>
        <div class="main">
          <div class="ql-editor" v-html="info.bodyText"></div>
        </div>
        <div class="file-list" v-if="files.length">
          <JnpfUploadFile v-model="files" disabled detailed />
        </div>
      </div>
    </el-dialog>
    <ScheduleView v-if='scheduleVisible' ref="schedule" />
  </div>
</template>

<script>
import { getMessageList, ReadInfo, MessageAllRead } from '@/api/system/message'
import ScheduleView from '@/components/ScheduleView'

export default {
  name: 'messageList',
  components: { ScheduleView },
  data() {
    return {
      drawer: false,
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        keyword: '',
        type: '',
        isRead: 0
      },
      messageList: [],
      list: [],
      activeItem: {},
      loading: true,
      visible: false,
      finish: false,
      files: [],
      info: {},
      type: '全部',
      isNoRead: true,
      scheduleVisible: false
    }
  },
  watch: {
    isNoRead(val) {
      this.listQuery.isRead = ''
      if (val) this.listQuery.isRead = 0
      this.init()
    }
  },
  created() {
    this.getDictionaryData()
  },
  methods: {
    init() {
      this.finish = false
      this.drawer = true
      this.listQuery.currentPage = 1
      this.list = []
      this.getList()
      this.$nextTick(() => {
        this.bindScroll()
      })
    },
    getDictionaryData() {
      this.messageList = []
      this.$store.dispatch('base/getDictionaryData', { sort: 'msgSourceType' }).then((res) => {
        let item = { id: '', fullName: '全部', enCode: '' }
        this.messageList.push(item, ...res)
      })
    },
    getList() {
      this.loading = true
      getMessageList(this.listQuery).then(res => {
        if (res.data.list.length < this.listQuery.pageSize) {
          this.finish = true
        }
        this.list = [...this.list, ...res.data.list]
        this.loading = false
      })
    },
    bindScroll() {
      let _this = this,
        vBody = _this.$refs.messageListBody;
      vBody.addEventListener("scroll", function () {
        if (vBody.scrollHeight - vBody.clientHeight - vBody.scrollTop <= 200 && !_this.loading && !_this.finish) {
          _this.listQuery.currentPage += 1
          _this.getList()
        }
      });
    },
    readInfo(item) {
      ReadInfo(item.id).then(res => {
        this.info = res.data
        this.files = res.data.files ? JSON.parse(res.data.files) : []
        if (item.isRead == '0') {
          item.isRead = '1'
          this.$emit('read')
        }
        if (item.type == 4) {
          let bodyText = JSON.parse(res.data.bodyText)
          if (bodyText.type == 3) return
          this.scheduleVisible = true
          this.$nextTick(() => {
            this.$refs.schedule.init(bodyText.id, '', bodyText.groupId)
          })
          return
        }
        if (item.type == 2 && item.flowType == 2) {
          let bodyText = JSON.parse(res.data.bodyText)
          this.drawer = false
          this.$router.push('/workFlow/entrust?config=' + bodyText.type)
        } else {
          if (item.type == 1 || item.type == 3) {
            this.visible = true
          } else {
            if (!res.data.bodyText) return
            this.drawer = false
            const Base64 = require('js-base64').Base64
            this.$router.push('/workFlowDetail?config=' + encodeURIComponent(Base64.encode(res.data.bodyText)))
          }
        }
      })
    },
    gotoCenter() {
      this.drawer = false
      this.$router.push('/messageRecord')
    },
    readAll() {
      this.$confirm('您确定全部标识为已读状态, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        const query = {
          keyword: this.listQuery.keyword,
          type: this.listQuery.type,
          isRead: this.isNoRead ? 0 : ''
        }
        MessageAllRead(query).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.$emit('read', true)
              if (this.isNoRead) {
                this.init()
              } else {
                for (let i = 0; i < this.list.length; i++) {
                  this.$set(this.list[i], 'isRead', '1')
                }
              }
            }
          })
        })
      }).catch(() => { });
    },
    handleClose(done) {
      let vBody = this.$refs.messageListBody;
      vBody.removeEventListener("scroll", function () { });
      done();
    },
    handleCommand(e) {
      let item = this.messageList.filter(o => o.enCode == e)[0]
      this.type = item.fullName
      this.listQuery.type = e || ''
      this.init()
    },
    search() {
      this.init()
    }
  }
}
</script>

<style lang="scss" scoped>
.main >>> a {
  color: #06c !important;
}
.main {
  margin-top: 0px !important;
  >>> .ql-editor {
    white-space: unset;
  }
}
.notice-wrapper {
  .notice-hd {
    padding: 24px 0;
    .title {
      color: #303133;
      margin-top: 0;
      margin-bottom: 8px;
    }
    .info {
      color: #606266;
      padding-bottom: 24px;
      line-height: normal;
    }
  }
  .excerpt {
    color: #303133;
    margin-bottom: 8px !important;
  }
}
.JNPF-messageList {
  .title-box {
    display: flex;
    .title {
      flex: 1;
    }
  }
  .search-input {
    >>> .el-input__inner {
      border-radius: 0;
      border-right: none;
      border-left: none;
    }
    .el-input__icon {
      cursor: pointer;
      &:hover {
        color: #1890ff;
      }
    }
  }
  .is-read-box {
    font-size: 14px;
    color: #999999;
    display: flex;
    align-items: center;

    >>> .el-switch {
      margin-left: 4px;
    }
  }
  .JNPF-messageList-title {
    display: flex;
    align-items: center;
    padding-right: 20px;
    .title {
      font-size: 18px;
      margin-right: 10px;
    }
    .icon-menu {
      font-size: 18px;
      color: #9fafbe;
      cursor: pointer;
    }
  }
  >>> .el-drawer__header {
    border: none;
  }
  >>> .el-drawer__body {
    overflow: hidden;
    display: flex;
    flex-direction: column;
    position: relative;
    .tool {
      height: 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 10px;
      background: #fff;
    }
    .JNPF-messageList-box {
      overflow: auto;
      overflow-x: hidden;
      position: relative;
      flex: 1;
      border-top: 1px solid #f5f7f9;
      >>> .el-loading-mask {
        top: 100px;
      }
      .noData-txt {
        font-size: 14px;
        color: #909399;
        line-height: 20px;
        text-align: center;
        padding-top: 20px;
      }
    }
    .JNPF-messageList-item {
      position: relative;
      display: block;
      padding: 0 10px;
      background-color: #fff;
      border-bottom: 1px solid #f5f7f9;
      height: 60px;
      display: flex;
      align-items: center;
      cursor: pointer;
      &:hover {
        background-color: #f5f7f9;
      }
      .flow-icon {
        background-color: #33cc51 !important;
      }
      .notice-icon {
        background-color: #e09f0c !important;
      }
      .schedule-icon {
        background-color: #7777ff !important;
      }
      .JNPF-messageList-item-icon {
        background-color: #1890ff;
        width: 36px;
        height: 36px;
        display: inline-block;
        font-size: 22px;
        color: #fff;
        line-height: 36px;
        border-radius: 50%;
        text-align: center;
      }
      .JNPF-messageList-txt {
        margin-left: 14px;
        overflow: hidden;
        flex: 1;
        padding-top: 1px;
        .title {
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          font-size: 14px;
          color: #333;
          margin-bottom: 5px;
        }
        .name {
          font-size: 12px;
          color: #999;
          display: flex;
          justify-content: space-between;
          .user {
            flex: 1;
            min-width: 0;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
          }
        }
      }
    }
    .bottom-box {
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: #303133;
      cursor: pointer;
      border-top: 1px solid #e3e6eb;

      &:hover {
        color: #1890ff;
      }
    }
  }
}
.main >>> a {
  color: #06c !important;
}
</style>
