<template>
  <div class="transfer__body" v-loading="allLoading" :style="{height}">
    <div class="transfer-pane">
      <div class="transfer-pane__tools">
        <el-input placeholder="输入关键词进行搜索" v-model="keyword" @keyup.enter.native="search" clearable>
          <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
        </el-input>
      </div>
      <div class="transfer-pane__body left-pane">
        <el-tree :data="treeData" :props="props" :expand-on-click-node="false" default-expand-all
          check-on-click-node @node-click="handleNodeClick" class="JNPF-common-el-tree"
          node-key="id" v-loading="positionLoading" ref="position" :filter-node-method="filterNode">
          <span class="custom-tree-node" slot-scope="{ node, data }">
            <i :class="data.icon"></i>
            <span class="text">{{node.label}}</span>
          </span>
        </el-tree>
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
              <div class="selected-item-text">
                <p class="name">{{item.fullName}}</p>
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
import { Selector } from '@/api/permission/authorize'
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
      treeData: []
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
      this.keyword = ''
      this.allLoading = true
      this.selectedData = []
      Selector().then(res => {
        this.treeData = res.data.list
        this.treeData.forEach(element => {
          if (ids.includes(element.id)) this.selectedData.push(element)
        })
        this.ids = this.selectedData.map(o => o.id)
        this.allLoading = false
      })
    },
    search() {
      this.$refs.position.filter(this.keyword);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data[this.props.label].indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      if (!data.id) return
      let ids = this.selectedData.filter(o => o.id === data.id)
      let id = ''
      if (ids.length) id = ids[0].id
      if (id) return
      let item = JSON.parse(JSON.stringify(data))
      this.selectedData.push(item)
      this.ids.push(data.id)
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