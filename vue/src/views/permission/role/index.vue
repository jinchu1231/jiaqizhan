<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-left">
      <div class="JNPF-common-title">
        <h2>{{$t('common.organization')}}</h2>
        <span class="options">
          <el-dropdown>
            <el-link icon="icon-ym icon-ym-mpMenu" :underline="false" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="getOrganizeList()">刷新数据</el-dropdown-item>
              <el-dropdown-item @click.native="toggleExpand(true)">展开全部</el-dropdown-item>
              <el-dropdown-item @click.native="toggleExpand(false)">折叠全部</el-dropdown-item>
              <el-dropdown-item @click.native="showDiagram">架构图</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </div>
      <div class="JNPF-common-tree-search-box">
        <el-input placeholder="输入关键字" v-model="filterText" suffix-icon="el-icon-search" clearable />
      </div>
      <el-scrollbar class="JNPF-common-el-tree-scrollbar" v-loading="treeLoading">
        <el-tree ref="treeBox" :data="treeData" :props="defaultProps" :default-expand-all="expands"
          highlight-current :expand-on-click-node="false" node-key="id"
          @node-click="handleNodeClick" class="JNPF-common-el-tree" v-if="refreshTree"
          :filter-node-method="filterNode">
          <span class=" custom-tree-node" slot-scope="{ data }" :title="data.fullName">
            <i :class="data.icon" />
            <span class="text" :title="data.fullName">{{data.fullName}}</span>
          </span>
        </el-tree>
      </el-scrollbar>
    </div>
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item :label="$t('common.keyword')">
              <el-input v-model="listQuery.keyword" :placeholder="$t('common.enterKeyword')"
                clearable @keyup.enter.native="search()" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="角色类型">
              <el-select v-model="listQuery.type" placeholder="请选择角色类型">
                <el-option label="全局" :value="1"></el-option>
                <el-option label="组织" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="listQuery.enabledMark" placeholder="请选择状态" clearable filterable>
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
          <topOpts @add="addOrUpdateHandle()"></topOpts>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="fullName" label="角色名称" width="200" />
          <el-table-column prop="enCode" label="角色编码" width="150" />
          <el-table-column prop="type" label="角色类型" width="70" align="center" />
          <el-table-column prop="organizeInfo" label="所属组织" min-width="150" show-overflow-tooltip />
          <el-table-column prop="creatorTime" label="创建时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="sortCode" label="排序" width="70" align="center" />
          <el-table-column prop="enabledMark" label="状态" width="70" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'" disable-transitions>
                {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)">
                <el-dropdown hide-on-click>
                  <span class="el-dropdown-link">
                    <el-button type="text" size="mini">
                      {{$t('common.moreBtn')}}<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="handleUserRelation(scope.row)">
                      {{$t('role.roleMember')}}
                    </el-dropdown-item>
                    <el-dropdown-item @click.native="checkPermissions(scope.row)">
                      查看权限</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </tableOpts>
            </template>
          </el-table-column>
        </JNPF-table>
        <pagination :total="total" :page.sync="listQuery.currentPage"
          :limit.sync="listQuery.pageSize" @pagination="initData" />
      </div>
      <Form v-if="formVisible" ref="Form" @refreshDataList="initData" />
      <Diagram v-if="diagramVisible" ref="Diagram" @close="diagramVisible = false" />
      <component :is="currentView" v-if="userRelationListVisible" ref="UserRelationList"
        @refreshDataList="initData" @closeDialog="userRelationListVisible=false" />
      <CheckPermissions v-if="checkPermissionsVisible" ref="checkPermissions"
        @close="checkPermissionsVisible=false" />
    </div>
  </div>
</template>

