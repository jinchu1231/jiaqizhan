<template>
  <div class="transfer__body" v-loading="allLoading" :style="{height}">
    <div class="transfer-pane">
      <div class="transfer-pane__tools">
        <el-input placeholder="输入关键词进行搜索" v-model="keyword" @keyup.enter.native="search" clearable>
          <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
        </el-input>
      </div>
      <div class="transfer-pane__body left-pane">
        <el-tabs v-model="activeName" class="transfer-pane__body-tab hasSys-tab">
          <el-tab-pane label="岗位" name="position">
            <el-tree :data="treeData" :props="props" :expand-on-click-node="false"
              default-expand-all check-on-click-node @node-click="handleNodeClick"
              class="JNPF-common-el-tree" node-key="id" v-loading="positionLoading" ref="position"
              :filter-node-method="filterNode">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <i :class="data.icon"></i>
                <span class="text">{{node.label}}</span>
              </span>
            </el-tree>
          </el-tab-pane>
          <el-tab-pane label="动态参数" name="system">
            <div class="trends">
              <el-tree :data="dynamicList" :props="props" :expand-on-click-node="false"
                default-expand-all check-on-click-node @node-click="handleNodeClick"
                class="JNPF-common-el-tree" node-key="id" v-loading="positionLoading" ref="system"
                :filter-node-method="filterNode">
                <span class="custom-tree-node" slot-scope="{ node, data }">
                  <i :class="data.icon"></i>
                  <span class="text">{{node.label}}</span>
                </span>
              </el-tree>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <div class="transfer-pane">
      <div class="transfer-pane__tools">
        <span>已选</span>
        <el-button @click="removeAll" type="text" class="removeAllBtn">清空列表</el-button>
      </div>
      <div class="transfer-pane__body shadow right-pane">
        <template v-if="selectedData.length">
          <div v-for="(item,index) in selectedData" :key="index" class="selected-item-user">
            <div class="selected-item-main">
              <el-avatar :size="36" :src="define.comUrl+item.headIcon"
                class="selected-item-headIcon" v-if="item.type==='user'">
              </el-avatar>
              <div class="selected-item-headIcon icon" v-else>
                <i :class="item.icon"></i>
              </div>
              <div class="selected-item-text">
                <p class="name">{{item.fullName}}</p>
                <p class="organize" :title="item.organize">{{item.organize}}</p>
              </div>
              <i class="el-icon-delete" @click="removeData(index)"></i>
            </div>
          </div>
        </template>
        <el-empty description="暂无数据" :image-size="120" v-else></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import { getSelectedList } from '@/api/permission/user'
export default {
  name: 'JNPF-userTransfer',
  data() {
    return {
      allLoading: false,
      loading: false,
      positionLoading: false,
      activeName: 'position',
      selectedData: [],
      props: {
        children: 'children',
        label: 'fullName',
        isLeaf: 'isLeaf'
      },
      ids: [],
      keyword: '',
    }
  },
  props: {
    height: {
      type: String,
      default: "400px"
    },
    dynamicList: {
      type: Array,
      default: () => []
    },
    treeData: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    },
    multiple: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: ''
    }
  },
  methods: {
    init(ids) {
      this.allLoading = true
      getSelectedList(ids).then(res => {
        this.selectedData = res.data.list.map(o => ({
          ...o,
          id: o.type ? o.id + '--' + o.type : o.id,
          icon: o.type === 'system' ? 'icon-ym icon-ym-tree-user2' : o.icon,
          organize: o.type === 'system' ? '动态参数' : o.organize
        }))
        this.ids = this.selectedData.map(o => o.id)
        this.allLoading = false
      })
    },
    search() {
      this.$refs[this.activeName].filter(this.keyword);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data[this.props.label].indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      const boo = this.selectedData.some(o => o.id === data.id + '--' + data.type)
      let item = JSON.parse(JSON.stringify(data))
      item.id += '--' + item.type
      if (boo) return
      this.selectedData.push(item)
      this.ids.push(item.id)
      this.$emit('input', this.ids)
      this.$emit('getValue', this.ids, this.selectedData)
    },
    removeAll() {
      this.selectedData = []
      this.ids = []
      this.$emit('input', this.ids)
      this.$emit('getValue', this.ids, this.selectedData)
    },
    removeData(index) {
      this.selectedData.splice(index, 1)
      this.ids.splice(index, 1)
      this.$emit('input', this.ids)
      this.$emit('getValue', this.ids, this.selectedData)
    },
  }
};
</script>
<style lang="scss" scoped>
.transfer-pane__body-tab {
  >>> .el-tabs__item {
    width: 50% !important;
  }
}
.trends {
  .trends_list {
    padding: 0 10px;
    &.trends_list:hover {
      background-color: #f5f7fa;
      cursor: pointer;
    }
  }
}
</style>