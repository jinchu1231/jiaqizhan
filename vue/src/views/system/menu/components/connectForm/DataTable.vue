<template>
  <div class="popupSelect-container">
    <div class="el-select" @click.stop="openDialog">
      <div class="el-select__tags" v-if="multiple" ref="tags"
        :style="{ 'max-width': inputWidth - 32 + 'px', width: '100%',cursor:'pointer' }">
        <span v-if="collapseTags && tagsList.length">
          <el-tag :closable="!selectDisabled" :size="collapseTagSize" type="info"
            @close="deleteTag($event, 0)" disable-transitions>
            <span class="el-select__tags-text">{{ tagsList[0].table }}</span>
          </el-tag>
          <el-tag v-if="tagsList.length > 1" :closable="false" type="info" disable-transitions>
            <span class="el-select__tags-text">+ {{ tagsList.length - 1 }}</span>
          </el-tag>
        </span>
        <transition-group @after-leave="resetInputHeight" v-if="!collapseTags">
          <el-tag v-for="(item,i) in tagsList" :key="item.table" :size="collapseTagSize"
            :closable="!selectDisabled" type="info" @close="deleteTag($event, i)"
            disable-transitions>
            <span class="el-select__tags-text">{{ item.table }}</span>
          </el-tag>
        </transition-group>
      </div>
      <el-input ref="reference" v-model="innerValue" type="text" :placeholder="currentPlaceholder"
        :disabled="selectDisabled" readonly :validate-event="false"
        :tabindex="(multiple) ? '-1' : null" @mouseenter.native="inputHovering = true"
        @mouseleave.native="inputHovering = false">
        <template slot="suffix">
          <i v-show="!showClose"
            :class="['el-select__caret', 'el-input__icon', 'el-icon-arrow-up']"></i>
          <i v-if="showClose" class="el-select__caret el-input__icon el-icon-circle-close"
            @click="handleClearClick"></i>
        </template>
      </el-input>
    </div>
    <el-dialog title="数据选择" :close-on-click-modal="false" :visible.sync="visible"
      class="JNPF-dialog JNPF-dialog_center table-dialog" lock-scroll append-to-body width="800px"
      :modal-append-to-body="false" @close="onClose">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="10">
            <el-form-item label="关键词">
              <el-input v-model="keyword" placeholder="请输入关键词查询" clearable
                @keyup.enter.native="initData()" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="initData()">
                {{ $t("common.search") }}</el-button>
              <el-button icon="el-icon-refresh-right" @click="refresh()">{{ $t("common.reset") }}
              </el-button>
            </el-form-item>
          </el-col>
        </el-form>
        <div class="JNPF-common-search-box-right" style="margin-top: 10px;">
          <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
            <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
              @click="initData()" />
          </el-tooltip>
        </div>
      </el-row>
      <JNPF-table v-loading="listLoading" :data="list" :border="false" highlight-current-row
        row-key="table" ref="multipleTable" @selection-change="handleSelectionChange" :hasNO="false"
        has-c>
        <el-table-column type="index" width="50" label="序号" align="center" />
        <el-table-column prop="table" label="表名" width="300" />
        <el-table-column prop="tableName" label="说明" show-overflow-tooltip />
      </JNPF-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="onCloseVisible">{{$t("common.cancelButton")}}</el-button>
        <el-button type="primary" @click="confirm()">{{$t("common.confirmButton")}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { addResizeListener, removeResizeListener } from 'element-ui/src/utils/resize-event';
import { DataModelList } from "@/api/systemData/dataModel";
export default {
  name: 'dataTable',
  inject: {
    elForm: {
      default: ''
    },
    elFormItem: {
      default: ''
    }
  },
  props: {
    dbLinkId: {
      default: ''
    },
    value: {
      default: ''
    },
    placeholder: {
      type: String,
      default: '请选择'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    multiple: {
      type: Boolean,
      default: false
    },
    collapseTags: {
      type: Boolean,
      default: false
    },
    clearable: {
      type: Boolean,
      default: false
    },
    size: String,
  },
  data() {
    return {
      treeData: [],
      allList: [],
      keyword: '',
      innerValue: '',
      visible: false,
      loading: false,
      props: {
        children: 'children',
        label: 'table',
        isLeaf: 'isLeaf'
      },
      listLoading: true,
      keyword: "",
      list: [],
      checked: "",
      checkedList: [],
      selectedData: [],
      tagsList: [],
      inputHovering: false,
      inputWidth: 0,
      initialInputHeight: 0,
    }
  },
  computed: {
    showClose() {
      let hasValue = this.multiple
        ? Array.isArray(this.value) && this.value.length > 0
        : this.value !== undefined && this.value !== null && this.value !== '';
      let criteria = this.clearable &&
        !this.selectDisabled &&
        this.inputHovering &&
        hasValue;
      return criteria;
    },
    currentPlaceholder() {
      if (this.multiple && Array.isArray(this.value) && this.value.length) {
        return ''
      } else {
        return this.placeholder
      }
    },
    selectDisabled() {
      return this.disabled || (this.elForm || {}).disabled;
    },
    _elFormItemSize() {
      return (this.elFormItem || {}).elFormItemSize;
    },
    selectSize() {
      return this.size || this._elFormItemSize || (this.$ELEMENT || {}).size;
    },
    collapseTagSize() {
      return ['small', 'mini'].indexOf(this.selectSize) > -1
        ? 'mini'
        : 'small';
    },
  },
  created() {
    this.getData()
  },
  mounted() {
    addResizeListener(this.$el, this.handleResize);

    const reference = this.$refs.reference;
    if (reference && reference.$el) {
      const sizeMap = {
        medium: 36,
        small: 32,
        mini: 28
      };
      const input = reference.$el.querySelector('input');
      this.initialInputHeight = input.getBoundingClientRect().height || sizeMap[this.selectSize];
    }
    if (this.multiple) {
      this.resetInputHeight();
    }
    this.$nextTick(() => {
      if (reference && reference.$el) {
        this.inputWidth = reference.$el.getBoundingClientRect().width;
      }
    });
    this.setDefault()
  },
  beforeDestroy() {
    if (this.$el && this.handleResize) removeResizeListener(this.$el, this.handleResize);
  },
  watch: {
    value(val) {
      this.setDefault()
    },
    selectDisabled() {
      this.$nextTick(() => {
        this.resetInputHeight();
      });
    }
  },
  methods: {
    async getData() {
      const dbLinkId = this.dbLinkId || "0";
      DataModelList(dbLinkId, {
        keyword: this.keyword,
        currentPage: 1,
        pageSize: 1000000
      }).then((res) => {
        this.allList = res.data.list
        this.setDefault()
      })
    },
    echoTable(rows) {
      rows.forEach(row => {
        this.$refs.multipleTable.$refs.JNPFTable.toggleRowSelection(row, true)
      })
    },
    closeDialog() {
      this.checked = "";
      this.selectedData = [];
      this.$emit("update:visible", false);
    },
    onCloseVisible() {
      if (this.allList.length && this.selectedData.length) {
        this.$nextTick(() => {
          let selData = []
          this.allList.forEach(i => {  // 循环嵌套
            this.selectedData.forEach(item => {
              if (i.table === item.table) { // 判断哪些数据是需要回显的
                selData.push(i)    // 需要回显的数据整合起来               
              }
            })
          })
          this.echoTable(selData) // 调用回显方法
        })
      }
      this.visible = false
    },
    refresh() {
      this.keyword = "";
      this.initData();
    },
    initData() {
      this.getData()
      this.listLoading = true;
      const dbLinkId = this.dbLinkId || "0";
      DataModelList(dbLinkId, {
        keyword: this.keyword,
        currentPage: 1,
        pageSize: 1000000
      }).then((res) => {
        this.list = res.data.list;
        this.listLoading = false;
        if (this.list.length && this.selectedData.length) {
          this.$nextTick(() => {
            let selData = []
            this.list.forEach(i => {  // 循环嵌套
              this.selectedData.forEach(item => {
                if (i.table === item.table) { // 判断哪些数据是需要回显的
                  selData.push(i)    // 需要回显的数据整合起来               
                }
              })
            })
            this.echoTable(selData) // 调用回显方法
          })
        }
      });
    },
    handleSelectionChange(val) {
      if (val.length > 0) {
        this.selectedData = val;
      }
    },
    onClose() { },
    openDialog() {
      if (this.selectDisabled) return
      this.refresh()
      this.setDefault()
      this.visible = true
    },
    filterNode(value, data) {
      if (!value) return true;
      return data[this.props.label].indexOf(value) !== -1;
    },
    removeAll() {
      this.selectedData = []
    },
    removeData(index) {
      this.selectedData.splice(index, 1)
    },
    confirm() {
      if (this.multiple) {
        this.innerValue = ''
        this.tagsList = JSON.parse(JSON.stringify(this.selectedData))
        let selectedIds = this.selectedData.map(o => o.table)
        this.$emit('input', selectedIds)
        this.$emit('change', selectedIds, this.selectedData)
      }
      this.visible = false
    },
    setDefault() {
      if (!this.value || !this.value.length) {
        this.innerValue = ''
        this.selectedData = []
        this.tagsList = []
        return
      }

      const arr = this.multiple ? this.value : [this.value]
      let selectedData = []
      for (let i = 0; i < arr.length; i++) {
        const item = arr[i];
        inner: for (let j = 0; j < this.allList.length; j++) {
          if (item === this.allList[j].table) {
            selectedData.push(this.allList[j])
            break inner
          }
        }
      }
      this.selectedData = selectedData
      if (this.multiple) {
        this.innerValue = ''
        this.tagsList = JSON.parse(JSON.stringify(this.selectedData))
        this.$nextTick(() => {
          this.resetInputHeight();
        })
      } else {
        if (!this.selectedData.length) return this.innerValue = ''
        this.innerValue = this.selectedData[0].table
      }
    },
    deleteTag(event, index) {
      this.selectedData.splice(index, 1)
      this.confirm()
      event.stopPropagation();
    },
    handleClearClick(event) {
      this.selectedData = []
      this.confirm()
      event.stopPropagation();
    },
    resetInputWidth() {
      this.inputWidth = this.$refs.reference.$el.getBoundingClientRect().width;
    },
    handleResize() {
      this.resetInputWidth();
      if (this.multiple) this.resetInputHeight();
    },
    resetInputHeight() {
      if (this.collapseTags) return;
      this.$nextTick(() => {
        if (!this.$refs.reference) return;
        let inputChildNodes = this.$refs.reference.$el.childNodes;
        let input = [].filter.call(inputChildNodes, item => item.tagName === 'INPUT')[0];
        const tags = this.$refs.tags;
        const tagsHeight = tags ? Math.round(tags.getBoundingClientRect().height) : 0;
        const sizeInMap = this.initialInputHeight || 40;
        input.style.height = this.selectedData.length === 0
          ? sizeInMap + 'px'
          : Math.max(
            tags ? (tagsHeight + (tagsHeight > sizeInMap ? 6 : 0)) : 0,
            sizeInMap
          ) + 'px';
      });
    },
    resetInputWidth() {
      this.inputWidth = this.$refs.reference.$el.getBoundingClientRect().width;
    },
    handleResize() {
      this.resetInputWidth();
      if (this.multiple) this.resetInputHeight();
    }
  }
}
</script>
<style lang="scss" scoped>
.table-dialog {
  >>> .el-dialog__body {
    height: 70vh;
    padding: 0 !important;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    .JNPF-common-search-box {
      margin-bottom: 0;
      .JNPF-common-search-box-right {
        padding: 10px 10px 0 0;
      }
    }
  }
}
</style>