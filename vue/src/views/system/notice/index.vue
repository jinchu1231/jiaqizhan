<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="listQuery.keyword" placeholder="请输入关键词查询" clearable
                @keyup.enter.native="search()" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="类型">
              <el-select v-model="noticeSource" multiple placeholder="请选择类型" clearable filterable>
                <el-option v-for="(item,index) in noticeSourceList" :key="index"
                  :label="item.fullName" :value="item.enCode">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="noticeStatus" multiple placeholder="请选择状态" clearable>
                <el-option v-for="(item,index) in noticeStatusList" :key="index"
                  :label="item.fullName" :value="item.enCode">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <template v-if="showAll">
            <el-col :span="6">
              <el-form-item label="发布人">
                <JnpfUserSelect v-model="releaseUser" placeholder='请选择发布人' clearable multiple />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="发布时间">
                <el-date-picker v-model="releaseTime" start-placeholder="开始时间"
                  end-placeholder="结束时间" clearable value-format="timestamp" type="datetimerange"
                  :default-time="['00:00:00', '23:59:59']" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="失效时间">
                <el-date-picker v-model="expirationTime" start-placeholder="开始时间"
                  end-placeholder="结束时间" clearable value-format="timestamp" type="datetimerange"
                  :default-time="['00:00:00', '23:59:59']" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="创建人">
                <JnpfUserSelect v-model="creatorUser" placeholder='请选择创建人' clearable multiple />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="创建时间">
                <el-date-picker v-model="creatorTime" start-placeholder="开始时间"
                  end-placeholder="结束时间" clearable value-format="timestamp" type="datetimerange"
                  :default-time="['00:00:00', '23:59:59']" />
              </el-form-item>
            </el-col>
          </template>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="search()">
                {{$t('common.search')}}</el-button>
              <el-button icon="el-icon-refresh-right" @click="reset()">{{$t('common.reset')}}
              </el-button>
              <el-button type="text" icon="el-icon-arrow-down" @click="showAll=true"
                v-if="!showAll">展开</el-button>
              <el-button type="text" icon="el-icon-arrow-up" @click="showAll=false" v-else>
                收起</el-button>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <div class="JNPF-common-head">
          <topOpts @add="addOrUpdateHandle()"></topOpts>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list" max-height="100%">
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" show-overflow-tooltip width="100">
            <template slot-scope="scope">
              <div>
                {{scope.row.category}}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="expirationTime" label="失效时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="creatorUser" label="创建人" width="120" />
          <el-table-column prop="creatorTime" label="创建时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="releaseUser" label="发布人" width="120" />
          <el-table-column prop="releaseTime" label="发布时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="enabledMark" label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag
                :type="scope.row.enabledMark==1?'success':scope.row.enabledMark==0?'warning':'info'"
                disable-transitions>
                {{ scope.row.enabledMark==1?'已发送':scope.row.enabledMark==0?'存草稿':'已过期' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)"
                :editDisabled="scope.row.enabledMark != 0">
                <el-dropdown>
                  <el-button type="text" size="mini">
                    {{$t('common.moreBtn')}}<i class="el-icon-arrow-down el-icon--right" />
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="handleView(scope.row.id)">
                      详情</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.enabledMark == 0"
                      @click.native="handlePublish(scope.row.id)">发布
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </tableOpts>
            </template>
          </el-table-column>
        </JNPF-table>
        <pagination :total="total" :page.sync="listQuery.currentPage"
          :limit.sync="listQuery.pageSize" @pagination="initData" />
      </div>
    </div>
    <Form v-show="formVisible" ref="Form" @close="formVisible=false" @refreshDataList="initData" />
    <ViewNotice v-if="viewVisible" ref="View" @refreshDataList="initData" />
  </div>
</template>
<script>
import { getNoticeList, delNotice, releaseNotice } from '@/api/system/message'
import Form from './Form'
import ViewNotice from './View'
export default {
  name: 'system-notice',
  components: { Form, ViewNotice },
  data() {
    return {
      showAll: false,
      list: [],
      total: 0,
      btnLoading: false,
      listLoading: true,
      listQuery: {
        keyword: '',
        currentPage: 1,
        pageSize: 20
      },
      noticeSourceList: [],
      noticeStatusList: [
        { fullName: "存草稿", enCode: '0' },
        { fullName: "已发送", enCode: '1' },
        { fullName: "已过期", enCode: '2' },
      ],
      noticeStatus: [],
      noticeSource: [],
      expirationTime: [],
      releaseTime: [],
      releaseUser: [],
      creatorUser: [],
      creatorTime: [],
      formVisible: false,
      viewVisible: false
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      // 获取公告类型
      this.$store.dispatch('base/getDictionaryData', { sort: 'NoticeType' }).then(res => {
        this.noticeSourceList = res
      })
      this.listLoading = true;
      this.listQuery.enabledMark = this.noticeStatus;
      this.listQuery.type = this.noticeSource;
      this.listQuery.expirationTime = this.expirationTime;
      this.listQuery.releaseTime = this.releaseTime;
      this.listQuery.releaseUser = this.releaseUser;
      this.listQuery.creatorUser = this.creatorUser;
      this.listQuery.creatorTime = this.creatorTime;
      getNoticeList(this.listQuery).then(res => {
        this.list = res.data.list
        this.total = res.data.pagination.total
        this.listLoading = false
        this.btnLoading = false
      }).catch(() => {
        this.listLoading = false
        this.btnLoading = false
      })
    },
    addOrUpdateHandle(id) {
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(id)
      })
    },
    handleView(id) {
      this.viewVisible = true
      this.$nextTick(() => {
        this.$refs.View.init(id)
      })
    },
    handleDel(id) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        delNotice(id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData()
            }
          })
        })
      }).catch(() => { })
    },
    handlePublish(id) {
      this.$confirm('您确定要发布当前公告, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        releaseNotice(id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData()
            }
          })
        })
      }).catch(() => { })
    },
    search() {
      this.listQuery = {
        ...this.listQuery,
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        enabledMark: this.noticeStatus,
        type: this.noticeSource,
        expirationTime: this.expirationTime,
        releaseTime: this.releaseTime,
        releaseUser: this.releaseUser,
        creatorUser: this.creatorUser,
        creatorTime: this.creatorTime
      }
      this.initData()
    },
    reset() {
      this.noticeSource = [];
      this.noticeStatus = [];
      this.listQuery.keyword = '';
      this.listQuery.enabledMark = [];
      this.listQuery.type = [];
      this.expirationTime = [];
      this.releaseTime = [];
      this.releaseUser = [];
      this.creatorUser = [];
      this.creatorTime = [];
      this.search()
    }
  }
}
</script>