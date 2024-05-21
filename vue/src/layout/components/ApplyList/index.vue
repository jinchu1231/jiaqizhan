<template>
  <div>
    <el-drawer title="切换应用" :visible.sync="drawer" direction="rtl"
      class="contacts-drawer JNPF-common-drawer" size="280px" :modal="true" :wrapperClosable="false"
      :before-close="handleClose" ref="drawer">
      <el-input v-model="keyword" placeholder="搜索：请输入关键词" clearable style="border:none"
        class="searchBox">
        <i slot="suffix" class="el-input__icon el-icon-search" title="搜索" />
      </el-input>
      <div class="main">
        <div class="item">
          <div class="item-list" v-if="applyList.length">
            <div class="item-list-item" v-for="(item, i) in applyList" :key="i"
              @click="changeMajor(item.id,'System')" :class="{ active: activeId === item.id }">
              <p class="com-hover">{{ item.name }}</p>
              <i class="el-icon-success" :class="{ active2: activeId === item.id }"></i>
            </div>
          </div>
          <el-empty description="暂无数据" :image-size="100" v-else></el-empty>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import { setMajor } from '@/api/permission/userSetting'
export default {
  data() {
    return {
      drawer: false,
      keyword: '',
      activeId: "",
      list: []
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    applyList() {
      this.list = this.userInfo.systemIds ? JSON.parse(JSON.stringify(this.userInfo.systemIds)) : []
      this.list = this.list.filter(item => item.name.includes(this.keyword))
      return this.list
    }
  },
  methods: {
    init() {
      this.keyword = ''
      this.drawer = true
      this.activeId = this.userInfo.systemId
      this.list = this.userInfo.systemIds ? JSON.parse(JSON.stringify(this.userInfo.systemIds)) : []
    },
    changeMajor(majorId, majorType) { //切换系统
      let query = {
        majorId,
        majorType
      }
      if (this.activeId === majorId) return
      setMajor(query).then(res => {
        this.activeId = majorId
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            this.$store.commit('user/SET_TOAST', false)
            this.$router.replace('/');
            setTimeout(() => {
              location.reload();
            }, 50);
          }
        })
      })
    },
    handleClose() {
      this.drawer = false
    }
  }
}
</script>
<style lang="scss" scoped>
>>> .el-drawer__body {
  overflow: hidden;
}
.searchBox {
  flex: 1;
  >>> .el-input__inner {
    border: none;
    border-bottom: 1px solid #dcdfe6;
    border-radius: 0;
  }
}
.main {
  padding: 10px 20px;
  height: calc(100% - 32px);
  overflow: auto;
  overflow-x: hidden;
  .item {
    .item-title {
      font-size: 12px;
      line-height: 30px;
      color: #999;
    }
    .item-list {
      font-size: 14px;
      color: #707070;
      .item-list-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 45px;
        cursor: pointer;
        >>> .el-icon-success:before {
          font-size: 20px;
          color: #bdbdbd;
        }
        .active2:before {
          color: #1890ff;
        }
        .icon {
          font-size: 18px;
          color: #bdbdbd;
        }
        &.active {
          // color: red;
        }
      }
    }
  }
  .noData-txt {
    font-size: 14px;
    color: #909399;
    line-height: 20px;
    text-align: center;
    padding-top: 10px;
  }
}
</style>