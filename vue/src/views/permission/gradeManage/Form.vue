<template>
  <el-dialog :title="!dataForm.id?'新建':'编辑'" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center" lock-scroll width="900px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
      label-width="100px" class="JNPF-dialog-gradeManage">
      <el-alert title="设置当前组织部门的管理员和相关操作权限" type="warning" :closable="false" show-icon
        class="mb-20">
      </el-alert>
      <el-form-item label="设置管理员" prop="userId" v-if="!dataForm.id && userInfo.isAdministrator">
        <JnpfUserSelect v-model="dataForm.userId" placeholder="请选择管理员" @change="onChange"
          :key="key" />
      </el-form-item>
      <el-form-item label="设置管理员" prop="userId" v-if="!dataForm.id && !userInfo.isAdministrator">
        <grade-user-select v-model="dataForm.userId" placeholder="请选择管理员" @change="onChange"
          :key="key" />
      </el-form-item>
      <el-form-item label="设置管理员" prop="userId" v-if="dataForm.id">
        <JnpfUserSelect v-model="dataForm.userId" placeholder="请选择管理员" @change="onChange" :key="key"
          disabled />
      </el-form-item>
      <template>
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="开发平台" name="module"></el-tab-pane>
          <el-tab-pane label="应用权限" name="system"></el-tab-pane>
          <el-tab-pane label="组织权限" name="gradeManage"></el-tab-pane>
        </el-tabs>
      </template>
      <template v-if="activeName!='gradeManage'">
        <el-tree :data="treeData" ref="treeData" v-loading="treeLoading" show-checkbox
          default-expand-all node-key="id" :check-strictly="checkStrictly" check-on-click-node
          :props="defaultProps" :expand-on-click-node="false" @check-change="selectTreeNodeClick"
          style="height:400px;overflow: auto">
          <span slot-scope="{ node, data }">
            <i :class="data.icon" />
            <span style="padding-left: 4px;">{{ node.label }}</span>
          </span>
        </el-tree>
      </template>
      <template v-else-if="activeName=='gradeManage'">
        <el-table v-loading="listLoading" :data="treeList" row-key="organizeId" default-expand-all
          :tree-props="{children: 'children', hasChildren: ''}" style="height:400px;overflow: auto">
          <el-table-column prop="fullName" label="组织架构">
            <template slot-scope="scope">
              <i :class=scope.row.icon></i>
              {{scope.row.fullName}}
            </template>
          </el-table-column>
          <el-table-column label="组织操作权限(本层级)" width="280">
            <template slot-scope="scope">
              <template v-if="scope.row.thisLayerSelect===2">
                <el-checkbox checked disabled>查看</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerSelect===3">
                <el-checkbox disabled>查看</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerSelect===0 ||scope.row.thisLayerSelect===1">
                <el-checkbox v-model="scope.row.thisLayerSelect" :true-label="1" :false-label="0"
                  @change="onThisLayerSelectChange($event,scope.row)">查看</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerAdd===2">
                <el-checkbox checked disabled>添加</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerAdd===3">
                <el-checkbox disabled>添加</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerAdd===0 ||scope.row.thisLayerAdd===1">
                <el-checkbox v-model="scope.row.thisLayerAdd" :true-label="1" :false-label="0"
                  :disabled="scope.row.thisLayerSelect===0">添加</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerEdit===2">
                <el-checkbox checked disabled>编辑</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerEdit===3">
                <el-checkbox disabled>编辑</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerEdit===0 ||scope.row.thisLayerEdit===1">
                <el-checkbox v-model="scope.row.thisLayerEdit" :true-label="1" :false-label="0"
                  :disabled="scope.row.thisLayerSelect===0">编辑</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerDelete===2">
                <el-checkbox checked disabled>删除</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerDelete===3">
                <el-checkbox disabled>删除</el-checkbox>
              </template>
              <template v-if="scope.row.thisLayerDelete===0 ||scope.row.thisLayerDelete===1">
                <el-checkbox v-model="scope.row.thisLayerDelete" :true-label="1" :false-label="0"
                  :disabled="scope.row.thisLayerSelect===0">删除</el-checkbox>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="子组织操作权限(子层级)" width="280">
            <template slot-scope="scope">
              <template v-if="scope.row.subLayerSelect===2">
                <el-checkbox checked disabled>查看</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerSelect===3">
                <el-checkbox disabled>查看</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerSelect===0 ||scope.row.subLayerSelect===1">
                <el-checkbox v-model="scope.row.subLayerSelect" :true-label="1" :false-label="0"
                  @change="onSubLayerSelectChange($event,scope.row)">查看</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerAdd===2">
                <el-checkbox checked disabled>添加</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerAdd===3">
                <el-checkbox disabled>添加</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerAdd===0 ||scope.row.subLayerAdd===1">
                <el-checkbox v-model="scope.row.subLayerAdd" :true-label="1" :false-label="0"
                  :disabled="scope.row.subLayerSelect===0">添加</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerEdit===2">
                <el-checkbox checked disabled>编辑</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerEdit===3">
                <el-checkbox disabled>编辑</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerEdit===0 ||scope.row.subLayerEdit===1">
                <el-checkbox v-model="scope.row.subLayerEdit" :true-label="1" :false-label="0"
                  :disabled="scope.row.subLayerSelect===0">编辑</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerDelete===2">
                <el-checkbox checked disabled>删除</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerDelete===3">
                <el-checkbox disabled>删除</el-checkbox>
              </template>
              <template v-if="scope.row.subLayerDelete===0 ||scope.row.subLayerDelete===1">
                <el-checkbox v-model="scope.row.subLayerDelete" :true-label="1" :false-label="0"
                  :disabled="scope.row.subLayerSelect===0">删除</el-checkbox>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import gradeUserSelect from "./GradeUserSelect"
