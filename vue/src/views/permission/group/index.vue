<template>
  <div class="JNPF-common-layout JNPF-flex-main">
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item :label="$t('common.keyword')">
              <el-input v-model="keyword" :placeholder="$t('common.enterKeyword')" clearable
                @keyup.enter.native="search()" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="类型">
              <el-select v-model="type" placeholder="请选择类型" clearable filterable>
                <el-option v-for="item in typeOptions" :key="item.id" :label="item.fullName"
                  :value="item.id">
                </el-option>
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
          <topOpts @add="addOrUpdateHandle()"></topOpts>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="fullName" label="分组名称" width="200" />
          <el-table-column prop="enCode" label="分组编码" width="150" />
          <el-table-column prop="type" label="分组类型" width="150" />
          <el-table-column prop="description" label="说明" min-width="100" show-overflow-tooltip />
          <el-table-column prop="creatorTime" label="创建时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="enabledMark" label="状态" width="70" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'" disable-transitions>
                {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sortCode" label="排序" width="70" align="center" />
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)">
                <el-dropdown hide-on-click>
                  <span class="el-dropdown-link">
                    <el-button type="text" size="mini">
                      {{$t('common.moreBtn')}}<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item
                      @click.native="handleUserRelation(scope.row.id, scope.row.fullName)">
                      组内成员
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
      <UserRelationList v-if="userRelationListVisible" ref="UserRelationList"
        @refreshDataList="initData" />
      <CheckPermissions v-if="checkPermissionsVisible" ref="checkPermissions"
        @close="checkPermissionsVisible=false" />
    </div>
  </div>
</template>

<script>
import { getGroupList, delGroup } from '@/api/permission/group'
import Form from './Form'
import UserRelationList from '@/views/permission/userRelation/Selector'
import CheckPermissions from '@/components/CheckPermissions'
export default {
  components: {
    Form,
    UserRelationList,
    CheckPermissions
  },
  name: 'permission-group',
  data() {
    return {
      list: [],
      categoryList: [],
      keyword: '',
      type: '',
      enabledMark: '',
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      },
      total: 0,
      listLoading: true,
      formVisible: false,
      userRelationListVisible: false,
      checkPermissionsVisible: false,
      options: [
        { fullName: '启用', id: 1 },
        { fullName: '禁用', id: 0 },
      ],
      typeOptions: []
    }
  },
  created() {
    this.initData()
  },
  methods: {
    checkPermissions(item) {
      this.checkPermissionsVisible = true
      this.$nextTick(() => {
        item = { ...item, type: 'group' }
        this.$refs.checkPermissions.init(item, 2)
      })
    },
    reset() {
      this.keyword = '',
        this.type = '',
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
    initData() {
      this.listLoading = true
      let query = {
        ...this.listQuery,
        keyword: this.keyword,
        type: this.type,
        enabledMark: this.enabledMark,
      }
      this.$store.dispatch('base/getDictionaryData', { sort: 'groupType' }).then(res => {
        this.typeOptions = res
      })
      getGroupList(query).then(res => {
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
    removeUserRelationList(isRefresh) {
      this.userRelationListVisible = false
      if (isRefresh) {
        this.initData()
      }
    },
    handleDel(id) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        delGroup(id).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.$store.commit('generator/SET_GROUP_TREE', [])
              this.initData()
            }
          })
        })
      }).catch(() => { })
    },
    handleUserRelation(id, fullName) {
      this.userRelationListVisible = true
      this.$nextTick(() => {
        this.$refs.UserRelationList.init(id, fullName, 'Group')
      })
    }
  }
}
</script>
