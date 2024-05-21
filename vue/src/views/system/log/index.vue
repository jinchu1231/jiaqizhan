<template>
  <div class="JNPF-common-layout systemLogs">
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="listQuery.keyword" placeholder="请输入关键词查询" clearable
                @keyup.enter.native="search()" />
            </el-form-item>
          </el-col>
          <template v-if="activeName != 1">
            <el-col :span="6">
              <el-form-item label="请求方式">
                <el-select v-model="listQuery.requestMethod" placeholder="请选择请求方式" clearable>
                  <el-option label="GET" value="GET"></el-option>
                  <el-option label="POST" value="POST"></el-option>
                  <el-option label="PUT" value="PUT"></el-option>
                  <el-option label="DELETE" value="DELETE"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="activeName == 5 ? '请求时间' : '操作时间'">
                <el-date-picker v-model="pickerVal" type="datetimerange" start-placeholder="开始时间"
                  end-placeholder="结束时间" value-format="timestamp" clearable :editable="false"
                  :default-time="['00:00:00', '23:59:59']" />
              </el-form-item>
            </el-col>
          </template>
          <template v-if="activeName == 1">
            <el-col :span="6">
              <el-form-item label="类型">
                <el-select v-model="listQuery.loginType" placeholder="请选择类型" clearable>
                  <el-option label="登录" :value="0"></el-option>
                  <el-option label="退出" :value="1"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="listQuery.loginMark" placeholder="请选择状态" clearable>
                  <el-option label="成功" :value="1"></el-option>
                  <el-option label="失败" :value="0"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </template>
          <template v-if="showAll && activeName == 1">
            <el-col :span="6">
              <el-form-item label="时间">
                <el-date-picker v-model="pickerVal" type="datetimerange" start-placeholder="开始时间"
                  end-placeholder="结束时间" value-format="timestamp" clearable :editable="false"
                  :default-time="['00:00:00', '23:59:59']" />
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
              <template v-if="activeName == 1">
                <el-button type="text" icon="el-icon-arrow-down" @click="showAll=true"
                  v-if="!showAll">展开</el-button>
                <el-button type="text" icon="el-icon-arrow-up" @click="showAll=false" v-else>
                  收起</el-button>
              </template>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <el-tabs v-model="activeName" type="border-card" class="logTabs"
          @tab-click="handleTabClick">
          <div class="JNPF-common-head">
            <div class="left-btn">
              <el-button type="danger" size="small" @click="handleDel" icon="el-icon-delete"
                :loading="btnLoading">删除
              </el-button>
              <el-link type="danger" :underline="false" @click="batchDel" style="margin-left:10px"
                :disabled="btnLoading">
                一键清空</el-link>
            </div>
            <div class="JNPF-common-head-right">
              <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
                <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                  @click="initData()" />
              </el-tooltip>
            </div>
          </div>
          <el-tab-pane label="登录日志" name="1">
            <JNPF-table v-loading="listLoading" :data="loginLogData" has-c
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
              <el-table-column prop="abstracts" label="说明" width="120" show-overflow-tooltip />
            </JNPF-table>
          </el-tab-pane>
          <el-tab-pane label="请求日志" name="5">
            <JNPF-table v-loading="listLoading" :data="requestLogData" has-c
              @selection-change="handleSelectionChange">
              <el-table-column prop="creatorTime" label="请求时间" :formatter="jnpf.tableDateFormat"
                width="140" />
              <el-table-column prop="userName" label="用户" />
              <el-table-column prop="ipAddress" label="IP地址" />
              <el-table-column prop="ipAddressName" label="地点" />
              <el-table-column prop="browser" label="浏览器" />
              <el-table-column prop="platForm" label="操作系统" />
              <el-table-column prop="requestMethod" label="请求方式" />
              <el-table-column prop="requestDuration" label="耗时(毫秒)" show-overflow-tooltip />
              <el-table-column prop="requestUrl" label="请求地址" width="200" show-overflow-tooltip />
              <el-table-column prop="json" label="操作" min-width="60">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="goDetail(scope.row,'5')">详情</el-button>
                </template>
              </el-table-column>
            </JNPF-table>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="3">
            <JNPF-table v-loading="listLoading" :data="operationLogData" has-c
              @selection-change="handleSelectionChange">
              <el-table-column prop="creatorTime" :formatter="jnpf.tableDateFormat" label="操作时间"
                width="140" />
              <el-table-column prop="userName" label="用户" />
              <el-table-column prop="ipAddress" label="IP地址" />
              <el-table-column prop="ipAddressName" label="地点" />
              <el-table-column prop="browser" label="浏览器" />
              <el-table-column prop="platForm" label="操作系统" />
              <el-table-column prop="moduleName" label="操作模块" />
              <el-table-column prop="requestMethod" label="请求方式" align="center" />
              <el-table-column prop="requestDuration" label="耗时(毫秒)" />
              <el-table-column prop="requestUrl" label="请求地址" width="200" show-overflow-tooltip />
              <el-table-column prop="json" label="操作" min-width="60">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="goDetail(scope.row,'3')">详情</el-button>
                </template>
              </el-table-column>
            </JNPF-table>
          </el-tab-pane>
          <el-tab-pane label="异常日志" name="4">
            <JNPF-table v-loading="listLoading" :data="errorLogData" has-c
              @selection-change="handleSelectionChange">
              <el-table-column prop="creatorTime" :formatter="jnpf.tableDateFormat" label="操作时间"
                width="140" />
              <el-table-column prop="userName" label="用户" />
              <el-table-column prop="ipAddress" label="IP地址" />
              <el-table-column prop="ipAddressName" label="地点" />
              <el-table-column prop="browser" label="浏览器" />
              <el-table-column prop="platForm" label="操作系统" />
              <el-table-column prop="requestMethod" label="请求方式" align="center" />
              <el-table-column prop="requestUrl" label="请求地址" show-overflow-tooltip width="200" />
              <el-table-column label="操作" width="50" fixed="right">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="goDetail(scope.row,'4')">详情</el-button>
                </template>
              </el-table-column>
            </JNPF-table>
          </el-tab-pane>
          <pagination :total="total" :page.sync="listQuery.currentPage"
            :limit.sync="listQuery.pageSize" @pagination="initData" />
        </el-tabs>
      </div>
    </div>
    <Form v-show="formVisible" ref="Form" @close="formVisible=false" />
  </div>
