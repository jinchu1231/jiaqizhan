<template>
  <div class="process-container" v-loading="loading">
    <div class="center-container">
      <process-main :conf="templateJson" :flowType="flowType" :formInfo="formData"
        v-if="templateJson" :verifyMode="verifyMode" :key="key" />
    </div>
  </div>
</template>
<script>
import ProcessMain from "./main"
import { NodeUtils, getMockData } from "./FlowCard/util.js";
import draggable from 'vuedraggable'
import { getInfo, update, create } from '@/api/onlineDev/integrate'

export default {
  name: 'Process',
  props: ['flowType', 'conf', 'formInfo'],
  data() {
    return {
      activeConf: null,
      key: + new Date(),
      loading: false,
      verifyMode: false,
      flowList: [],
      visible: false,
      handleType: '',
      defaultData: {},
      formFieldList: {},
      templateJson: null,
      formData: null
    };
  },
  components: {
    ProcessMain,
    draggable
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      getInfo(this.formInfo.id).then(res => {
        this.formData = res.data || {}
        this.templateJson = res.data.templateJson ? JSON.parse(res.data.templateJson) : null;
        this.defaultData = getMockData(this.formData.type)
        this.initData()
      })
    },
    initData() {
      this.loading = true
      if (this.templateJson !== null && typeof this.templateJson === 'object' && JSON.stringify(this.templateJson) !== '{}') {
        this.templateJson = this.templateJson
      } else {
        this.templateJson = JSON.parse(JSON.stringify(this.defaultData))
      }
      this.$nextTick(() => {
        this.loading = false
      })
    },
    // 给父级组件提供的获取流程数据得方法
    getData() {
      return new Promise((resolve, reject) => {
        this.verifyMode = true;
        this.formData.templateJson = this.templateJson
        if (NodeUtils.checkAllNode(this.formData.templateJson)) {
          resolve({ data: this.formData });
        } else {
          reject({ msg: '' });
        }
      });
    },
  }
};
</script>

<style scoped lang="scss">
.process-container {
  display: flex;
  height: 100%;

  .left-container {
    flex-shrink: 0;
    width: 220px;
    margin-right: 10px;
    background: #fff;
    border-radius: 4px;
    height: 100%;
    .left-list {
      padding: 10px;
      height: calc(100% - 40px);
      border-bottom: 1px solid #dcdfe6;
      overflow: auto;
      .left-item {
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border: 1px solid #dcdfe6;
        padding: 0 10px;
        border-radius: 4px;
        margin-bottom: 10px;
        .option-drag {
          margin-right: 6px;
          cursor: move;
        }
        .icon {
          cursor: pointer;
        }
        &.active {
          border: 1px solid #1890ff;
          background: #1890ff;
          .name {
            color: #fff;
          }
          .icon {
            color: #fff;
          }
          .option-drag {
            color: #fff;
          }
        }
        .name {
          width: 160px;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          word-break: break-all;
        }
      }
    }
    .add-btn {
      height: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
    }
  }
  .center-container {
    flex: 1;
    height: 100%;
    overflow: hidden;
  }
}
</style>
