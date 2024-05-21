<template>
  <el-drawer title="切换门户" :visible.sync="drawer" direction="rtl" size="260px"
    class="JNPF-common-drawer board">
    <el-input v-model="keyword" placeholder="搜索：请输入关键词" clearable style="border:none"
      class="searchBox">
      <i slot="suffix" class="el-input__icon el-icon-search" title="搜索" />
    </el-input>
    <div class="main" v-loading="loading" :element-loading-text="loadingText">
      <div v-if="getPortalList.length">
        <div class="item" v-for="(item,i) in getPortalList" :key="i">
          <p class="item-title">{{item.fullName}}</p>
          <div class="item-list">
            <div class="item-list-item" v-for="(child,ii) in  item.children" :key="ii"
              @click="selectItem(child.id)" :class="{'active':activeId === child.id}">
              <p class="com-hover">{{child.fullName}}</p>
              <i class="el-icon-success"></i>
            </div>
          </div>
        </div>
      </div>
      <el-empty description="暂无数据" :image-size="100" v-else></el-empty>
    </div>
  </el-drawer>
</template>
<script>
import { getPortalSelector, SetDefault } from '@/api/onlineDev/portal'
import { mapGetters } from 'vuex'

export default {
  name: 'Setting',
  data() {
    return {
      drawer: false,
      list: [],
      loading: true,
      loadingText: '数据加载中',
      activeId: '',
      keyword: ''
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    getPortalList() {
      const newList = [];
      let list = JSON.parse(JSON.stringify(this.list));
      for (let i = 0; i < list.length; i++) {
        const item = list[i];
        item.children = item.children?.length ? item.children.filter(o => o.fullName.indexOf(this.keyword) !== -1) : [];
        newList.push(item);
      }
      return newList.filter(o => o.children && o.children.length);
    }
  },
  methods: {
    init(id) {
      this.keyword = ''
      this.drawer = true
      this.activeId = id
      this.getList()
    },
    getList() {
      this.loading = true
      this.loadingText = '数据加载中'
      getPortalSelector(1, this.userInfo.systemId).then(res => {
        this.list = res.data.list
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    selectItem(id) {
      if (this.activeId == id) return
      this.loading = true
      this.loadingText = '切换门户中'
      SetDefault(id).then(res => {
        this.activeId = id
        this.$emit('refresh', id)
        this.loading = false
        this.$store.commit('user/SET_USERINFO_PORTALID', id)
        this.$message({ message: res.msg, type: 'success', duration: 1000 })
      }).catch(() => { this.loading = false })
    }
  }
}
</script>
<style lang="scss" scoped>
.board {
  >>> .el-drawer__body {
    overflow: hidden;
  }
  >>> .el-input__inner {
    border: none;
    border-bottom: 1px solid #dcdfe6;
    border-radius: 0;
  }
  >>> .main {
    padding: 10px 20px;
    height: 100%;
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
          i {
            font-size: 20px;
            color: #bdbdbd;
          }
          &.active i {
            color: #1890ff;
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
}
</style>