</template>
<script>
import { getLogList, delLog, batchDelLog } from '@/api/system/log'
import Form from './Form'
export default {
  name: 'system-log',
  components: { Form },
  data() {
    return {
      btnLoading: false,
      showAll: false,
      formVisible: false,
      listLoading: true,
      activeName: '1',
      loginLogData: [],
      errorLogData: [],
      requestLogData: [],
      operationLogData: [],
      multipleSelection: [],
      total: 0,
      listQuery: {
        keyword: '',
        startTime: '',
        loginType: '',
        loginMark: '',
        endTime: '',
        sort: 'desc',
        currentPage: 1,
        pageSize: 20,
        requestMethod: ''
      },
      pickerVal: []
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      const activeId = this.activeName
      this.listLoading = true
      this.listQuery.category = activeId
      getLogList(this.listQuery).then(res => {
        if (activeId === '1') this.loginLogData = res.data.list
        if (activeId === '3') this.operationLogData = res.data.list
        if (activeId === '4') this.errorLogData = res.data.list
        if (activeId === '5') this.requestLogData = res.data.list
        this.total = res.data.pagination.total
        this.listLoading = false
        this.btnLoading = false
      }).catch(() => {
        this.listLoading = false
        this.btnLoading = false
      })
      return
    },
    handleTabClick() {
      this.reset()
    },
    goDetail(data, type) {
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(data, type)
      })
    },
    handleSelectionChange(val) {
      const res = val.map(item => item.id)
      this.multipleSelection = res
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
    batchDel() {
      this.$confirm('此操作会将所有日志删除，是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        this.btnLoading = true
        batchDelLog(this.activeName).then(res => {
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
    search() {
      this.btnLoading = true
      this.listQuery.currentPage = 1
      if (this.pickerVal && this.pickerVal.length) {
        this.listQuery.startTime = this.pickerVal[0]
        this.listQuery.endTime = this.pickerVal[1]
      } else {
        this.listQuery.startTime = ''
        this.listQuery.endTime = ''
      }
      this.initData()
    },
    reset() {
      this.pickerVal = []
      this.listQuery.keyword = ''
      this.listQuery.loginMark = ''
      this.listQuery.loginType = ''
      this.listQuery.requestMethod = ''
      this.listQuery.pickerVal = []
      this.search()
    }
  }
}
</script>

<style lang="scss" scoped>
.systemLogs {
  .JNPF-common-layout-main {
    padding: 0;
  }
  .logTabs {
    height: 100%;
  }
  .line1 {
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    word-break: break-all;
  }
  >>> .el-tabs__content {
    height: calc(100% - 40px);
    padding: 0;
    .el-tab-pane {
      height: calc(100% - 110px);
      overflow: hidden;
    }
  }
}
</style>