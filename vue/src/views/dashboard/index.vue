<template>
  <div class="dashboard-container" v-loading="loading">
    <template v-if="!noData">
      <template v-if="!ajaxing">
        <template v-if="portalId">
          <PortalLayout :layout="layout" :enabledLock="enabledLock" v-if="type===0"
            @layoutUpdatedEvent="layoutUpdatedEvent" />
          <div class="custom-page" v-if="type===1">
            <component :is="currentView" v-if="linkType===0" />
            <embed :src="url" width="100%" height="100%" type="text/html" v-if="linkType===1" />
          </div>
        </template>
        <div class="portal-layout-nodata" v-else>
          <img src="@/assets/images/dashboard-nodata.png" alt="" class="layout-nodata-img">
          <p class="layout-nodata-txt">暂无数据</p>
        </div>
      </template>
      <Setting ref="Setting" @refresh="refresh" />
      <el-button type="primary" icon="el-icon-arrow-left" size="large" class="setting-btn"
        @click="$refs.Setting.init(portalId)"></el-button>
    </template>
    <template v-else>
      <el-scrollbar class="dashboard-container">
        <adminDashboard />
      </el-scrollbar>
    </template>
  </div>
</template>
<script>
import Setting from './Setting'
import adminDashboard from './admin'
import mixin from './mixin';
export default {
  components: { Setting, adminDashboard },
  mixins: [mixin],
  created() {
    const system = this.userInfo.systemIds.filter(o => o.currentSystem)[0]
    this.systemId = system.id
    this.portalId = this.userInfo.portalId
    this.getData()
  },
  methods: {
    refresh(id) {
      if (!id) return
      this.portalId = id
      this.clearAutoRefresh()
      this.getData()
    },
  }
}
</script>
<style lang="scss" scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
  background: #ebeef5;
  position: relative;
  .custom-page {
    width: 100%;
    height: 100%;
  }
  >>> .layout-area {
    width: 100%;
    border-radius: 4px;
    overflow: hidden;
  }
  .setting-btn {
    position: absolute;
    top: 200px;
    right: -10px;
    height: 40px;
    width: 40px;
    text-align: center;
    padding: 0;
    border-radius: 20px 0 0 20px;
    z-index: 100;
    >>> i {
      font-size: 20px;
      font-weight: 580;
    }
  }
  >>> .vue-grid-layout {
    margin: -10px;
  }
  >>> .el-scrollbar__view {
    overflow: hidden;
  }
}
</style>