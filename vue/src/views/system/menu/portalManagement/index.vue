<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-common-layout menu-list">
      <div class="JNPF-common-layout-center">
        <div class="JNPF-preview-main flow-form-main">
          <div class="JNPF-common-page-header">
            <el-page-header @back="goBack" :content="title" />
            <div class="options">
              <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
            </div>
          </div>
          <div class="JNPF-common-layout-main JNPF-flex-main">
            <el-row class="JNPF-common-search-box" :gutter="16">
              <el-form @submit.native.prevent>
                <el-col :span="6">
                  <el-form-item label="关键词">
                    <el-input v-model="keyword" placeholder="请输入关键词查询" clearable
                      @keyup.enter.native="initData()" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="分类">
                    <el-select v-model="category" placeholder="请选择分类" clearable>
                      <el-option v-for="item in categoryList" :key="item.id" :label="item.fullName"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="状态">
                    <el-select v-model="enabledMark" placeholder="请选择状态" clearable>
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
            <el-tabs type="border-card" v-model="categoryType" class="menu-tab"
              @tab-click="activeClick">
              <el-tab-pane label="Web门户" name="Web"></el-tab-pane>
              <el-tab-pane label="App门户" name="App"></el-tab-pane>
              <div class="JNPF-common-layout-main JNPF-flex-main">
                <div class="JNPF-common-head">
                  <topOpts @add="addOrUpdateHandle" addText='添加'>
                  </topOpts>
                  <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
                    <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                      @click="initData()" />
                  </el-tooltip>
                </div>
                <JNPF-table v-loading="listLoading" :data="list">
                  <el-table-column prop="fullName" label="门户名称" width="200" />
                  <el-table-column prop="categoryName" label="分类" width="150" />
                  <el-table-column prop="description" label="说明" min-width="200"
                    show-overflow-tooltip />
                  <el-table-column prop="creatorUser" label="创建人" width="120" />
                  <el-table-column prop="creatorTime" label="创建时间" width="140"
                    :formatter="jnpf.tableDateFormat" />
                  <el-table-column prop="lastModifyTime" label="最后修改时间" width="140"
                    :formatter="jnpf.tableDateFormat" />
                  <el-table-column prop="sortCode" label="排序" width="70" align="center" />
                  <el-table-column prop="enabledMark" label="状态" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'"
                        disable-transitions>
                        {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" fixed="right" width="150">
                    <template slot-scope="scope">
                      <tableOpts @edit="addOrUpdateHandle(scope.row.id)"
                        @del="handleDel(scope.row.id)">
                        <template>
                          <el-dropdown>
                            <span class="el-dropdown-link">
                              <el-button type="text" size="mini">{{ $t("common.moreBtn")
                            }}<i class="el-icon-arrow-down el-icon--right"></i>
                              </el-button>
                            </span>
                            <el-dropdown-menu slot="dropdown">
                              <el-dropdown-item @click.native="handleAuthorize(scope.row.id)">
                                授权
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                        </template>
                      </tableOpts>
                    </template>
                  </el-table-column>
                </JNPF-table>
                <pagination :total="total" :page.sync="listQuery.currentPage"
                  :limit.sync="listQuery.pageSize" @pagination="initData" />
              </div>
            </el-tabs>
          </div>
          <Form v-if="formVisible" ref="Form" @refreshDataList="refresh" />
          <Transfer ref="transfer" :visible.sync="transferShow" :id="transferId"
            :systemId='systemId' />
        </div>
      </div>
    </div>
  </transition>
</template>
<script>
import {
  getPortalManageList, delPortal, Release
} from "@/api/system/portal";
import Form from "./Form";
import Transfer from "./Transfer";
export default {
  name: "PortalManagement",
  components: {
    Form, Transfer
  },
  data() {
    return {
      title: "",
      keyword: "",
      listLoading: true,
      formVisible: false,
      systemId: '',
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: '',
      },
      categoryType: "Web",
      releaseQuery: {
        pc: 1,
        app: 1,
        systemId: '',
        toUserIds: ''
      },
      total: 0,
      list: [],
      portalId: '',
      releaseDialogVisible: false,
      currRow: {},
      toUserIds: [],
      releaseBtnLoading: false,
      transferShow: false,
      transferId: '',
      categoryList: [],
      category: "",
      enabledMark: '',
      options: [
        { fullName: '启用', id: 1 },
        { fullName: '禁用', id: 0 },
      ]
    }
  },
  created() {
    this.getDictionaryData()
  },
  methods: {
    goBack(isRefresh) {
      if (this.formChildVisible) {
        return this.formChildVisible = false
      }
      this.$emit('close', isRefresh)
    },
    init(data) {
      this.title = data.fullName + '的门户管理'
      this.systemId = data.id
      this.categoryType = 'Web'
      this.initData();
    },
    handleAuthorize(id) {
      this.transferId = id
      this.transferShow = true
    },
    openReleaseDialog(row) {
      this.currRow = row
      this.releaseDialogVisible = true
      this.releaseQuery = {
        pc: 1,
        app: 1,
        systemId: '',
        toUserIds: ''
      }
      this.$nextTick(() => {
        this.$refs['releaseForm'] && this.$refs['releaseForm'].resetFields()
      })
    },
    selectToggle(key) {
      this.releaseQuery[key] = this.releaseQuery[key] === 1 ? 0 : 1
    },
    getDictionaryData() {
      this.$store.dispatch('base/getDictionaryData', { sort: 'portalDesigner' }).then((res) => {
        this.categoryList = res
      })
    },
    // 发布菜单
    release() {
      if (!this.releaseQuery.pc && !this.releaseQuery.app) return this.$message.error('请至少选择一种同步方式')
      this.releaseBtnLoading = true
      this.releaseQuery.systemId = this.systemId
      this.releaseQuery.toUserIds = this.toUserIds.join(',')
      Release(this.currRow.id, this.releaseQuery).then(res => {
        this.releaseBtnLoading = false
        this.releaseDialogVisible = false
        this.initData()
        this.$message({
          type: 'success',
          message: res.msg,
          duration: 1000,
        });
      }).catch(() => { this.releaseBtnLoading = false })
    },
    refresh(isRefresh) {
      this.formVisible = false
      if (isRefresh) {
        this.initData()
      }
    },
    reset() {
      this.keyword = "";
      this.category = "";
      this.enabledMark = ""
      this.search();
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
    activeClick(tab, event) {
      this.categoryType = tab.name
      this.reset()
    },
    initData() {
      this.listLoading = true;
      let query = {
        ...this.listQuery,
        category: this.category,
        enabledMark: this.enabledMark,
        keyword: this.keyword,
        platform: this.categoryType
      }
      getPortalManageList(this.systemId, query).then((res) => {
        this.list = res.data.list;
        this.total = res.data.pagination.total
        this.listLoading = false;
        this.btnLoading = false;
      })
        .catch(() => {
          this.listLoading = false;
          this.btnLoading = false;
        });
    },
    addOrUpdateHandle(id) {
      this.formVisible = true;
      this.$nextTick(() => {
        this.$refs.Form.init(id, this.systemId, this.categoryList, this.categoryType);
      });
    },
    handleDel(id) {
      this.$confirm(this.$t("common.delTip"), this.$t("common.tipTitle"), {
        type: "warning",
      }).then(() => {
        delPortal(id).then((res) => {
          this.$message({
            type: "success",
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData();
            },
          });
        });
      }).catch(() => { });
    },
    exportMenu(id) {
      this.$confirm("您确定要导出该菜单, 是否继续?", "提示", {
        type: "warning",
      })
        .then(() => {
          exportMenu(id).then((res) => {
            this.jnpf.downloadFile(res.data.url);
          });
        })
        .catch(() => { });
    },
  },
};
</script>
<style lang="scss" scoped>
.menu-list {
  .table-icon {
    vertical-align: middle;
    font-size: 16px;
  }
  // .JNPF-common-layout-main {
  //   padding: 0 0 10px;
  // }
  .menu-tab {
    height: 100%;
    >>> .el-tabs__content {
      padding: 0;
      height: calc(100% - 33px);
      .box {
        flex: 1;
        height: 100%;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        padding-bottom: 10px;
      }
    }
  }
}

.flow-form-main {
  .el-form {
    padding-top: 0;
  }
  >>> .el-tabs__header {
    padding: 0;
  }
}
</style>
