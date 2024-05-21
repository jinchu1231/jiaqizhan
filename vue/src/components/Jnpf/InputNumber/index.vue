<template>
  <div>
    <template v-if='!controls'>
      <div class="el-input el-input--small el-input-group el-input-group--prepend el-input--suffix">
        <div class="el-input-group__prepend" v-if="addonBefore">{{ addonBefore }}</div>
        <el-input-number v-model="innerValue" :placeholder="placeholder" :max="max" :min="min"
          :step="step" :precision="precision" :disabled="disabled" v-thousands
          :class="addonBefore&&addonAfter? 'addon':addonBefore?'addonBefore':addonAfter?'addonAfter':'input-number'"
          :controls-position="controlsPosition" :key="key2" :controls="false" @change="change"
          @blur="onblur($event)">
        </el-input-number>
        <div class="el-input-group__append" v-if="addonAfter">{{ addonAfter }}</div>
      </div>
    </template>
    <template v-else>
      <el-input-number v-model="innerValue" :placeholder="placeholder" :max="max" :min="min"
        :step="step" :precision="precision" :disabled="disabled" class="work-number"
        :controls-position="controlsPosition" @blur="onblur($event)" :controls="controls"
        @change="change">
      </el-input-number>
    </template>
    <p v-if="isAmountChinese" class="amountChinese">{{ amountChineseName }}</p>
  </div>
</template>
<script>
import { getAmountChinese, thousandsFormat } from "@/components/Generator/utils/index"

export default {
  name: 'JnpfInputNumber',
  components: {},
  props: {
    controlsPosition: {
      type: String,
      default: ''
    },
    controls: {
      type: Boolean,
      default: true
    },
    value: {
      type: [Number, String],
      default: undefined
    },
    max: {
      type: Number,
      default: Infinity
    },
    min: {
      type: Number,
      default: -Infinity
    },
    step: {
      type: Number,
      default: 1
    },
    precision: {
      type: Number,
      default: undefined
    },
    disabled: {
      type: Boolean,
      default: false
    },
    thousands: {
      type: Boolean,
      default: false
    },
    isAmountChinese: {
      type: Boolean,
      default: false
    },
    addonBefore: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '数字文本'
    },
    addonAfter: {
      type: String,
      default: ''
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      innerValue: undefined,
      amountChineseName: '',
      key2: ''
    }
  },
  watch: {
    innerValue: {
      handler(val) {
        this.amountChinese(val)
      },
      deep: true
    },
    value: {
      handler(val) {
        let value = val ? val : val === 0 ? 0 : undefined
        this.innerValue = value
        this.amountChinese(val)
      },
      deep: true,
      immediate: true
    },
    isAmountChinese() {
      this.amountChinese(this.value)
    },
    thousands(val) {
      this.key2 = +new Date()
    }
  },
  directives: {
    thousands: {
      // 被绑定元素插入父节点时调用
      inserted: (el, binding, vnode) => {
        let precision = vnode.child.precision
        let thousands = vnode.context.thousands
        // 获取input节点
        if (el.tagName.toLocaleUpperCase() !== 'INPUT') {
          el = el.getElementsByTagName('input')[0]
        }
        if (thousands) {
          // 千分位
          el.value = vnode.context.format_number_thousandth(el.value)
          // 聚焦转化为数字格式（去除千分位）
          el.onfocus = e => {
            if (el.value) {
              let a = el.value.replace(/,/g, '') //去除千分号的','
              el.value = Number(parseFloat(a))
              if (precision) el.value = Number(el.value).toFixed(precision)
            }
          }
          el.onblur = e => {
            el.value = vnode.context.format_number_thousandth(el.value)
          }
        }
      },
      componentUpdated: (el, binding, vnode) => {
        // 聚焦转化为数字格式（去除千分位）
        el.focus()
        let precision = vnode.child.precision
        let thousands = vnode.context.thousands
        if (thousands) {
          if (el.tagName.toLocaleUpperCase() !== 'INPUT') {
            el = el.getElementsByTagName('input')[0]
          }
          el.onblur = e => {
            if (el.value) {
              if (precision) el.value = Number(el.value).toFixed(precision)
              el.value = vnode.context.format_number_thousandth(el.value)
            }
          }
        }
      },
      unbind: (el, binding, vnode) => {
        // 聚焦转化为数字格式（去除千分位）
        if (el.tagName.toLocaleUpperCase() !== 'INPUT') {
          el = el.getElementsByTagName('input')[0]
        }
      }
    }
  },
  methods: {
    amountChinese(val) {
      if (!this.isAmountChinese) return
      this.amountChineseName = getAmountChinese(val)
    },
    format_number_thousandth(num) {
      return thousandsFormat(num)
    },
    onblur(event) {
      this.$emit('blur', event)
    },
    change(val) {
      let value = val ? val : val == 0 ? 0 : undefined
      this.$emit('input', value)
      this.$emit('change', value)
    },
  }
}
</script>
<style lang="scss" scoped>
.input-number {
  width: 100% !important;
  min-width: 60px !important;
  >>> .el-input__inner {
    border-radius: 4px !important;
    text-align: left;
  }
}
.addon {
  width: 100%;
  >>> .el-input__inner {
    border-radius: unset !important;
  }
}
.addonBefore {
  width: 100%;
  >>> .el-input__inner {
    border-top-left-radius: unset !important;
    border-bottom-left-radius: unset !important;
  }
}
.addonAfter {
  width: 100%;
  >>> .el-input__inner {
    border-bottom-right-radius: unset !important ;
    border-top-right-radius: unset !important ;
    border-top-left-radius: 4px !important;
    border-bottom-left-radius: 4px !important;
  }
}
.amountChinese {
  color: #c0c0c0;
}
</style>