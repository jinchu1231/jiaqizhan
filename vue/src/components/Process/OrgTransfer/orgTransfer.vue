<template>
  <div class="transfer__body" v-loading="loading" :style="{height}">
    <div class="transfer-pane">
      <div class="transfer-pane__tools">
        <el-input placeholder="输入关键词进行搜索" v-model="filterText" @keyup.enter.native="search"
          clearable>
          <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
        </el-input>
      </div>
      <div class="transfer-pane__body">
        <el-tabs v-model="activeName" class="transfer-pane__body-tab">
          <el-tab-pane label="组织" name="organize">
            <el-tree :data="treeData" :props="props" default-expand-all ref="treeBox"
              :expand-on-click-node="false" class="JNPF-common-el-tree" node-key="id"
              v-loading="loading" :filter-node-method="filterNode" @node-click="handleNodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <i :class="data.icon"></i>
                <span class="text">{{node.label}}</span>
              </span>
            </el-tree>
          </el-tab-pane>
          <el-tab-pane label="动态参数" name="trends">
            <div class="trends">
              <p v-for="(item,index) in dynamicList" :key="index" class="trends_list"
                @click="handleNodeClick(item)">
                {{ item.fullName }}</p>
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
        <template>
          <div v-for="(item, index) in selectedTextData" :key=" index" class="selected-item">
            <span>{{ item}}</span>
            <i class="el-icon-delete" @click="removeData(item,index)"></i>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'JNPF-transfer',
  data() {
    return {
      selectedData: [],
      selectedTextData: [],
      filterText: '',
      activeName: 'organize',
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
    loading: {
      type: Boolean,
      default: false
    },
    multiple: {
      type: Boolean,
      default: false
    },
    treeData: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    },
    props: {
      type: Object,
      default: () => {
        return { value: "id", label: "fullName", children: "children" };
      }
    },
    type: {
      type: String,
      default: ''
    }
  },
  computed: {
    allList() {
      return this.$store.getters.departmentList || []
    }
  },
  watch: {
    value(val) {
      this.initHandle()
    },
  },
  methods: {
    initHandle() {
      if (!this.value || !this.value.length) {
        this.selectedTextData = [];
        this.selectedData = [];
        return
      }
      let allList = [...this.dynamicList, ...this.allList]
      let selectedData = this.value
      this.selectedData = JSON.parse(JSON.stringify(selectedData))
      let textList = []
      for (let i = 0; i < selectedData.length; i++) {
        const item = selectedData[i];
        let textItem = JSON.parse(JSON.stringify(item))
        for (let j = 0; j < item.length; j++) {
          inner: for (let ii = 0; ii < allList.length; ii++) {
            if (item[j] === allList[ii].id) {
              textItem[j] = allList[ii].fullName
              break inner
            }
          }
        }
        textList.push(textItem)
      }
      this.selectedTextData = textList.map(o => o.join('/'))
    },
    search() {
      this.$refs.treeBox.filter(this.filterText);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.fullName.indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      let currId = data.organizeIds
      let currData = data.organize
      const boo = this.selectedData.some(o => o.join('/') === currId.join('/'))
      if (boo) return
      this.selectedData.push(currId)
      this.selectedTextData.push(currData)
      this.$emit('input', this.selectedData)
      this.$emit('getValue', this.selectedData, this.selectedTextData)
    },
    removeData(item, index) {
      this.selectedData.splice(index, 1)
      this.selectedTextData.splice(index, 1)
      this.$emit('input', this.selectedData)
      this.$emit('getValue', this.selectedData, this.selectedTextData)
    },
    removeAll() {
      this.selectedData = []
      this.selectedTextData = []
      this.$emit('input', this.selectedData)
      this.$emit('getValue', this.selectedData, this.selectedTextData)
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