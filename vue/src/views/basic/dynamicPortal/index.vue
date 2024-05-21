<template>
  <div class="dynamicPortal-container" v-loading="loading">
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
  </div>
</template>
<script>
import mixin from '../../dashboard/mixin';
export default {
  name: 'dynamicPortal',
  mixins: [mixin],
  created() {
    this.systemId = this.userInfo.systemId;
    this.portalId = this.$route.meta.relationId
    this.getData()
  },
}
</script>
<style lang="scss" scoped>
.dynamicPortal-container {
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
  >>> .vue-grid-layout {
    margin: -10px;
  }
  >>> .el-scrollbar__view {
    overflow: hidden;
  }
}
</style>