import { mapGetters } from "vuex";
import { getSelectorOrgList, saveGradeManage } from '@/api/permission/gradeManage'
export default {
  components: { gradeUserSelect },
  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      dataForm: {
        id: '',
        userId: ''
      },
      activeName: 'module',
      listLoading: false,
      refreshTable: true,
      treeList: [],
      dataRule: {
        userId: [
          { required: true, message: '请选择管理员', trigger: 'click' }
        ]
      },
      treeData: [],
      menuTreeData: [],
      systemData: [],
      checkStrictly: false,
      treeLoading: false,
      defaultProps: { // 配置项（必选）
        value: 'id',
        label: 'fullName',
        children: 'children'
      },
      moduleIds: [],
      systemIds: [],
      key: +new Date()
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  methods: {
    onChange(val) {
      if (!val) return
      this.initData()
    },
    handleClick() {
      if (this.activeName === 'gradeManage') return
      this.$nextTick(() => {
        this.treeData = this.activeName == 'module' ? this.menuTreeData : this.systemData
        const parentIds = this.parentIds(this.treeData)
        const childrenIds = this[this.activeName + 'Ids'].filter(id => !parentIds.includes(id))
        this.$refs.treeData.setCheckedKeys(childrenIds)
      })
    },
    parentIds(treeDate) {
      const arr = []
      const loop = list => {
        for (let i = 0; i < list.length; i++) {
          const e = list[i]
          const children = e.children
          if (children && children.length > 0) {
            arr.push(e.id)
            loop(children)
          }
        }
      }
      loop(treeDate)
      return arr
    },
    // 设置数据
    selectTreeNodeClick() {
      const parentIds = this.$refs.treeData.getHalfCheckedKeys()
      const childrenIds = this.$refs.treeData.getCheckedKeys()
      const newIds = [...parentIds, ...childrenIds]
      const dataIds = [...new Set(newIds)]
      this[this.activeName + 'Ids'] = dataIds
    },
    initData() {
      this.activeName = 'module'
      if (this.dataForm.userId) this.treeLoading = true
      this.listLoading = true
      getSelectorOrgList(this.dataForm.userId || '').then(res => {
        this.treeList = res.data.orgAdminList || []
        this.menuTreeData = res.data.modulePermissionList || []
        this.systemData = res.data.systemPermissionList || []
        this.moduleIds = res.data.moduleIds || []
        this.systemIds = res.data.systemIds || []
        this.handleClick()
        this.listLoading = false
        this.btnLoading = false
        this.treeLoading = false
      }).catch(() => {
        this.listLoading = false
        this.btnLoading = false
        this.treeLoading = false
        this.dataForm.userId = ''
        this.key = +new Date()
      })
    },
    onThisLayerSelectChange(val, row) {
      if (val) return
      if (row.thisLayerAdd === 1) this.$set(row, 'thisLayerAdd', 0)
      if (row.thisLayerEdit === 1) this.$set(row, 'thisLayerEdit', 0)
      if (row.thisLayerDelete === 1) this.$set(row, 'thisLayerDelete', 0)
    },
    onSubLayerSelectChange(val, row) {
      if (val) return
      if (row.subLayerAdd === 1) this.$set(row, 'subLayerAdd', 0)
      if (row.subLayerEdit === 1) this.$set(row, 'subLayerEdit', 0)
      if (row.subLayerDelete === 1) this.$set(row, 'subLayerDelete', 0)
    },
    init(id) {
      this.treeList = []
      this.treeData = []
      this.dataForm.id = id || ""
      this.dataForm.userId = id || ""
      this.visible = true
      this.formLoading = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.dataForm.id = id || ""
        this.dataForm.userId = id || ""
        this.initData()
        this.formLoading = false
      })
      this.formLoading = false
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          let query = {
            ...this.dataForm,
            orgAdminModel: this.treeList,
            systemIds: this.systemIds,
            moduleIds: this.moduleIds
          }
          this.btnLoading = true
          saveGradeManage(query).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('refreshDataList')
              }
            })
          }).catch(() => {
            this.btnLoading = false
          })
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.JNPF-dialog-gradeManage {
  height: 60vh;
}
.el-checkbox {
  margin-right: 0;
}
.el-checkbox + .el-checkbox {
  margin-left: 10px;
}
>>> .el-dialog .el-dialog__body {
  padding: 20px 20px 2px !important;
}
</style>