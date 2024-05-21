<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-center">
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
              <el-select v-model="type" placeholder="请选择类型" clearable>
                <el-option label="事件触发" :value="1" />
                <el-option label="定时触发" :value="2" />
                <el-option label="webhook触发" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="enabledMark" placeholder="请选择状态" clearable filterable>
                <el-option v-for="item in options" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
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
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <div class="JNPF-common-head">
          <div>
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-button type="primary" icon="el-icon-plus">
                  新建<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="1">事件触发</el-dropdown-item>
                <el-dropdown-item :command="2">定时触发</el-dropdown-item>
                <el-dropdown-item :command="3">webhook触发</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <upload-btn url="/api/visualdev/Integrate/Actions/Import" accept=".bi"
              @on-success="initData" />
          </div>
          <div class="JNPF-common-head-right">
            <el-tooltip content="执行队列" placement="top">
              <el-link icon="icon-ym icon-ym-generator-slider" :underline="false"
                @click="openDrawer()" />
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="fullName" label="名称" show-overflow-tooltip min-width="200" />
          <el-table-column prop="enCode" label="编码" width="180" />
          <el-table-column prop="type" label="触发类型" width="100" align="center">
            <template slot-scope="scope">
              {{scope.row.type==1?'事件触发':scope.row.type==2?'定时触发':'webhook触发'}}
            </template>
          </el-table-column>
          <el-table-column prop="creatorUser" label="创建人" width="120" />
          <el-table-column prop="creatorTime" label="创建时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="lastModifyTime" label="最后修改时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="enabledMark" label="状态" width="70" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'" disable-transitions>
                {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="150">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)">
                <el-dropdown>
                  <span class="el-dropdown-link">
                    <el-button type="text" size="mini">{{$t('common.moreBtn')}}<i
                        class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="handleDesign(scope.row.id,scope.row.fullName)">
                      设计
                    </el-dropdown-item>
                    <el-dropdown-item @click.native="handleLog(scope.row)">日志
                    </el-dropdown-item>
                    <el-dropdown-item
                      @click.native="handleState(scope.row)">{{scope.row.enabledMark!= 1?'启用':'禁用'}}
                    </el-dropdown-item>
                    <el-dropdown-item @click.native="handleCopy(scope.row.id)">复制
                    </el-dropdown-item>
                    <el-dropdown-item @click.native="exportTemplate(scope.row.id)">导出
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
    <Form v-if="formVisible" ref="form" @close="closeForm" @integrateDesign='updateNodes' />
    <Drawer v-if="drawerVisible" ref="drawer" @close="closeForm" />
    <Design v-if="processVisible" ref="processDesign" @close="closeForm1" />
    <Log v-if="logVisible" ref="log" @close="logVisible=false" @updateNodes='updateNodes' />
  </div>
</template>

<script>
import { getIntegrateList, delIntegrate, updateState, copy, exportData } from '@/api/onlineDev/integrate'
import Form from './Form'
import Drawer from './Drawer'
import Design from "./design"
import Log from "./log"
export default {
  name: 'onlineDev-visualPortal',
  components: { Form, Drawer, Design, Log },
  data() {
    return {
      list: [],
      keyword: '',
      category: '',
      type: '',
      enabledLock: '',
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      },
      total: 0,
      drawerVisible: false,
      listLoading: false,
      formVisible: false,
      flowTemplateJson: {},
      processVisible: false,
      logVisible: false,
      enabledMark: '',
      options: [
        { fullName: '启用', id: 1 },
        { fullName: '禁用', id: 0 },
      ],
    }
  },
  created() {
    this.initData()
  },
  methods: {
    inherit(row) {
      this.processVisible = true
      this.$nextTick(() => {
        this.$refs.processDesign && this.$refs.processDesign.init(row)
      })
    },
    reset() {
      this.keyword = ''
      this.category = ''
      this.type = ''
      this.enabledLock = ''
      this.enabledMark = '',
        this.search()
    },
    search() {
      this.listQuery = {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      }
      this.initData()
    },
    handleDesign(id, fullName) {
      this.processVisible = true
      this.$nextTick(() => {
        this.$refs.processDesign.init({ id: id, fullName: fullName })
      })
    },
    updateNodes(id, fullName) {
      this.handleDesign(id, fullName)
    },
    initData() {
      this.listLoading = true
      let query = {
        ...this.listQuery,
        keyword: this.keyword,
        type: this.type,
        category: this.category,
        enabledLock: this.enabledLock,
        enabledMark: this.enabledMark
      }
      getIntegrateList(query).then(res => {
        this.list = res.data.list
        this.total = res.data.pagination.total
        this.listLoading = false
      })
    },
    handleDel(id) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        delIntegrate(id).then(res => {
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
    openDrawer() {
      this.drawerVisible = true
      this.$nextTick(() => {
        this.$refs.drawer.open()
      })
    },
    handleState(row) {
      this.$confirm(row.enabledMark != 1 ? '此操作将启用该集成助手' : '此操作将禁用该集成助手', this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        updateState(row.id).then(res => {
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
    handleCopy(id) {
      this.$confirm('您确定要复制该集成助手, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        copy(id).then(res => {
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
    exportTemplate(id) {
      this.$confirm('您确定要导出该集成助手, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        exportData(id).then(res => {
          this.jnpf.downloadFile(res.data.url)
        })
      }).catch(() => { });
    },
    handleLog(row) {
      this.logVisible = true
      this.$nextTick(() => {
        this.$refs.log.init(row)
      })
    },
    design(row) {
      this.portalDesignVisible = true
      this.$nextTick(() => {
        this.$refs.drawer.init(row)
      })
    },
    handleCommand(command) {
      this.addOrUpdateHandle('', command)
    },
    addOrUpdateHandle(id, type) {
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.form.init(id, type)
      })
    },
    closeForm(isRefresh) {
      this.formVisible = false
      if (isRefresh) this.initData()
    },
    closeForm1(isRefresh) {
      this.processVisible = false
      if (isRefresh) this.initData()
    }
  }
}
</script>