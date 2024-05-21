<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-common-layout menu-list" style="position: unset">
      <div class="JNPF-common-layout-center">
        <div class="JNPF-preview-main flow-form-main">
          <div class="JNPF-common-page-header">
            <el-page-header @back="goBack" :content="title" />
            <div class="options">
              <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
            </div>
          </div>
          <template>
            <div class="app-container Profile-container">
              <el-tabs tab-position="left" style="height:100%;" v-model="activeKey"
                class="profile-tabs" v-loading="loading" v-if="permissionsList.length"
                @tab-click="handleTabClick($event)">
                <el-tab-pane v-for="item in permissionsList" :key="item.id" :label="item.fullName"
                  :name="item.id">
                  <div class="authorize">
                    <el-tabs v-model="activeTab" class="JNPF-el_tabs"
                      @tab-click="handleAuthorizeClick">
                      <el-tab-pane label="应用权限" name="system"></el-tab-pane>
                      <el-tab-pane label="菜单权限" name="module"></el-tab-pane>
                      <el-tab-pane label="按钮权限" name="button"></el-tab-pane>
                      <el-tab-pane label="列表权限" name="column"></el-tab-pane>
                      <el-tab-pane label="表单权限" name="form"></el-tab-pane>
                      <el-tab-pane label="数据权限" name="resource"></el-tab-pane>
                      <el-tab-pane label="门户权限" name="portalManage"></el-tab-pane>
                      <el-tree :data="treeData" :props="defaultProps" default-expand-all
                        highlight-current ref="treeBox" :expand-on-click-node="false"
                        class="JNPF-common-el-tree" node-key="id" v-loading="treeLoading"
                        v-if="treeData.length">
                        <span class="custom-tree-node" slot-scope="{ node, data }">
                          <i :class="data.icon"></i>
                          <span class="text">{{node.label}}</span>
                        </span>
                      </el-tree>
                      <div class="no-data" v-else>
                        <el-empty class="empty" :description="description" />
                      </div>
                    </el-tabs>
                  </div>
                </el-tab-pane>
              </el-tabs>
              <div class="no-data" v-else>
                <el-empty class="empty" v-if="!loading" :description="description" />
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </transition>
</template>
<script>
import { getPermissionGroup, getPermission } from '@/api/permission/organize'
export default {
  data() {
    return {
      visible: false,
      list: [],
      title: '',
      activeKey: '',
      loading: false,
      objectType: '',
      id: '',
      type: 1,
      description: '',
      permissionsList: [],
      treeData: [],
      info: [],
      activeTab: 'system',
      defaultProps: {
        children: 'children',
        label: 'fullName'
      },
      activeKey: '',
      treeLoading: false
    }
  },
  methods: {
    goBack() {
      this.$emit('close')
    },
    init(item, type = 1) {
      this.visible = true
      this.list = []
      this.title = item.fullName + '的权限'
      this.loading = true
      this.id = item.id || ''
      this.type = type || 1
      this.objectType = item.type || ''
      this.activeTab = 'system'
      this.getPermissionGroup()
    },
    getPermissionGroup() {
      let query = {
        id: this.id,
        objectType: this.objectType
      }
      getPermissionGroup(query).then(res => {
        this.description = res.data.type == 0 ? '该菜单暂未分配权限！' : '未分配权限'
        this.permissionsList = res.data.list
        if (!this.permissionsList.length) return this.loading = false
        this.activeKey = this.permissionsList[0].id
        this.getList()
      }).catch(() => { this.loading = false })
    },
    handleTabClick(item) {
      this.activeKey = item.name || ''
      this.activeTab = 'system'
      this.getList()
    },
    handleAuthorizeClick(item) {
      this.activeTab = item.name
      this.getList()
    },
    getList() {
      this.loading = false
      this.treeLoading = true
      let query = {
        id: this.id,
        permissionId: this.activeKey,
        itemType: this.activeTab,
        objectType: this.objectType
      }
      getPermission(query).then(res => {
        this.description = res.data.type == 0 ? '该菜单暂未分配权限！' : '未分配权限'
        this.treeData = res.data || []
        this.treeLoading = false
      })
    },
  }
}
</script>
<style lang="scss" scoped>
.menu-list {
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
  >>> .no-data {
    height: 100%;
    .empty {
      height: 100%;
    }
  }
}
.Profile-container {
  position: relative;
  padding-top: 10px;
  .profile-tabs {
    >>> .el-tabs__nav-scroll {
      width: 160px;
    }
    >>> .el-tabs__item {
      text-align: left !important;
    }
    >>> .el-tabs__content {
      height: 100%;
      .el-tab-pane {
        height: 100%;
        overflow: auto;
        overflow-x: hidden;

        &.el-tab-pane-authorize {
          overflow: hidden;
        }
      }
    }
  }
}
.authorize {
  height: 100%;
  overflow: hidden;
  >>> .el-tabs__nav-scroll {
    padding-top: 0 !important;
    width: unset !important;
  }
  >>> .el-tabs__content {
    overflow: auto;
    overflow-x: hidden;
    .el-tab-pane {
      height: auto !important;
      padding: unset !important;
    }
    .JNPF-common-el-tree {
      padding: 0;
      height: 630px;
      padding: unset !important;
    }
  }
}
</style>