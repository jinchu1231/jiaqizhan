<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="listQuery.keyword" placeholder="请输入关键词查询" clearable
                @keyup.enter.native="initData()" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="listQuery.enabledMark" placeholder="请选择状态">
                <el-option label="启用" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="initData()">
                {{$t('common.search')}}</el-button>
              <el-button icon="el-icon-refresh-right" @click="reset()">{{$t('common.reset')}}
              </el-button>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <div class="JNPF-common-head">
          <topOpts @add="addOrUpdateHandle()" />
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="fullName" label="应用名称" min-width="200" />
          <el-table-column prop="enCode" label="应用编码" min-width="200" />
          <el-table-column prop="icon" label="图标" width="50" align="center">
            <template slot-scope="scope">
              <i :class="scope.row.icon + ' table-icon'" />
            </template>
          </el-table-column>
          <el-table-column prop="sortCode" label="排序" width="70" align="center" />
          <el-table-column prop="enabledMark" label="状态" width="70" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'" disable-transitions>
                {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="150">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)"
                :delDisabled="scope.row.isMain==1">
                <el-dropdown>
                  <el-button type="text" size="mini">
                    {{$t('common.moreBtn')}}<i class="el-icon-arrow-down el-icon--right" />
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="preview(scope.row)">
                      菜单管理</el-dropdown-item>
                    <el-dropdown-item v-if="!scope.row.mainSystem"
                      @click.native="portalManagement(scope.row)">
                      门户管理</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </tableOpts>
            </template>
          </el-table-column>
        </JNPF-table>
      </div>
    </div>
    <Form v-if="formVisible" ref="Form" @refreshDataList="initData" />
    <menuManage v-if="previewVisible" ref="menuManage" @close="closeForm" />
    <PortalManagement v-if="portalVisible" ref="portalManage" @close="closeForm" />
  </div>
</template>
<script>
import Form from "./Form";
import MenuManage from "./menuManage";
import PortalManagement from './portalManagement'
import { getSystem, delSystem } from "@/api/system/system";
export default {
  name: "system-menu",
  components: {
    Form,
    MenuManage,
    PortalManagement
  },
  data() {
    return {
      options: [],
      listQuery: {
        keyword: "",
        enabledMark: ''
      },
      list: [],
      btnLoading: false,
      listLoading: false,
      formVisible: false,
      expands: true,
      refreshTable: true,
      previewVisible: false,
      total: 0,
      portalVisible: false
    };
  },
  created() {
    this.initData();
  },
  methods: {
    portalManagement(row) {
      this.portalVisible = true
      this.$nextTick(() => {
        this.$refs.portalManage.init(row);
      })
    },
    closeForm(isRefresh) {
      this.previewVisible = false
      this.portalVisible = false
      if (isRefresh) this.refresh()
    },
    reset() {
      this.listQuery.keyword = "";
      this.listQuery.enabledMark = ""
      this.initData();
    },
    initData() {
      this.listLoading = true;
      getSystem(this.listQuery).then((res) => {
        this.list = res.data.list;
        this.listLoading = false;
      }).catch(() => {
        this.listLoading = false;
      });
    },
    preview(row) {
      this.previewVisible = true
      this.$nextTick(() => {
        this.$refs.menuManage.init(row);
      })
    },
    addOrUpdateHandle(id) {
      this.formVisible = true;
      this.$nextTick(() => {
        this.$refs.Form.init(id);
      });
    },
    handleDel(id) {
      this.$confirm(this.$t("common.delTip"), this.$t("common.tipTitle"), {
        type: "warning",
      }).then(() => {
        delSystem(id).then((res) => {
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
  }
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
</style>
