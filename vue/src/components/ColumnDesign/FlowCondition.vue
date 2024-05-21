<template>
  <el-dialog class="JNPF-dialog JNPF-dialog_center" title="过滤规则配置" :visible.sync="dialogVisible"
    width="800px" append-to-body :before-close="handleClose">
    <Condition :value="conditionList" ref="base" :modelType="modelType" :matchLogic="matchLogic"
      :columnOptions="formFieldsOptions">
    </Condition>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="confirm">确 定</el-button>
    </span>
  </el-dialog>
</template>


<script>
import Condition from "./condition";
export default {
  props: {
    modelType: {
      type: [String, Number],
      default: ""
    },
    value: {
      type: Array,
      default: () => []
    },
    formFieldsOptions: {
      type: Array,
      default: () => []
    },
    options: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      tempCondition: [],
      dialogVisible: false,
      conditionList: [],
      columnOptions: [],
      matchLogic: 'and',
    };
  },
  methods: {
    init(data) {
      // 从缓存拿暂存数据
      let ruleData = this.$store.getters.ruleData;
      if (ruleData && ruleData.length > 0) {
        this.conditionList = JSON.parse(JSON.stringify(ruleData.conditionList));
        this.matchLogic = ruleData.matchLogic
      } else {
        this.$store.commit("generator/SET_FILTER_DATA", JSON.parse(JSON.stringify(data)))
        this.conditionList = data.conditionList;
        this.matchLogic = data.matchLogic
      }
    },
    validData() {
      let isOk = true;
      for (let i = 0; i < this.conditionList.length; i++) {
        const e = this.conditionList[i];
        for (let j = 0; j < e.groups.length; j++) {
          const child = e.groups[j];
          if (!child.field) {
            this.$message.warning('条件字段不能为空');
            isOk = false;
            return;
          }
          if (!child.symbol) {
            this.$message.warning('条件符号不能为空');
            isOk = false;
            return;
          }
          if (!['null', 'notNull'].includes(child.symbol) && !['switch'].includes(child.jnpfKey) && ((!child.fieldValue && child.fieldValue !== 0) || ((typeof child.fieldValue === "object" && child.fieldValue.length == 0)) || this.jnpf.isEmpty(child.fieldValue))) {
            this.$message.warning('数据值不能为空');
            isOk = false;
            return;
          }
        }
      }
      return isOk;
    },
    confirm() {
      // 获取属性配置
      this.$nextTick(() => {
        let item = this.$refs.base && this.$refs.base.getData() || ''
        this.conditionList = item ? item.conditionList : []
        if (!this.validData()) {
          return;
        }
        let cloneConditions = {
          conditionList: JSON.parse(JSON.stringify(this.conditionList)),
          matchLogic: item.matchLogic
        }
        this.$store.commit("generator/SET_FILTER_DATA", cloneConditions)
        this.$emit("ruleConfig", cloneConditions);
        this.dialogVisible = false;
      });
    },
    handleClose() {
      this.$emit("ruleConfig", this.$store.getters.ruleData);
      this.dialogVisible = false;
    },
    show(data) {
      this.dialogVisible = true;
      if (data && (typeof data === "object" && data.length == 0) || data.conditionList.length == 0) {
        this.conditionList = [];
        this.addCondition();
      } else {
        this.init(data);
      }
    },
    addCondition() {
      let item = {
        logic: "and",
        groups: [{
          fieldValue: '',
          field: '',
          symbol: '',
          jnpfKey: '',
          cellKey: +new Date()
        }]
      };
      this.conditionList.push(item);
    }
  },
  components: {
    Condition
  }
};
</script>

<style lang="scss" scoped>
.title {
  font-size: 15px;
  color: black;
  font-weight: 400;
}
</style>
