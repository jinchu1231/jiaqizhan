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
            <div v-loading="loading" style="height: 100%;">
              <div v-if="list.length" class="menu-tab">
                <el-tabs class="tabs-box" v-model="activeKey" @tab-click="handleTabClick">
                  <el-tab-pane v-for="tab in list" :key="tab.id" :name="tab.id"
                    :label="tab.fullName"></el-tab-pane>
                </el-tabs>
                <el-scrollbar class="px-20px">
                  <template v-for="(item,index) in authorizeList">
                    <div :key="index">
                      <p class="title">{{ item.fullName }}</p>
                      <p class="content">
                        <template v-if="item.type==1">
                          <span v-for="(child,i) in item.list" :key="i">{{ child.fullName }}</span>
                        </template>
                        <template v-else-if="item.type==2">未分配{{ item.fullName }}</template>
                        <template v-else>未设置{{ item.fullName }}（无权限控制）</template>
                      </p>
                    </div>
                  </template>
                </el-scrollbar>
              </div>
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
import { getMenuPermissionGroup, getMenuPermission } from "@/api/system/menu";
export default {
  data() {
    return {
      visible: false,
      list: [],
      title: '',
      authorizeList: [],
      activeKey: '',
      loading: false,
      objectType: '',
      id: '',
      type: 1,
      description: ''
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
      this.getPermissionGroup()
    },
    getPermissionGroup() {
      let query = {
        id: this.id
      }
      getMenuPermissionGroup(query).then(res => {
        this.description = res.data.type == 0 ? '未开启权限' : '该菜单暂未分配权限！'
        this.list = res.data.list
        if (!this.list.length) return this.loading = false
        this.activeKey = this.list[0].id
        this.handleTabClick()
      }).catch(() => { this.loading = false })
    },
    handleTabClick() {
      this.loading = true
      this.authorizeList = []
      let query = {
        id: this.id,
        permissionId: this.activeKey
      }
      getMenuPermission(query).then(res => {
        for (let [_key, value] of Object.entries(res.data)) {
          value && this.authorizeList.push(value)
        }
        this.loading = false
      })
    },
  }
}
</script>
<style lang="scss" scoped>
.el-zoom-in-center {
  >>> .JNPF-common-layout {
    position: unset !important;
  }
}
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
</style>