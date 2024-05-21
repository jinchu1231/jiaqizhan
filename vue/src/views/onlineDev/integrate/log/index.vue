<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack()" :content="title" />
        <div class="options">
          <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <div class="main">
        <el-row class="JNPF-common-search-box" :gutter="16">
          <el-form @submit.native.prevent>
            <el-col :span="6">
              <el-form-item label="执行时间">
                <el-date-picker v-model="pickerVal" type="datetimerange" start-placeholder="开始日期"
                  end-placeholder="结束日期" :picker-options="pickerOptions" value-format="timestamp"
                  clearable :editable="false" :default-time="['00:00:00', '23:59:59']" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="执行结果">
                <el-select v-model="listQuery.resultType" placeholder="请选择执行结果" clearable>
                  <el-option label="成功" :value="1" />
                  <el-option label="失败" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="search()">
                  {{$t('common.search')}}</el-button>
                <el-button icon="el-icon-refresh-right" @click="reset()">{{$t('common.reset')}}
                </el-button>
              </el-form-item>
            </el-col>
          </el-form>
        </el-row>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="processId" label="实例ID" min-width="300" show-overflow-tooltip>
            <template slot-scope="scope">
              <template v-if="scope.row.isRetry">
                <span>{{ scope.row.processId   }}</span><span>{{'&nbsp;'}}</span><el-tag
                  type="primary" disable-transitions>
                  重试</el-tag><span>{{'&nbsp;'}}</span><span>
                  <el-tooltip class="item" effect="dark" placement="top">
                    <div slot="content">
                      原实例ID：{{ scope.row.parentId }}<br />原实例执行时间：{{ scope.row.parentTime|toDate('yyyy-MM-dd HH:mm:ss')}}
                    </div>
                    <i class="icon-ym icon-ym-generator-link cursor-pointer" />
                  </el-tooltip>
                </span>
              </template>
              <template v-else>{{ scope.row.processId }}</template>
            </template>
          </el-table-column>
          <el-table-column prop="resultType" label="执行结果" width="150">
            <template slot-scope="scope">
              <el-tag :type="scope.row.resultType == 1 ? 'success':'danger'" disable-transitions>
                {{scope.row.resultType==1?'成功':'失败'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="executionTime" label="执行时间 " :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column label="操作" fixed="right" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="Detail(scope.row.id)">
                详情</el-button>
              <el-button size="mini" type="text" class="JNPF-table-delBtn"
                @click="handleDetail(scope.row.id)">
                删除</el-button>
              <el-button size="mini" type="text" v-if="scope.row.resultType!=1"
                @click="handleRedo(scope.row.id)">
                重试</el-button>
            </template>
          </el-table-column>
        </JNPF-table>
        <pagination :total="total" :page.sync="listQuery.currentPage"
          :limit.sync="listQuery.pageSize" @pagination="initData" />
      </div>
      <Detail v-if="detailVisible" ref="Detail" @updateNodes='updateNodes'></Detail>
    </div>
  </transition>
</template>

<script>
import { getTaskList, retryTask, delTask } from '@/api/onlineDev/integrate'

import Detail from './Detail'
export default {
  components: { Detail },
  data() {
    return {
      integrateId: '',
      title: '',
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        resultType: '',
        startTime: '',
        endTime: '',
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
      },
      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const start = new Date()
            const end = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const start = new Date()
            const end = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近三个月',
          onClick(picker) {
            const start = new Date()
            const end = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            picker.$emit('pick', [start, end])
          }
        }]
      },
      pickerVal: [],
      detailVisible: false
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    Detail(id) {
      this.detailVisible = true
      this.$nextTick(() => {
        this.$refs.Detail.openDialog(id)
      })
    },
    updateNodes() {
      this.$emit('updateNodes', this.integrateId);
      this.goBack()
    },
    handleDetail(id) {
      this.$confirm('确定将本实例进行删除?', this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        delTask(id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1000,
            onClose: () => {
              this.initData()
            }
          });
        })
      }).catch(() => { });
    },
    handleRedo(id) {
      this.$confirm('确定将本实例进行重试?', this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        retryTask(id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1000,
            onClose: () => {
              this.initData()
            }
          });
        })
      }).catch(() => { });
    },
    init(data) {
      this.integrateId = data.id
      this.title = data.fullName
      this.initData()
    },
    initData() {
      this.listLoading = true
      let query = {
        ...this.listQuery,
        integrateId: this.integrateId
      }
      getTaskList(query).then(res => {
        this.list = res.data.list
        this.total = res.data.pagination.total
      })
      this.listLoading = false
    },
    search() {
      if (this.pickerVal && this.pickerVal.length) {
        this.listQuery.startTime = this.pickerVal[0]
        this.listQuery.endTime = this.pickerVal[1]
      } else {
        this.listQuery.startTime = ''
        this.listQuery.endTime = ''
      }
      this.listQuery.currentPage = 1
      this.listQuery.pageSize = 20
      this.listQuery.sort = 'desc'
      this.initData()
    },
    reset() {
      this.pickerVal = []
      this.listQuery.resultType = ''
      this.search()
    },
  }
}
</script>
<style lang="scss" scoped>
.main {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 0 10px;
  >>> .el-table {
    flex: 1;
    border-top: none;
  }
}
.cursor-pointer {
  cursor: pointer;
}
</style>