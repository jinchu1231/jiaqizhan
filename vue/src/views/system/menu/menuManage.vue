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
          <el-row class="JNPF-common-search-box" :gutter="16">
            <el-form @submit.native.prevent>
              <el-col :span="6">
                <el-form-item label="关键词">
                  <el-input v-model="listQuery.keyword" placeholder="请输入关键词查询" clearable
                    @keyup.enter.native="initData()" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="类型">
                  <el-select v-model="listQuery.type" placeholder="请选择类型" clearable>
                    <el-option v-for="item in typeOptions" :key="item.id" :label="item.fullName"
                      :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="状态">
                  <el-select v-model="listQuery.enabledMark" placeholder="请选择状态" clearable>
                    <el-option v-for="item in options" :key="item.id" :label="item.fullName"
                      :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item>
                  <el-button type="primary" icon="el-icon-search" @click="initData()">
                    {{ $t("common.search") }}</el-button>
                  <el-button icon="el-icon-refresh-right" @click="reset()">{{ $t("common.reset") }}
                  </el-button>
                </el-form-item>
              </el-col>
            </el-form>
          </el-row>
          <div class="JNPF-common-layout-main JNPF-flex-main">
            <el-tabs type="border-card" v-model="listQuery.category" class="menu-tab">
              <el-tab-pane label="Web菜单" name="Web"></el-tab-pane>
              <el-tab-pane label="App菜单" name="App" v-if="!mainSystem"></el-tab-pane>
              <div class="box">
                <div class="JNPF-common-head">
                  <topOpts @add="addOrUpdateHandle()">
                    <upload-btn :url="'/api/system/Menu/'+systemId+'/Actions/Import'"
                      :data="{category: listQuery.category, systemId:systemId }"
                      @on-success="uploadSuccess" accept=".bm" icon="el-icon-upload2" type="menu" />
                  </topOpts>
                  <div class="JNPF-common-head-right">
                    <el-tooltip effect="dark" content="展开" placement="top">
                      <el-link v-show="!expands" type="text"
                        icon="icon-ym icon-ym-btn-expand JNPF-common-head-icon" :underline="false"
                        @click="toggleExpand()" />
                    </el-tooltip>
                    <el-tooltip effect="dark" content="折叠" placement="top">
                      <el-link v-show="expands" type="text"
                        icon="icon-ym icon-ym-btn-collapse JNPF-common-head-icon" :underline="false"
                        @click="toggleExpand()" />
                    </el-tooltip>
                    <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
                      <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon"
                        :underline="false" @click="initData()" />
                    </el-tooltip>
                  </div>
                </div>
                <JNPF-table v-loading="listLoading" :data="treeList" row-key="id"
                  v-if="refreshTable" :default-expand-all="expands"
                  :tree-props="{children: 'children', hasChildren: ''}">
                  <el-table-column prop="fullName" label="菜单名称" width="260" />
                  <el-table-column prop="enCode" label="编码" />
                  <el-table-column prop="urlAddress" label="菜单地址" show-overflow-tooltip />
                  <el-table-column prop="icon" label="图标" width="50" align="center">
                    <template slot-scope="scope">
                      <i :class="scope.row.icon + ' table-icon'" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="类型" width="70" align="center">
                    <template slot-scope="scope">
                      <span v-if="scope.row.type === 1">目录</span>
                      <span v-if="scope.row.type === 2">页面</span>
                      <span v-if="scope.row.type === 3">功能</span>
                      <span v-if="scope.row.type === 4">字典</span>
                      <span v-if="scope.row.type === 5">报表</span>
                      <span v-if="scope.row.type === 6">大屏</span>
                      <span v-if="scope.row.type === 7">外链</span>
                      <span v-if="scope.row.type === 8">门户</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="sortCode" label="排序" width="70" align="center" />
                  <el-table-column prop="enabledMark" label="状态" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'"
                        disable-transitions>{{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template slot-scope="scope">
                      <tableOpts @edit="addOrUpdateHandle(scope.row.id)"
                        @del="handleDel(scope.row.id)">
                        <template
                          v-if="(listQuery.category==='Web'&&scope.row.type) || (listQuery.category==='App'&&scope.row.type!=1)">
                          <el-dropdown>
                            <span class="el-dropdown-link">
                              <el-button type="text" size="mini">{{ $t("common.moreBtn")
                            }}<i class="el-icon-arrow-down el-icon--right"></i>
                              </el-button>
                            </span>
                            <el-dropdown-menu slot="dropdown">
                              <el-dropdown-item
                                v-if='listQuery.category==="Web" && scope.row.type==1'
                                @click.native="addOrUpdateHandle('',scope.row.id)">
                                新建子级
                              </el-dropdown-item>
                              <template v-if="[2, 3, 4].indexOf(scope.row.type) > -1">
                                <el-dropdown-item v-if="scope.row.isButtonAuthorize === 1"
                                  @click.native="handleButtonAuthorize(scope.row)">
                                  按钮权限
                                </el-dropdown-item>
                                <el-dropdown-item v-if="scope.row.isColumnAuthorize === 1"
                                  @click.native="handleColumnAuthorize(scope.row)">
                                  列表权限
                                </el-dropdown-item>
                                <el-dropdown-item v-if="scope.row.isFormAuthorize === 1"
                                  @click.native="handleFormAuthorize(scope.row)">
                                  表单权限
                                </el-dropdown-item>
                                <el-dropdown-item v-if="scope.row.isDataAuthorize === 1"
                                  @click.native="handleDataAuthorize(scope.row)">
                                  数据权限
                                </el-dropdown-item>
                                <el-dropdown-item v-if="![1,5,6,7,8].includes(scope.row.type)"
                                  @click.native="checkAuthorize(scope.row)">
                                  查看权限
                                </el-dropdown-item>
                              </template>
                              <el-dropdown-item @click.native="exportMenu(scope.row.id)"
                                v-if="scope.row.type!=1">
                                导出模板
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                        </template>
                      </tableOpts>
                    </template>
                  </el-table-column>
                </JNPF-table>
              </div>
            </el-tabs>
          </div>
        </div>
        <menuForm v-if="formVisible" ref="Form" @refreshDataList="initData" />
        <ButtonAuthorizeListDrawer v-if="buttonAuthorizeListDrawer" ref="buttonAuthorizeList" />
        <ColumnAuthorizeListDrawer v-if="columnAuthorizeListDrawer" ref="ColumnAuthorizeList" />
        <FormAuthorizeListDrawer v-if="formAuthorizeListDrawer" ref="FormAuthorizeList" />
        <DataAuthorizeListDrawer v-if="dataAuthorizeListDrawer" ref="DataAuthorizeList" />
        <CheckPermissions v-if="checkAuthorizeVisible" ref="checkPermissions"
          @close="checkAuthorizeVisible=false" />
      </div>
    </div>
  </transition>
</template>
<script>
import {
  getMenuList,
  updateMenuState,
  delMenu,
  exportMenu,
  getMenuSelector,
  getPermissionGroup,
  getPermission
} from "@/api/system/menu";
import menuForm from "./menuForm";
import ButtonAuthorizeListDrawer from "./components/buttonAuthorize/index";
import ColumnAuthorizeListDrawer from "./components/columnAuthorize/index";
import FormAuthorizeListDrawer from "./components/formAuthorize/index";
import DataAuthorizeListDrawer from "./components/dataAuthorize/index";
import CheckPermissions from '@/components/CheckMenuPermissions'
export default {
  name: "system-menu",
  components: {
    menuForm,
    ButtonAuthorizeListDrawer,
    ColumnAuthorizeListDrawer,
    FormAuthorizeListDrawer,
    DataAuthorizeListDrawer,
    CheckPermissions
  },
  data() {
    return {
      options: [],
      listQuery: {
        enabledMark: '',
        type: '',
        keyword: "",
        category: "Web",
      },
      treeList: [],
      btnLoading: false,
      listLoading: true,
      formVisible: false,
      buttonAuthorizeListDrawer: false,
      columnAuthorizeListDrawer: false,
      formAuthorizeListDrawer: false,
      dataAuthorizeListDrawer: false,
      expands: true,
      refreshTable: true,
      title: '',
      systemId: '',
      formRule: {
        parentId: [
          { required: true, message: '上级菜单不能为空', trigger: 'change' }
        ]
      },
      treeData: [],
      dataForm: {
        parentId: ""
      },
      menuBtnLoading: false,
      checkAuthorizeVisible: false,
      fullName: '',
      authorize: true,
      activeMenu: "1",
      list: [],
      activeKey: "",
      mainSystem: false,
      authorizeList: [],
      menuId: '',
      description: '',
      loading: false,
      options: [
        { fullName: '启用', id: 1 },
        { fullName: '禁用', id: 0 },
      ],
      typeOptions: [
        { fullName: '目录', id: 1 },
        { fullName: '页面', id: 2 },
        { fullName: '功能', id: 3 },
        { fullName: '字典', id: 4 },
        { fullName: '报表', id: 5 },
        { fullName: '大屏', id: 6 },
        { fullName: '外链', id: 7 },
        { fullName: '门户', id: 8 },
      ]
    }
  },
  watch: {
    "listQuery.category": function (val) {
      this.reset();
    },
  },
  methods: {
    goBack(isRefresh) {
      this.$emit('close', isRefresh)
    },
    init(data) {
      this.title = data.fullName + '的菜单管理'
      this.mainSystem = data.mainSystem
      this.systemId = data.id
      this.initData();
    },
    reset() {
      this.listQuery.keyword = "";
      this.listQuery.type = "";
      this.listQuery.enabledMark = "";
      this.initData();
    },
    initData() {
      this.listLoading = true;
      getMenuList(this.systemId, this.listQuery)
        .then((res) => {
          this.treeList = res.data.list;
          this.listLoading = false;
          this.btnLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
          this.btnLoading = false;
        });
    },
    toggleExpand() {
      this.refreshTable = false;
      this.expands = !this.expands;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    addOrUpdateHandle(id, parentId) {
      this.formVisible = true;
      this.$nextTick(() => {
        this.$refs.Form.init(id, this.listQuery.category, this.systemId, parentId);
      });
    },
    handleUpdateState(row) {
      const txt = row.enabledMark ? "禁用" : "开启";
      this.$confirm(`您确定要${txt}当前菜单吗, 是否继续?`, "提示", {
        type: "warning",
      })
        .then(() => {
          updateMenuState(row.id).then((res) => {
            this.$message({
              type: "success",
              message: res.msg,
              duration: 1000,
              onClose: () => {
                row.enabledMark = row.enabledMark ? 0 : 1;
              },
            });
          });
        })
        .catch(() => { });
    },
    handleDel(id) {
      this.$confirm(this.$t("common.delTip"), this.$t("common.tipTitle"), {
        type: "warning",
      })
        .then(() => {
          delMenu(id).then((res) => {
            this.$message({
              type: "success",
              message: res.msg,
              duration: 1500,
              onClose: () => {
                this.initData();
              },
            });
          });
        })
        .catch(() => { });
    },
    handleButtonAuthorize(row) {
      const moduleId = row.id;
      const fullName = row.fullName;
      this.buttonAuthorizeListDrawer = true;
      this.$nextTick(() => {
        this.$refs.buttonAuthorizeList.init(moduleId, fullName);
      });
    },
    //列表权限
    handleColumnAuthorize(row) {
      const moduleId = row.id;
      const fullName = row.fullName;
      const type = row.type;
      this.columnAuthorizeListDrawer = true;
      this.$nextTick(() => {
        this.$refs.ColumnAuthorizeList.init(moduleId, fullName, type, 1);
      });
    },
    //表单权限
    handleFormAuthorize(row) {
      const moduleId = row.id;
      const fullName = row.fullName;
      const type = row.type;
      this.formAuthorizeListDrawer = true;
      this.$nextTick(() => {
        this.$refs.FormAuthorizeList.init(moduleId, fullName, type, 2);
      });
    },
    //数据权限
    handleDataAuthorize(row) {
      const moduleId = row.id;
      const fullName = row.fullName;
      const type = row.type;
      this.dataAuthorizeListDrawer = true;
      this.$nextTick(() => {
        this.$refs.DataAuthorizeList.init(moduleId, fullName, type, 3);
      });
    },
    //查看权限
    checkAuthorize(row) {
      this.checkAuthorizeVisible = true;
      this.$nextTick(() => {
        this.$refs.checkPermissions.init(row, 1)
      })
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
    uploadSuccess() {
      this.selectMenuVisible = false
      this.initData()
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
  .JNPF-common-layout-main {
    padding: 0;
  }
  .menu-tab {
    height: 100%;
    >>> .el-tabs__content {
      padding: 0;
      height: calc(100% - 40px);
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

.tabs-box {
  >>> .el-tabs__nav:first-child {
    margin-left: 20px !important;
  }
}
.title {
  font-weight: bold;
  line-height: 36px;
  font-size: 14px;
}

.px-20px {
  padding: 0 20px;
}
.content {
  min-height: 20px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  span {
    position: relative;
    padding-right: 20px;
  }
  span:not(:last-child) {
    &::after {
      content: '';
      position: absolute;
      top: 1px;
      right: 10px;
      width: 1px;
      height: 15px;
      background: #606266;
      z-index: 1;
    }
  }
}
.no-data {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

