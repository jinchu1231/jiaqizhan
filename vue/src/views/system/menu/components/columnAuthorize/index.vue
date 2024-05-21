<template>
  <div>
    <el-drawer :title="dialogTitle" :visible.sync="columnAuthorizeListDrawer"
      :wrapperClosable="false" ref="drawer" size="700px" class="JNPF-common-drawer">
      <div class="JNPF-flex-main">
        <div class="JNPF-common-head">
          <topOpts @add="addOrUpdateHandle('')">
            <el-button type="text" icon="el-icon-copy-document" @click="handleBatchAdd">
              批量新增
            </el-button>
            <el-button v-if="menuType === 2" type="text" icon="icon-ym icon-ym-dbLink"
              @click="addDataConnect">
              数据连接
            </el-button>
          </topOpts>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh
              JNPF-common-head-icon" :underline="false" @click="getList()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="treeList" row-key="id" default-expand-all
          :tree-props="{ children: 'children', hasChildren: '' }">
          <el-table-column prop="bindTable" label="数据库表" width="120" />
          <el-table-column prop="enCode" label="字段名称" width="160" />
          <el-table-column prop="fullName" label="字段说明" />
          <el-table-column prop="sortCode" label="排序" width="90" align="center" />
          <el-table-column prop="enabledMark" label="状态" width="90">
            <template slot-scope="scope">
              <el-tag :type="scope.row.enabledMark == 1 ? 'success' : 'danger'" disable-transitions>
                {{scope.row.enabledMark==1?'启用':'禁用'}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row.id)" @del="handleDel(scope.row.id)" />
            </template>
          </el-table-column>
        </JNPF-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
      </span>
    </el-drawer>
    <ColumnAuthorizeForm v-if="columnAuthorizeFormVisible" ref="ColumnAuthorizeForm"
      @refreshDataList="getList" />
    <ColumnAuthorizeBatchForm v-if="columnAuthorizeBatchFormVisible" ref="ColumnAuthorizeBatchForm"
      @refreshDataList="getList" />
    <ColumnConnectForm v-if="columnConnectFormVisible" ref="ColumnConnectForm"
      @refreshDataList="getConnectList" />
  </div>
</template>

<script>
import { getInfo } from "@/api/system/authorize";
import {
  getColumnAuthorizeList,
  updateColumnState,
  delColumn
} from "@/api/system/columnAuthorize";
import ColumnAuthorizeForm from "./Form";
import ColumnAuthorizeBatchForm from "./BatchForm";
import { getFieldNameList } from "@/api/system/menu";
import { getDataSourceListAll } from "@/api/systemData/dataSource";
import ColumnConnectForm from "../connectForm";
export default {
  components: {
    ColumnAuthorizeForm,
    ColumnAuthorizeBatchForm,
    ColumnConnectForm
  },
  data() {
    return {
      columnAuthorizeListDrawer: false,
      columnAuthorizeFormVisible: false,
      columnAuthorizeBatchFormVisible: false,
      columnConnectFormVisible: false,
      dialogTitle: "",
      listQuery: {
        keyword: ""
      },
      moduleId: "",
      loading: false,
      btnLoading: false,
      listLoading: false,
      treeList: [],
      menuType: 2,
      dbOptions: [],
      dbList: [],
      tableName: "",
      dataType: "",
      dataList: {}
    };
  },
  methods: {
    init(moduleId, fullName, type, dataType) {
      this.menuType = type;
      this.dataType = dataType
      this.columnAuthorizeListDrawer = true;
      this.moduleId = moduleId;
      this.dialogTitle = `列表权限 - ${fullName}`;
      this.dbList = [];
      this.dbOptions = [];
      this.$nextTick(() => {
        if (this.$refs["ColumnConnectForm"]) {
          this.$refs["ColumnConnectForm"].dataForm.dataSelect = "";
        }
        this.listQuery.keyword = "";
        this.getList();
      });
      if (this.menuType === 2) {
        this.getDataSourceListAll();
        this.getInfo()
      }
    },
    getDataSourceListAll() {
      getDataSourceListAll().then(res => {
        const list = res.data.list || []
        this.dbOptions = list.filter(o => o.children && o.children.length);
      })
    },
    getInfo() {
      getInfo(this.moduleId, this.dataType).then(res => {
        this.dataList = res.data || {}
        this.tableName = this.dataList.linkTables || ''
      })
    },
    //数据连接
    addDataConnect() {
      this.columnConnectFormVisible = true;
      this.$nextTick(() => {
        this.$refs.ColumnConnectForm.init(this.dataList, this.moduleId, this.dbOptions, this.dataType);
      });
    },
    getConnectList(tableName) {
      this.tableName = tableName || "";
      this.getInfo()
    },
    getList() {
      this.listLoading = true;
      getColumnAuthorizeList(this.moduleId, this.listQuery)
        .then(res => {
          this.treeList = res.data.list;
          this.listLoading = false;
          this.btnLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
          this.btnLoading = false;
        });
    },
    handleReLoad() {
      this.btnLoading = true;
      this.getList();
    },
    handleUpdateState(row) {
      const txt = row.enabledMark ? "禁用" : "开启";
      this.$confirm(`您确定要${txt}当前列表权限吗, 是否继续?`, "提示", {
        type: "warning"
      })
        .then(() => {
          updateColumnState(row.id).then(res => {
            this.$message({
              type: "success",
              message: res.msg,
              duration: 1000,
              onClose: () => {
                row.enabledMark = row.enabledMark ? 0 : 1;
              }
            });
          });
        })
        .catch(() => { });
    },
    addOrUpdateHandle(id) {
      this.columnAuthorizeFormVisible = true;
      this.$nextTick(() => {
        this.$refs.ColumnAuthorizeForm.init(
          this.moduleId,
          id,
          this.menuType,
          this.dataType
        );
      });
    },
    handleBatchAdd() {
      this.columnAuthorizeBatchFormVisible = true;
      this.$nextTick(() => {
        this.$refs.ColumnAuthorizeBatchForm.init(this.moduleId);
      });
    },
    handleDel(id) {
      this.$confirm(this.$t("common.delTip"), this.$t("common.tipTitle"), {
        type: "warning"
      })
        .then(() => {
          delColumn(id).then(res => {
            this.$message({
              type: "success",
              message: res.msg,
              duration: 1500,
              onClose: () => {
                this.getList();
              }
            });
          });
        })
        .catch(() => { });
    }
  }
};
</script>
