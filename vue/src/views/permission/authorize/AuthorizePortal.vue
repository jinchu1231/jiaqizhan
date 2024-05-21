<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main authorize-bd">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack" :content="title" />
        <div class="options">
          <span class="border-button" @click="setAllExpand('open')">
            {{$t('authorize.expandAll')}}
          </span>
          <span class="border-button" @click="setAllExpand('close')">
            {{$t('authorize.foldAll')}}
          </span>
          <span class="border-button" @click="handleCheck('checkAll')">
            {{$t('authorize.checkAll')}}
          </span>
          <span class="border-button" @click="handleCheck('cancelCheckAll')">
            {{$t('authorize.deselectAll')}}
          </span>
          <el-divider direction='vertical' class="divider"></el-divider>
          <el-button type="primary" :loading="btnLoading"
            @click="handleConfirm()">{{$t('common.saveButton')}}</el-button>
          <el-button @click="goBack">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <el-alert title="请先在对应权限组【权限授权】中授权应用权限" type="warning" show-icon :closable="false">
      </el-alert>
      <div class="main">
        <el-form ref="dataForm" :model="dataForm" @keyup.enter.native="handleConfirm()">
          <el-form-item>
            <el-tree :data="list" ref="authorizeTree" v-loading="treeLoading" show-checkbox
              v-show="list.length" default-expand-all node-key="id" :check-strictly="checkStrictly"
              check-on-click-node :props="defaultProps" :expand-on-click-node="false"
              @check-change="selectTreeNodeClick">
              <div slot-scope="{ node, data }">
                <i :class="data.icon" />
                <span style="padding-left: 4px;">{{ node.label }}</span>
              </div>
            </el-tree>
            <div v-show="!list.length" v-loading="treeLoading">
              <el-empty description="请先设置权限授权"></el-empty>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </transition>
</template>
<script>
import {
  getPortalSelector,
  savePortal
} from '@/api/permission/authorize'
export default {
  data() {
    return {
      visible: false,
      treeLoading: false,
      btnLoading: false,
      checkStrictly: true,
      objectId: '',
      dataForm: {
        ids: [],
      },
      portalData: [],
      title: '',
      active: 0,
      list: [],
      defaultProps: { // 配置项（必选）
        value: 'id',
        label: 'fullName',
        children: 'children'
      }
    }
  },
  methods: {
    init(id, fullName) {
      Object.assign(this.$data, this.$options.data())
      this.treeLoading = true
      this.visible = true
      this.objectId = id
      this.title = fullName
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.getPortalSelector()
      })
    },
    getPortalSelector() {
      this.list = []
      getPortalSelector(this.objectId).then(res => {
        this.list = res.data.list
        this.portalData = res.data.all
        this.dataForm.ids = [...res.data.ids]
        this.dataForm.ids = [...new Set(this.dataForm.ids)]
        this.moduleIdsTemp = this.dataForm.ids
        this.ids = this.moduleIdsTemp
        this.$refs.authorizeTree.setCheckedKeys(this.dataForm.ids)
        this.checkStrictly = false
        this.treeLoading = false
      }).catch(() => {
        this.treeLoading = false
      })
    },
    goBack() {
      this.$emit('close')
    },
    // 全选/全不选
    handleCheck(val) {
      if (val === 'checkAll') {
        this.$refs.authorizeTree.setCheckedKeys(this.portalData)
      }
      if (val === 'cancelCheckAll') {
        this.$refs.authorizeTree.setCheckedKeys([])
      }
    },
    // 折叠 展开
    setAllExpand(state) {
      let result
      if (state === 'open') {
        result = true
      }
      if (state === 'close') {
        result = false
      }
      const nodes = this.$refs.authorizeTree.store.nodesMap
      for (let i in nodes) {
        nodes[i].expanded = result
      }
    },
    // 设置数据
    selectTreeNodeClick() {
      const parentIds = this.$refs.authorizeTree.getHalfCheckedKeys()
      const childrenIds = this.$refs.authorizeTree.getCheckedKeys()
      const newIds = [...parentIds, ...childrenIds]
      const dataIds = [...new Set(newIds)]
      this.dataForm.ids = dataIds
    },
    handleConfirm() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          savePortal(this.objectId, this.dataForm).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('close', true)
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
.authorize-bd {
  .JNPF-common-page-header {
    border-bottom: none;
  }
  .el-steps {
    border-radius: 0;
    padding: 13px 45px !important;
  }
}
.main {
  padding: 20px;
}
.border-button {
  border: 1px solid transparent;
  background-color: transparent;
  cursor: pointer;
  width: 70px;
  height: 32px;
  color: #606266;
  font-size: 12px;
  border-radius: 3px;
  text-align: center;
  line-height: 32px;
  display: inline-block;
  margin-right: 10px;
}

.border-button:hover {
  background-color: rgba(0, 0, 0, 0.06);
}
.divider {
  height: 28px;
  margin: 0 15px 0 0;
}
</style>
