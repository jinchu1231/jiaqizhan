<template>
  <section class="app-main" :copyright="sysConfig.copyright">
    <keep-alive :include="useCache?cachedViews:[]">
      <router-view :key="key" />
    </keep-alive>

    <ResetPwdForm v-if="resetFormVisible" ref="ResetPwdForm" @refreshDataList="initData" />
  </section>
</template>

<script>
import ResetPwdForm from './ResetPassword'
import { mapGetters } from 'vuex'
import { getSystemConfig } from '@/api/system/sysConfig'
import { updatePasswordMessage } from '@/api/user'

export default {
  name: 'AppMain',
  components: {
    ResetPwdForm
  },
  data() {
    return {
      resetFormVisible: false,
      baseForm:{
        mandatoryModificationOfInitialPassword:0,
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    },
    useCache() {
      return this.$store.state.settings.useCache
    },
    key() {
      return this.$route.path
    },
    sysConfig() {
      return this.$store.state.settings.sysConfig
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      this.listLoading = true
      this.$nextTick(() => {
        updatePasswordMessage().then(res => {

        }),
        getSystemConfig().then(res => {
          if(this.userInfo.changePasswordDate==null && res.data.mandatoryModificationOfInitialPassword==1){
            this.resetFormVisible = true
            this.$nextTick(() => {
              this.$refs.ResetPwdForm.init(this.userInfo.userId, this.userInfo.userAccount)
            })
          }
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
  }
}
</script>

<style lang="scss" scoped>
.app-main {
  height: calc(100vh - 60px);
  width: 100%;
  position: relative;
  overflow: auto;
  overflow-x: hidden;
  padding: 10px;
  margin-bottom: 20px;
  box-sizing: border-box;
  background: url('../../assets/images/loading-iframe.gif') no-repeat center
    center #ebeef5;
  background-size: 600px 450px;
  &::before {
    content: attr(copyright);
    font-size: 14px;
    text-align: center;
    color: #999;
    text-align: center;
    width: 100%;
    display: block;
    position: absolute;
    bottom: 20px;
    left: 0;
  }
}

.fixed-header + .app-main {
  // padding-top: 50px;
  padding: 70px 20px 20px;
}

.hasTagsView {
  .app-main {
    height: calc(100vh - 100px);
  }

  .fixed-header + .app-main {
    padding: 104px 20px 20px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
