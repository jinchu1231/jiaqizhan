<template>
  <div>
    <template v-if="!detailed">
      <el-input v-model="innerValue" v-if="isStorage==0" readonly placeholder="用于展示计算结果，且数据不会保存" />
      <el-input v-model="innerValue" v-else readonly placeholder="用于展示计算结果，且数据同时会保存入库" />
    </template>
    <p v-else>{{innerValue}}</p>
    <p v-if="isAmountChinese" class="isAmountChinese amountChinese">{{ amountChineseName }}</p>
  </div>
</template>
<script>
import { getAmountChinese, thousandsFormat, mergeNumberOfExps, validExp, toRPN, calcRPN, debounce } from "@/components/Generator/utils/index"
export default {
  model: {
    prop: 'value',
    event: 'input'
  },
  props: [
    "value",
    "formData",
    "expression",
    "tableVModel",
    "componentVModel",
    'isStorage',
    "rowIndex", // 计算公式放在表格中时， 需要获取在表格中的行位置
    "detailed",
    "isAmountChinese",
    "thousands",
    'precision'
  ],
  name: 'JnpfCalculate',
  data() {
    return {
      innerValue: this.value,
      RPN_EXP: toRPN(mergeNumberOfExps(this.expression)),
      setValue: 0,
      amountChineseName: ''
    }
  },
  computed: {
    rootFormData() {
      return this.formData || this.getFormData()
    },
  },
  methods: {
    getFormData() {
      var root = this.$parent
      while (root) {
        if ('vmFormData' in root) {
          return root.vmFormData
        }
        root = root.$parent
      }
    },
    /**
     * 获取指定组件的值
     */
    getFormVal(vModel) {
      try {
        if (vModel.indexOf('.') > -1) {
          let [tableVModel, cmpVModel] = vModel.split('.')
          if (typeof this.rowIndex === 'number') {
            return this.rootFormData[tableVModel][this.rowIndex][cmpVModel] || 0
          } else {
            return this.rootFormData[tableVModel].reduce((sum, c) => (c[cmpVModel] ? Number(c[cmpVModel]) : 0) + sum, 0)
          }
        }
        return this.rootFormData[vModel] || 0
      } catch (error) {
        console.warn('计算公式出错, 可能包含无效的组件值', error)
        return 0
      }
    },
    /**
     * 计算表达式
     */
    execRPN() {
      const temp = this.RPN_EXP.map(t => typeof t === 'object' ? this.getFormVal(t.__vModel__) : t)
      this.innerValue = parseFloat(parseFloat(calcRPN(temp)).toFixed(this.precision || 0))
      this.setValue = JSON.parse(JSON.stringify(this.innerValue))
      if (isNaN(this.innerValue)) this.innerValue = 0
      if (isNaN(this.setValue)) this.setValue = 0
      this.$emit('input', this.setValue)
      if (this.rowIndex >= 0 && this.componentVModel && this.tableVModel) {
        if (this.rootFormData[this.tableVModel][this.rowIndex][this.componentVModel] !== this.innerValue) {
          this.$emit('change', this.setValue)
        }
      }
      if (this.thousands) {
        this.innerValue = thousandsFormat(this.innerValue)
      }
      if (this.isAmountChinese) this.amountChineseName = getAmountChinese(this.setValue||0)
    }
  },
  watch: {
    formData: {
      handler: function (val) {
        if (!val) return
        if (!this.computeExps) { // formData更新可能比较频繁
          this.computeExps = debounce(this.execRPN, 100)
        }
        this.computeExps()
      },
      deep: true,
      immediate: true
    }

  },
}
</script>
<style lang="scss" scoped>
.isAmountChinese {
  color: #c0c0c0;
}
</style>