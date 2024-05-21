<template>
  <div class="sysLog JNPF-flex-main">
    <el-row class="JNPF-common-search-box" :gutter="16">
      <el-form @submit.native.prevent>
        <el-col :span="6">
          <el-form-item label="关键词">
            <el-input v-model="keyword" placeholder="请输入关键词查询" clearable
              @keyup.enter.native="search()" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="类型">
            <el-select v-model="loginType" placeholder="请选择类型" clearable>
              <el-option label="登录" :value="0"></el-option>
              <el-option label="退出" :value="1"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态">
            <el-select v-model="loginMark" placeholder="请选择状态" clearable>
              <el-option label="成功" :value="1"></el-option>
              <el-option label="失败" :value="0"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <template v-if="showAll">
          <el-col :span="6">
            <el-form-item label="时间">
              <el-date-picker v-model="pickerVal" type="datetimerange" start-placeholder="开始时间"
                end-placeholder="结束时间" value-format="timestamp" :editable="false" clearable
                :default-time="['00:00:00', '23:59:59']" align="right">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </template>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="search()">
              {{$t('common.search')}}
            </el-button>
            <el-button icon="el-icon-refresh-right" @click="reset()">
              {{$t('common.reset')}}
            </el-button>
            <el-button type="text" icon="el-icon-arrow-down" @click="showAll=true"
              v-if="!showAll">展开</el-button>
            <el-button type="text" icon="el-icon-arrow-up" @click="showAll=false" v-else>
              收起</el-button>
          </el-form-item>
        </el-col>
      </el-form>
    </el-row>
    <div class="JNPF-common-head">
      <div>
        <el-button type="danger" icon="el-icon-delete" @click="handleDel"
          :loading="btnLoading">删除</el-button>
        <el-link type="danger" :underline="false" style="margin-left:10px" @click="batchDel"
          :disabled="btnLoading">
          一键清空</el-link>
      </div>
    </div>
    <JNPF-table v-loading="listLoading" :data="loginLogData" tooltip-effect="dark" has-c
      @selection-change="handleSelectionChange">
      <el-table-column prop="loginType" label="类型">
        <template slot-scope="scope">
          <div class="login-type-box">
            <span class="circle-box"
              :class="scope.row.loginType == 0 ? 'circle-box-primary' : 'circle-box-error'" />
            {{ scope.row.loginType == 0 ? '登录' : '退出' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="creatorTime" label="时间" :formatter="jnpf.tableDateFormat"
        width="140" />
      <el-table-column prop="userName" label="用户" />
      <el-table-column prop="ipAddress" label="IP地址" />
      <el-table-column prop="ipAddressName" label="地点" />
      <el-table-column prop="browser" label="浏览器" />
      <el-table-column prop="platForm" label="操作系统" />
      <el-table-column prop="requestDuration" label="耗时(毫秒)" show-overflow-tooltip />
      <el-table-column prop="loginMark" label="状态">
        <template slot-scope="scope">
          <el-tag :type="scope.row.loginMark == 1 ? 'success' : 'danger'">
            {{ scope.row.loginMark == 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="abstracts" label="操作说明" width="120" show-overflow-tooltip />
    </JNPF-table>
    <pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
      @pagination="initData" />
  </div>
</template>
<script>
import { LogList } from '@/api/permission/userSetting'
import { delLog, deleteAll } from '@/api/system/log'
export default {
  name: 'sysLog',
  data() {
    return {
      btnLoading: false,
      showAll: false,
      keyword: '',
      loginMark: '',
      loginType: '',
      startTime: '',
      endTime: '',
      loginLogData: [],
      multipleSelection: [],
      total: 0,
      listLoading: true,
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc'
      },
      pickerVal: []
    };
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      let category = 1
      this.listLoading = true
      let data = {
        ...this.listQuery,
        keyword: this.keyword,
        startTime: this.startTime,
        endTime: this.endTime,
        loginMark: this.loginMark,
        loginType: this.loginType,
        category
      }
      LogList(data).then(res => {
        this.loginLogData = res.data.list
        this.total = res.data.pagination.total
        this.listLoading = false
        this.btnLoading = false
      })
    },
    handleSelectionChange(val) {
      const res = val.map(item => item.id)
      this.multipleSelection = res
    },
    search() {
      this.btnLoading = true
      if (this.pickerVal && this.pickerVal.length) {
        this.startTime = this.pickerVal[0]
        this.endTime = this.pickerVal[1]
      } else {
        this.startTime = ''
        this.endTime = ''
      }
      this.initData()
    },
    batchDel() {
      if (!this.loginLogData.length) return
      this.btnLoading = true
      this.$confirm('此操作会将所有日志删除，是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        deleteAll().then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData()
            }
          })
        }).catch(() => {
          this.btnLoading = false
        })
      }).catch(() => {
        this.btnLoading = false
      })
    },
    handleDel() {
      if (!this.multipleSelection.length) {
        this.$message({
          type: 'error',
          message: '请选择一条数据',
          duration: 1500,
        })
        return
      }
      const data = {
        ids: this.multipleSelection
      }
      this.$confirm('您确定要删除这些数据吗, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        this.btnLoading = true
        delLog(data).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData()
            }
          })
        }).catch(() => { this.btnLoading = false })
      }).catch(() => { this.btnLoading = false })
    },
    reset() {
      this.btnLoading = true
      this.keyword = ''
      this.list = []
      this.pickerVal = []
      this.startTime = ''
      this.endTime = ''
      this.loginMark = ''
      this.loginType = ''
      this.listQuery = {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc'
      }
      this.initData()
    }
  }
}
</script>
<style lang="scss" scoped>
.sysLog {
  height: 100%;
  overflow: hidden;
  >>> .el-tabs__nav-scroll {
    padding-top: 0 !important;
  }
  .JNPF-el_tabs {
    height: calc(100% - 60px);
    overflow: hidden;
  }
  >>> .el-tabs__content {
    overflow: auto;
    overflow-x: hidden;
    display: flex;
    flex-direction: column;
    .el-tab-pane {
      height: auto !important;
      flex: 1;
    }
  }
  .JNPF-common-search-box {
    margin-bottom: 0;
  }
}
</style>