<script>
import { getDepartmentSelectorByAuth } from '@/api/permission/department'
import { getRoleList, delRole, updateRoleState } from '@/api/permission/role'
import Form from './Form'
import UserRelationList from './userRelation'
import GlobalUserRelationList from '@/views/permission/userRelation/Selector'
import Diagram from '@/views/permission/user/Diagram'
import CheckPermissions from '@/components/CheckPermissions'
import { mapGetters } from "vuex";
export default {
  components: { Form, UserRelationList, GlobalUserRelationList, Diagram, CheckPermissions },
  name: 'permission-role',
  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'fullName'
      },
      list: [],
      listQuery: {
        organizeId: '',
        keyword: '',
        type: '',
        enabledMark: '',
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      },
      total: 0,
      listLoading: true,
      treeLoading: false,
      formVisible: false,
      diagramVisible: false,
      userRelationListVisible: false,
      currentView: null,
      expands: true,
      refreshTree: true,
      filterText: '',
      checkPermissionsVisible: false,
      options: [
        { fullName: '启用', id: 1 },
        { fullName: '禁用', id: 0 },
      ]

    }
  },
  watch: {
    filterText(val) {
      this.$refs.treeBox.filter(val)
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  created() {
    this.getOrganizeList(true)
  },
  methods: {
    reset() {
      this.listQuery.keyword = ''
      this.listQuery.type = ''
      this.listQuery.enabledMark = ''
      this.search()
    },
    search() {
      this.listQuery = {
        ...this.listQuery,
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      }
      this.initData()
    },
    showDiagram() {
      this.diagramVisible = true
      this.$nextTick(() => {
        this.$refs.Diagram.init()
      })
    },
    checkPermissions(item) {
      this.checkPermissionsVisible = true
      this.$nextTick(() => {
        item = { ...item, type: 'role' }
        this.$refs.checkPermissions.init(item, 2)
      })
    },
    toggleExpand(expands) {
      this.filterText = ''
      this.refreshTree = false
      this.expands = expands
      this.$nextTick(() => {
        this.refreshTree = true
        this.$nextTick(() => {
          this.$refs.treeBox.setCurrentKey(this.companyId)
        })
      })
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.fullName.indexOf(value) !== -1;
    },
    getOrganizeList(isInit) {
      this.filterText = ''
      this.treeLoading = true
      getDepartmentSelectorByAuth().then(res => {
        if (this.userInfo.isAdministrator) {
          let globalItem = {
            fullName: "全局",
            hasChildren: false,
            id: "0",
            parentId: "-1",
            icon: 'icon-ym icon-ym-global-role'
          }
          this.treeData = [...res.data.list, globalItem]
        } else {
          this.treeData = res.data.list || []
        }
        this.$nextTick(() => {
          this.treeLoading = false
          if (isInit) this.initData()
        })
      }).catch(() => {
        this.treeLoading = false
      })
    },
    initData() {
      this.listLoading = true
      getRoleList(this.listQuery).then(res => {
        this.list = res.data.list
        this.total = res.data.pagination.total
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    addOrUpdateHandle(id) {
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(id)
      })
    },
    handleNodeClick(data) {
      if (this.listQuery.organizeId === data.id) return
      this.listQuery.organizeId = data.id
      this.reset()
    },
    removeUserRelationList(isRefresh) {
      this.userRelationListVisible = false
      if (isRefresh) {
        this.initData()
      }
    },
    handleUpdateState(row) {
      const txt = row.enabledMark ? '禁用' : '开启'
      this.$confirm(`您确定要${txt}当前角色吗, 是否继续?`, '提示', {
        type: 'warning'
      }).then(() => {
        updateRoleState(row.id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1000,
            onClose: () => {
              row.enabledMark = row.enabledMark ? 0 : 1
            }
          })
        })
      }).catch(() => { })
    },
    handleDel(id) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        delRole(id).then(res => {
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
    handleUserRelation(row) {
      this.currentView = row.type === '全局' ? GlobalUserRelationList : UserRelationList
      this.$nextTick(() => {
        this.userRelationListVisible = true
        this.$nextTick(() => {
          this.$refs.UserRelationList.init(row.id, row.fullName, 'Role')
        })
      })
    },
  }
}
</script>