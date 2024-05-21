<template>
  <el-dialog :close-on-click-modal="false" :close-on-press-escape="false" :visible.sync="visible"
    lock-scroll class="JNPF-dialog JNPF-dialog_center" width="700px">
    <el-form ref="dataForm" v-loading="formLoading" :model="dataForm" label-width="100px">
      <el-form-item label="工作移交人" prop="account">
        <el-input v-model="dataForm.account" placeholder="请输入工作移交人" readonly />
      </el-form-item>
      <el-form-item label="工作交接人" prop="handoverId">
        <JnpfUserSelect v-model="dataForm.managerId" placeholder="请选择工作交接人" @change="changeUser"
          :key="key" />
      </el-form-item>
      <el-form-item label="交接内容" prop="handoverContent">
        <template>
          <div class="tabs-box">
            <el-tabs v-model="activeName">
              <el-tab-pane label="流程列表" name="flow">
                <el-alert title="转交内容：发起节点、审批节点、抄送人是移交人的流程" type="warning" show-icon>
                </el-alert>
                <div class="tabs-box-inner">
                  <el-tree :data="treeData.flow" :props="props" show-checkbox default-expand-all
                    @check-change="handleCheckChange(1)" node-key="id" ref="handoverContentTree1"
                    check-on-click-node :expand-on-click-node="false">
                    <span class="custom-tree-node" slot-scope="{ node, data }" :title="node.label">
                      <i :class="data.icon"></i>
                      <span class="text">{{node.label}}</span>
                    </span>
                  </el-tree>
                </div>
              </el-tab-pane>
              <el-tab-pane label="我的待办" name="wait">
                <el-alert title="转交内容：包含移交人未处理的待办和已处理的待办" type="warning" show-icon>
                </el-alert>
                <div class="tabs-box-inner">
                  <el-tree :data="treeData.wait" :props="props" show-checkbox default-expand-all
                    @check-change="handleCheckChange(2)" node-key="id" ref="handoverContentTree2"
                    check-on-click-node :expand-on-click-node="false">
                    <span class="custom-tree-node" slot-scope="{ node, data }" :title="node.label">
                      <i :class="data.icon"></i>
                      <span class="text">{{node.label}}</span>
                    </span>
                  </el-tree>
                </div>
              </el-tab-pane>
              <el-tab-pane label="权限组" name="permission">
                <el-alert title="转交内容：移交人的权限组" type="warning" show-icon>
                </el-alert>
                <div class="tabs-box-inner">
                  <el-tree :data="treeData.permission" :props="props" show-checkbox
                    default-expand-all @check-change="handleCheckChange(3)" node-key="id"
                    ref="handoverContentTree3" check-on-click-node :expand-on-click-node="false">
                    <span class="custom-tree-node" slot-scope="{ node, data }" :title="node.label">
                      <i :class="data.icon"></i>
                      <span class="text">{{node.label}}</span>
                    </span>
                  </el-tree>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </template>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>
<script>
import {
  getWorkHandover,
  getWorkByUser
} from '@/api/permission/user'
import { mapGetters } from "vuex"
export default {
  data() {
    return {
      key: +new Date(),
      visible: false,
      formLoading: false,
      btnLoading: false,
      activeName: 'flow',
      treeData: [],
      props: {
        children: 'children',
        label: 'fullName',
        isLeaf: 'isLeaf',
        icon: 'icon',
        value: 'id',
        disabled: 'disabled'
      },
      workHandoverPersonnelData: {},
      fromId: '',
      dataForm: {
        id: '',
        account: '',
        managerId: '',
        waitList: [],
        flowList: [],
        permissionList: []
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  methods: {
    changeUser(id, item) {
      if (this.workHandoverPersonnelData.id === id || item.isAdministrator == 1) {
        this.$message.warning(this.workHandoverPersonnelData.id === id ? '工作交接无法转交给本人' : '工作交接无法转交给管理员')
        this.dataForm.managerId = "";
        this.key = +new Date();
      }
    },
    init(data) {
      this.workHandoverPersonnelData = data
      this.dataForm.account = data.realName + '/' + data.account
      this.visible = true
      this.activeName = 'flow'
      this.dataForm.managerId = ''
      this.dataForm.waitList = []
      this.dataForm.flowList = []
      this.dataForm.permissionList = []
      this.fromId = data.id
      getWorkByUser(this.fromId).then(res => {
        this.treeData = res.data || []
      })
    },
    handleCheckChange(type) {
      const dataName = type == 1 ? 'flowList' : type == 2 ? 'waitList' : 'permissionList'
      const treeRef = this.$refs['handoverContentTree' + type];
      const parentIds = treeRef.getHalfCheckedKeys();
      const childrenIds = treeRef.getCheckedKeys();
      const dataIds = [...new Set([...parentIds, ...childrenIds])];
      this.dataForm[dataName] = dataIds
    },
    dataFormSubmit() {
      // 检查是否是自己交接给自己
      if (this.workHandoverPersonnelData.id === this.dataForm.managerId) {
        this.dataForm.managerId = "";
        this.$message.warning("工作交接无法转交给本人");
        this.key = +new Date();
        return;
      }
      // 检查是否选择了交接人
      if (!this.dataForm.managerId) {
        return this.$message.warning("请先选择工作交接人");
      }
      // 检查是否选择了交接内容
      if (
        !this.dataForm.waitList.length &&
        !this.dataForm.flowList.length &&
        !this.dataForm.permissionList.length
      ) {
        return this.$message.warning("请先选择交接内容");
      }
      this.btnLoading = true;
      const query = {
        waitList: this.dataForm.waitList,
        flowList: this.dataForm.flowList,
        permissionList: this.dataForm.permissionList,
        toId: this.dataForm.managerId,
        fromId: this.fromId
      };
      getWorkHandover(query)
        .then(() => {
          this.$message.success("工作交接成功");
          setTimeout(() => {
            this.visible = false;
            this.btnLoading = false;
            this.$emit("complete");
          }, 1000);
        })
        .catch(() => {
          this.btnLoading = false;
        });
    }
  }
}
</script>
<style lang="scss">
.JNPF-dialog_center {
  .tabs-box {
    border: 1px solid #dcdfe6;
    min-height: 300px;
    padding: 0 10px;
    border-radius: 4px;
    .el-tabs {
      .el-tabs__header {
        margin: 0 0 6px !important;
      }
    }
    .tabs-box-inner {
      max-height: 200px;
      overflow-y: scroll;
    }
  }
}
</style>
